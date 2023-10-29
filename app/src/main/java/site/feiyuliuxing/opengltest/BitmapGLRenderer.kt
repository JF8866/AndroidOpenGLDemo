package site.feiyuliuxing.opengltest

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

private const val TAG = "BitmapGLRenderer"

//原文链接：https://blog.csdn.net/gongxiaoou/article/details/89344561

class BitmapGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    companion object {

        private val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            attribute vec2 aTextureCoord;
            varying vec2 vTextureCoord;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                vTextureCoord = aTextureCoord;
            }
        """.trimIndent()

        private val fragmentShaderSource = """
            precision mediump float;
            uniform sampler2D uTextureUnit;
            varying vec2 vTextureCoord;
            void main(void) {
                gl_FragColor = texture2D(uTextureUnit,vTextureCoord);
            }
        """.trimIndent()

        /**
         * 顶点坐标
         * (x,y,z)
         */
        private val POSITION_VERTEX = floatArrayOf(
            0f, 0f, 0f,     //顶点坐标V0
            1f, 1f, 0f,     //顶点坐标V1
            -1f, 1f, 0f,    //顶点坐标V2
            -1f, -1f, 0f,   //顶点坐标V3
            1f, -1f, 0f     //顶点坐标V4
        )

        /**
         * 纹理坐标
         * (s,t)
         */
        private val TEX_VERTEX = floatArrayOf(
            0.5f, 0.5f, //纹理坐标V0
            1f, 0f,     //纹理坐标V1
            0f, 0f,     //纹理坐标V2
            0f, 1.0f,   //纹理坐标V3
            1f, 1.0f    //纹理坐标V4
        )

        /**
         * 绘制顺序索引
         */
        private val VERTEX_INDEX = shortArrayOf(
            0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
            0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
            0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
            0, 4, 1   //V0,V4,V1 三个顶点组成一个三角形
        )
    }

    private val vertexBuffer = ByteBuffer.allocateDirect(POSITION_VERTEX.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(POSITION_VERTEX)
        .position(0)

    private val textureVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(TEX_VERTEX)
        .position(0)

    private val vertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.size * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .put(VERTEX_INDEX)
        .position(0)

    private var muMVPMatrixHandle = 0
    private var programHandle = 0

    //(相机)视图矩阵
    private val vMatrix = FloatArray(16)
    private val vPMatrix = FloatArray(16)

    //投影矩阵
    private val projMatrix = FloatArray(16)

    private var textureId = 0
    private var bitmapWidth = 0
    private var bitmapHeight = 0

    private fun loadTexture(resourceId: Int): Int {
        val textureIds = intArrayOf(0)
        //创建一个纹理对象
        GLES20.glGenTextures(1, textureIds, 0)
        if (textureIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL textureId object.")
            return 0
        }
        val options = BitmapFactory.Options()
        //这里需要加载原图未经缩放的数据
        options.inScaled = false
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
        if (bitmap == null) {
            Log.e(TAG, "Resource ID $resourceId could not be decoded.")
            GLES20.glDeleteTextures(1, textureIds, 0)
            return 0
        }
        bitmapWidth = bitmap.width
        bitmapHeight = bitmap.height
        //绑定纹理到OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        //设置默认的纹理过滤参数
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR_MIPMAP_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        //加载bitmap到纹理中
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        //生成MIP贴图
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        //数据如果已经被加载进OpenGL,则可以回收该bitmap
        bitmap.recycle()
        //取消绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textureIds[0]
    }

    private fun compileShaders(): Int {
        val status = intArrayOf(0)

        println(vertexShaderCode)
        println(fragmentShaderSource)

        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertexShader, vertexShaderCode)
        GLES20.glCompileShader(vertexShader)
        GLES20.glGetShaderiv(vertexShader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e(TAG, "顶点着色器编译失败：" + GLES20.glGetShaderInfoLog(vertexShader))
        }

        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShader, fragmentShaderSource)
        GLES20.glCompileShader(fragmentShader)
        GLES20.glGetShaderiv(fragmentShader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e(TAG, "片段着色器编译失败：" + GLES20.glGetShaderInfoLog(fragmentShader))
        }

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e(TAG, "链接源程序失败：" + GLES20.glGetProgramInfoLog(program))
        }

        // Delete the shaders as the program has them now
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)

        return program
    }

    private fun drawFrame() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        val positionHandle = GLES20.glGetAttribLocation(programHandle, "vPosition")
        val textureCoordHandle = GLES20.glGetAttribLocation(programHandle, "aTextureCoord")
        GLES20.glEnableVertexAttribArray(positionHandle)

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        //启用纹理坐标属性
        GLES20.glEnableVertexAttribArray(textureCoordHandle);
        GLES20.glVertexAttribPointer(
            textureCoordHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            textureVertexBuffer
        )
        //激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        // 绘制
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            VERTEX_INDEX.size,
            GLES20.GL_UNSIGNED_SHORT,
            vertexIndexBuffer
        )

        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        println("onSurfaceCreated")
        programHandle = compileShaders()
        textureId = loadTexture(R.mipmap.gougou)
        GLES20.glUseProgram(programHandle)
        muMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix")
        // Create a camera view matrix
        Matrix.setLookAtM(
            vMatrix, 0, 0f, 0f, -6f,
            0f, 0f, 0f, 0f, 1.0f, 0.0f
        )
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        println("onSurfaceChanged() - ${width}x${height}")

        val w = bitmapWidth
        val h = bitmapHeight
        val sWH = w / h.toFloat()
        val sWidthHeight = width / height.toFloat()
        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(
                    projMatrix,
                    0,
                    -sWidthHeight * sWH,
                    sWidthHeight * sWH,
                    -1f,
                    1f,
                    3f,
                    7f
                );
            } else {
                Matrix.orthoM(
                    projMatrix,
                    0,
                    -sWidthHeight / sWH,
                    sWidthHeight / sWH,
                    -1f,
                    1f,
                    3f,
                    7f
                );
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(
                    projMatrix,
                    0,
                    -1f,
                    1f,
                    -1 / sWidthHeight * sWH,
                    1 / sWidthHeight * sWH,
                    3f,
                    7f
                )
            } else {
                Matrix.orthoM(
                    projMatrix,
                    0,
                    -1f,
                    1f,
                    -sWH / sWidthHeight,
                    sWH / sWidthHeight,
                    3f,
                    7f
                )
            }
        }
    }

    override fun onDrawFrame(p0: GL10?) {
        // Combine the projection and camera view matrices
        Matrix.multiplyMM(vPMatrix, 0, projMatrix, 0, vMatrix, 0)
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, vPMatrix, 0)

        drawFrame()
    }
}