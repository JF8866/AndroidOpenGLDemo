package site.feiyuliuxing.gl_comm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES11Ext.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT
import android.opengl.GLES11Ext.GL_TEXTURE_MAX_ANISOTROPY_EXT
import android.opengl.GLES30.*
import android.opengl.GLUtils
import android.util.Log
import androidx.annotation.DrawableRes
import java.nio.ByteBuffer

object GLUtil {
    private const val TAG = "GLUtil"

    fun createShaderProgram(vertexShaderSource: String, fragmentShaderSource: String): Int {
        val vShader = glCreateShader(GL_VERTEX_SHADER)
        val fShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(vShader, vertexShaderSource)
        glShaderSource(fShader, fragmentShaderSource)

        val status = IntArray(1)

        glCompileShader(vShader)
        checkOpenGLError()
        glGetShaderiv(vShader, GL_COMPILE_STATUS, status, 0)
        if (status[0] != 1) {
            Log.e(TAG, "vertex compilation failed")
            printShaderLog(vShader)
        }

        glCompileShader(fShader)
        checkOpenGLError()
        glGetShaderiv(fShader, GL_COMPILE_STATUS, status, 0)
        if (status[0] != 1) {
            Log.e(TAG, "fragment compilation failed")
            printShaderLog(fShader)
        }

        val vfProgram = glCreateProgram()
        glAttachShader(vfProgram, vShader)
        glAttachShader(vfProgram, fShader)
        glLinkProgram(vfProgram)
        checkOpenGLError()
        glGetProgramiv(vfProgram, GL_LINK_STATUS, status, 0)
        if (status[0] != 1) {
            Log.e(TAG, "linking failed")
            printProgramLog(vfProgram)
        }
        return vfProgram
    }

    private fun printShaderLog(shader: Int) {
        val len = IntArray(1)
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0)
        if (len[0] > 0) {
            val log = glGetShaderInfoLog(shader)
            Log.e(TAG, "Shader Info Log: $log")
        }
    }

    private fun printProgramLog(prog: Int) {
        val len = IntArray(1)
        glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0)
        Log.e(TAG, "printProgramLog() - log length=${len[0]}")
        if (len[0] > 0) {
            val log = glGetProgramInfoLog(prog)
            Log.e(TAG, "Program Info Log: $log")
        }
    }

    private fun checkOpenGLError(): Boolean {
        var foundError = false
        var glErr = glGetError()
        while (glErr != GL_NO_ERROR) {
            Log.e(TAG, "glError: $glErr")
            foundError = true
            glErr = glGetError()
        }
        return foundError
    }

    fun Context.loadTexture(@DrawableRes img: Int): Int {
        val options = BitmapFactory.Options()
        options.inScaled = false
        val bitmap = BitmapFactory.decodeResource(resources, img, options)
        return loadTexture(bitmap)
    }

    fun loadTexture(bitmap: Bitmap): Int {
        Log.d(TAG, "bitmap size: ${bitmap.width} x ${bitmap.height}")

        val textures = IntArray(1)
        glGenTextures(1, textures, 0)
        val textureID = textures[0]
        if (textureID == 0) {
            Log.e(TAG, "Could not generate a new OpenGL textureId object.")
            return 0
        }
        glBindTexture(GL_TEXTURE_2D, textureID)

        // https://developer.android.google.cn/reference/android/opengl/GLES20#glTexImage2D(int,%20int,%20int,%20int,%20int,%20int,%20int,%20int,%20java.nio.Buffer)
        /*      int target,
                int level,
                int internalformat,
                int width,
                int height,
                int border,
                int format,
                int type,
                Buffer pixels */
        val pixels = ByteBuffer.allocateDirect(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(pixels)
        pixels.position(0)//这步比较关键，不然无法加载纹理数据

        val internalformat = GLUtils.getInternalFormat(bitmap)
        val type = GLUtils.getType(bitmap)
//        Log.i(TAG, "internalformat=$internalformat, GL_RGBA=$GL_RGBA")
//        Log.i(TAG, "type=$type, GL_UNSIGNED_BYTE=$GL_UNSIGNED_BYTE")
//        glTexImage2D(GL_TEXTURE_2D, 0, internalformat, bitmap.width, bitmap.height, 0, internalformat, type, pixels)
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        glGenerateMipmap(GL_TEXTURE_2D)

        val ext = glGetString(GL_EXTENSIONS)
//        Log.e(TAG, ext)
        if (ext.contains("GL_EXT_texture_filter_anisotropic")) {
            val anisoset = FloatArray(1)
            glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoset, 0)
            Log.d(TAG, "anisoset=${anisoset[0]}")
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoset[0])
        }
        bitmap.recycle()
        return textureID
    }
}