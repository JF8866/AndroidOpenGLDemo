package site.feiyuliuxing.ch2_1

import android.opengl.GLES30
import site.feiyuliuxing.gl_comm.BaseGLRenderer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : BaseGLRenderer() {
    private val TAG = "MyGLRenderer"

    private var xOffsetHandle = 0

    override fun vertexShaderSource(): String {
        return """
            #version 300 es
            uniform mat4 uMVPMatrix;
            uniform float offset;
            void main(void) {
                if(gl_VertexID == 0) gl_Position = vec4(0.25 + offset, -0.25, 0.0, 1.0);
                else if(gl_VertexID == 1) gl_Position = vec4(-0.25 + offset, -0.25, 0.0, 1.0);
                else if(gl_VertexID == 2) gl_Position = vec4(0.25 + offset, 0.25, 0.0, 1.0);
                gl_Position = uMVPMatrix * gl_Position;
                gl_PointSize = 30.0;
            }
        """.trimIndent()
    }

    override fun fragmentShaderSource(): String {
        return """
            #version 300 es
            precision mediump float;
            out vec4 color;
            void main(void) {
                if(gl_FragCoord.x < 540.0) {
                    color = vec4(1.0, 0.0, 0.0, 1.0);
                } else {
                    color = vec4(0.0, 0.0, 1.0, 1.0);
                }
            }
        """.trimIndent()
    }


    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        super.onSurfaceCreated(p0, p1)
        xOffsetHandle = GLES30.glGetUniformLocation(renderingProgram, "offset")
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(p0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        super.onDrawFrame(p0)
        display()
    }

    private var x = 0.0f
    private var inc = 0.01f

    private fun display() {
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        x += inc
        if (x > 1.0f) inc = -0.01f
        if (x < -1.0f) inc = 0.01f
        GLES30.glUniform1f(xOffsetHandle, x)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
    }
}