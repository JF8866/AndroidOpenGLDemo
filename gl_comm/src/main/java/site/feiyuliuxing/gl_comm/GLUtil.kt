package site.feiyuliuxing.gl_comm

import android.opengl.GLES30
import android.util.Log

object GLUtil {
    private const val TAG = "GLUtil"

    fun createShaderProgram(vertexShaderSource: String, fragmentShaderSource: String): Int {
        val vShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        val fShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(vShader, vertexShaderSource)
        GLES30.glShaderSource(fShader, fragmentShaderSource)

        val status = IntArray(1)

        GLES30.glCompileShader(vShader)
        checkOpenGLError()
        GLES30.glGetShaderiv(vShader, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] != 1) {
            Log.e(TAG, "vertex compilation failed")
            printShaderLog(vShader)
        }

        GLES30.glCompileShader(fShader)
        checkOpenGLError()
        GLES30.glGetShaderiv(fShader, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] != 1) {
            Log.e(TAG, "fragment compilation failed")
            printShaderLog(fShader)
        }

        val vfProgram = GLES30.glCreateProgram()
        GLES30.glAttachShader(vfProgram, vShader)
        GLES30.glAttachShader(vfProgram, fShader)
        GLES30.glLinkProgram(vfProgram)
        checkOpenGLError()
        GLES30.glGetProgramiv(vfProgram, GLES30.GL_LINK_STATUS, status, 0)
        if (status[0] != 1) {
            Log.e(TAG, "linking failed")
            printProgramLog(vfProgram)
        }
        return vfProgram
    }

    private fun printShaderLog(shader: Int) {
        val len = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_INFO_LOG_LENGTH, len, 0)
        if (len[0] > 0) {
            val log = GLES30.glGetShaderInfoLog(shader)
            Log.e(TAG, "Shader Info Log: $log")
        }
    }

    private fun printProgramLog(prog: Int) {
        val len = IntArray(1)
        GLES30.glGetProgramiv(prog, GLES30.GL_INFO_LOG_LENGTH, len, 0)
        Log.e(TAG, "printProgramLog() - log length=${len[0]}")
        if (len[0] > 0) {
            val log = GLES30.glGetProgramInfoLog(prog)
            Log.e(TAG, "Program Info Log: $log")
        }
    }

    private fun checkOpenGLError(): Boolean {
        var foundError = false
        var glErr = GLES30.glGetError()
        while (glErr != GLES30.GL_NO_ERROR) {
            Log.e(TAG, "glError: $glErr")
            foundError = true
            glErr = GLES30.glGetError()
        }
        return foundError
    }
}