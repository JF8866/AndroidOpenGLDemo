package site.feiyuliuxing.ch4_6

import android.content.Context
import android.opengl.GLES30
import android.opengl.Matrix
import site.feiyuliuxing.gl_comm.BaseGLRenderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 参考《计算机图形学编程·使用OpenGL和C++（第2版）》4.6 渲染一个对象的多个副本
 */
class MyGLRenderer(private val context: Context) : BaseGLRenderer() {

    private val vertexPositions = floatArrayOf(
        -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f,
        -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f
    )
    private val vertexBuffer = ByteBuffer.allocateDirect(vertexPositions.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(vertexPositions)
        .apply { position(0) }

    private val mMat = FloatArray(16)

    private var tfLoc = 0
    private var vMatLoc = 0
    private var pMatLoc = 0

    override fun vertexShaderSource(): String {
        context.assets.open("vertexSource.glsl").use {
            return String(it.readBytes())
        }
    }

    override fun fragmentShaderSource(): String {
        return """
            #version 300 es
            precision mediump float;
            in vec4 varyingColor;
            out vec4 color;
            void main(void) {
                color = varyingColor;
            }
        """.trimIndent()
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        super.onSurfaceCreated(p0, p1)

        tfLoc = GLES30.glGetUniformLocation(renderingProgram, "tf")
        vMatLoc = GLES30.glGetUniformLocation(renderingProgram, "v_matrix")
        pMatLoc = GLES30.glGetUniformLocation(renderingProgram, "proj_matrix")

        Matrix.setIdentityM(vMatrix, 0)
        Matrix.translateM(vMatrix, 0, 0f, 0f, -220f)
//        Matrix.translateM(vMatrix, 0, 0f, -2f, 0f)
        GLES30.glUniformMatrix4fv(vMatLoc, 1, false, vMatrix, 0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vertexPositions.size * 4,
            vertexBuffer,
            GLES30.GL_STATIC_DRAW
        )
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val aspect: Float = width.toFloat() / height.toFloat()
        //投影矩阵分【透视投影矩阵】和【正射投影矩阵】
        // fovy: field of view in y direction, in degrees
        Matrix.perspectiveM(projMatrix, 0, 60f, aspect, 0.1f, 1000f)//1.0472 radians = 60 degrees
        GLES30.glUniformMatrix4fv(pMatLoc, 1, false, projMatrix, 0)
    }

    private var degrees = 0f

    override fun onDrawFrame(p0: GL10?) {
        degrees += 1f
        val radians = Math.toRadians(degrees.toDouble()).toFloat()
        GLES30.glUniform1f(tfLoc, radians)

//        val tf = System.currentTimeMillis() / 10 % 360
//        GLES30.glUniform1f(tfLoc, Math.toRadians(tf.toDouble()).toFloat())

        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0)
        GLES30.glEnableVertexAttribArray(0)

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthFunc(GLES30.GL_LEQUAL)
        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, 36, 10_0000)
    }
}