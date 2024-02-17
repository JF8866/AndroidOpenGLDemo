package site.feiyuliuxing.ch4_5

import android.opengl.GLES30
import android.opengl.Matrix
import site.feiyuliuxing.gl_comm.BaseGLRenderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * 参考《计算机图形学编程·使用OpenGL和C++（第2版）》4.5 我们的第一个3D程序
 */
class MyGLRenderer : BaseGLRenderer() {

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

    override fun vertexShaderSource(): String {
        // 代码中将位置坐标乘1/2，然后加1/2，以将取值区间从[−1, +1]转换为[0, 1]
        return """
            #version 300 es
            uniform mat4 uMVPMatrix;
            layout(location=0) in vec3 position;
            out vec4 varyingColor;
            void main(void) {
                gl_Position = uMVPMatrix * vec4(position, 1.0);
                varyingColor = vec4(position, 1.0) * 0.5 + vec4(0.5, 0.5, 0.5, 0.5);
            }
        """.trimIndent()
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

        Matrix.setIdentityM(vMatrix, 0)
        Matrix.translateM(vMatrix, 0, 0f, 0f, -8f)
//        Matrix.translateM(vMatrix, 0, 0f, -2f, 0f)

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
        Matrix.multiplyMM(vPMatrix, 0, projMatrix, 0, vMatrix, 0)
    }

    private var degrees = 0f

    override fun onDrawFrame(p0: GL10?) {
        degrees += 1f
        val radians = Math.toRadians(degrees.toDouble()).toFloat()
        Matrix.setIdentityM(mMat, 0)
        Matrix.rotateM(mMat, 0, degrees, 0f, 1f, 0f)
        Matrix.rotateM(mMat, 0, degrees, 1f, 0f, 0f)
        Matrix.rotateM(mMat, 0, degrees, 0f, 0f, 1f)
        Matrix.translateM(
            mMat, 0,
            sin(0.35f * radians) * 2f,
            cos(0.52f * radians) * 2f,
            sin(0.7f * radians) * 2f
        )
        Matrix.multiplyMM(vPMatrix, 0, projMatrix, 0, vMatrix, 0)
        Matrix.multiplyMM(vPMatrix, 0, vPMatrix, 0, mMat, 0)

        super.onDrawFrame(p0)
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0)
        GLES30.glEnableVertexAttribArray(0)

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthFunc(GLES30.GL_LEQUAL)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36)
    }
}