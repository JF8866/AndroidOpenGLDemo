package site.feiyuliuxing.ch4_6

import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import site.feiyuliuxing.gl_comm.GLUtil
import site.feiyuliuxing.gl_comm.ext.FloatArrayExt.toBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 4.7 渲在同一个场景中染多个不同的模型
 */
class RendererMultipleModels : GLSurfaceView.Renderer {
    private val numVAOs = 1
    private val numVBOs = 2

    private var cameraX = 0f
    private var cameraY = 0f
    private var cameraZ = 8f

    //立方体
    private var cubeLocX = 0f
    private var cubeLocY = -2f
    private var cubeLocZ = 0f

    //四棱锥
    private var pyrLocX = 0f
    private var pyrLocY = 2f
    private var pyrLocZ = 0f

    private var renderingProgram = 0

    private val vao = IntArray(numVAOs)
    private val vbo = IntArray(numVBOs)

    private var mvLoc = 0
    private var projLoc = 0

    private val pMat = FloatArray(16)
    private val vMat = FloatArray(16)
    private val mMat = FloatArray(16)
    private val mvMat = FloatArray(16)

    private fun setupVertices() {
        //立方体
        val vertexPositions = floatArrayOf(
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
        //四棱锥
        val pyramidPositions = floatArrayOf(
            -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,    //front
            1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right
            1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  //back
            -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left
            -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
            1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
        )

        glGenVertexArrays(1, vao, 0)
        glBindVertexArray(vao[0])
        glGenBuffers(numVBOs, vbo, 0)

        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(GL_ARRAY_BUFFER, vertexPositions.size * 4, vertexPositions.toBuffer(), GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, vbo[1])
        glBufferData(GL_ARRAY_BUFFER, pyramidPositions.size * 4, pyramidPositions.toBuffer(), GL_STATIC_DRAW)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        val vertexShaderSource = """
            #version 300 es

            layout (location=0) in vec3 position;

            uniform mat4 mv_matrix;
            uniform mat4 proj_matrix;

            out vec4 varyingColor;

            void main(void)
            {
            	gl_Position = proj_matrix * mv_matrix * vec4(position,1.0);
            	varyingColor = vec4(position,1.0)*0.5 + vec4(0.5, 0.5, 0.5, 0.5);
            }
        """.trimIndent()
        val fragmentShaderSource = """
            #version 300 es
            precision mediump float;
            in vec4 varyingColor;
            out vec4 color;

            void main(void)
            {	color = varyingColor;
            }
        """.trimIndent()
        renderingProgram = GLUtil.createShaderProgram(vertexShaderSource, fragmentShaderSource)
        mvLoc = glGetUniformLocation(renderingProgram, "mv_matrix");
        projLoc = glGetUniformLocation(renderingProgram, "proj_matrix")
        setupVertices()
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(pMat, 0, 60f, aspect, 0.1f, 1000f)
    }

    override fun onDrawFrame(p0: GL10?) {
        glClear(GL_DEPTH_BUFFER_BIT)

        glUseProgram(renderingProgram)

        Matrix.setIdentityM(vMat, 0)
        Matrix.translateM(vMat, 0, -cameraX, -cameraY, -cameraZ)

        // draw the cube using buffer #0
        Matrix.setIdentityM(mMat, 0)
        Matrix.translateM(mMat, 0, cubeLocX, cubeLocY, cubeLocZ)
        Matrix.multiplyMM(mvMat, 0, vMat, 0, mMat, 0)

        glUniformMatrix4fv(mvLoc, 1, false, mvMat, 0)
        glUniformMatrix4fv(projLoc, 1, false, pMat, 0)

        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(0)

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)

        glDrawArrays(GL_TRIANGLES, 0, 36)

        // draw the pyramid using buffer #1
        Matrix.setIdentityM(mMat, 0)
        Matrix.translateM(mMat, 0, pyrLocX, pyrLocY, pyrLocZ)
        Matrix.multiplyMM(mvMat, 0, vMat, 0, mMat, 0)

        glUniformMatrix4fv(mvLoc, 1, false, mvMat, 0)
        glUniformMatrix4fv(projLoc, 1, false, pMat, 0)

        glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(0)

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)

        glDrawArrays(GL_TRIANGLES, 0, 18)
    }
}