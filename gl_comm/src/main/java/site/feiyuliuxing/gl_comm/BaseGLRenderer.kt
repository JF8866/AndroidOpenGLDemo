package site.feiyuliuxing.gl_comm

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class BaseGLRenderer : GLSurfaceView.Renderer {
    //(相机)视图矩阵
    open val vMatrix = FloatArray(16)

    //投影矩阵
    open val projMatrix = FloatArray(16)

    //用于接收前面两个矩阵相乘后的结果
    open val vPMatrix = FloatArray(16)
    private var muMVPMatrixHandle = 0

    open var renderingProgram = 0

    private val numVAOs = 1
    private val numVBOs = 2
    open val vao = IntArray(numVAOs)
    open val vbo = IntArray(numVBOs)

    abstract fun vertexShaderSource(): String
    abstract fun fragmentShaderSource(): String

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        renderingProgram = GLUtil.createShaderProgram(vertexShaderSource(), fragmentShaderSource())
        GLES30.glUseProgram(renderingProgram)
        muMVPMatrixHandle = GLES30.glGetUniformLocation(renderingProgram, "uMVPMatrix")

        GLES30.glGenVertexArrays(numVAOs, vao, 0)
        GLES30.glBindVertexArray(vao[0])
        GLES30.glGenBuffers(numVBOs, vbo, 0)

        // 第5个参数 eyeZ 为正值，视点在屏幕前方，负值则在屏幕后方，其符号会影响X轴方向
        // 其绝对值影响绘制元素的大小，绝对值越大，视点离屏幕越远，物体越小
        // 倒数第2个参数 upY 的符号是影响Y轴坐标是否反转的，如果发现图片上下或者左右翻转了，就要调整这俩参数了
        // Create a camera view matrix
        Matrix.setLookAtM(
            vMatrix, 0, 0f, 0f, 7f,
            0f, 0f, 0f, 0f, 1f, 0.0f
        )
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        // create a projection matrix from device screen geometry
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        // Combine the projection and camera view matrices
        Matrix.multiplyMM(vPMatrix, 0, projMatrix, 0, vMatrix, 0)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES30.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, vPMatrix, 0)
    }
}