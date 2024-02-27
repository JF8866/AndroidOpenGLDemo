package site.feiyuliuxing.ch5

import android.content.Context
import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import site.feiyuliuxing.ch4_6.R
import site.feiyuliuxing.gl_comm.GLUtil
import site.feiyuliuxing.gl_comm.GLUtil.loadTexture
import site.feiyuliuxing.gl_comm.IShaderProvider
import site.feiyuliuxing.gl_comm.ext.FloatArrayExt.toBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class RendererTexturedPyramid(private val context: Context) : GLSurfaceView.Renderer,
    IShaderProvider {
    private val numVAOs = 1
    private val numVBOs = 2

    private var cameraX = 0f
    private var cameraY = 0f
    private var cameraZ = 6f

    private var pyrLocX = 0f
    private var pyrLocY = 0f
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

    private var brickTexture = 0

    private fun setupVertices() {
        val pyramidPositions = floatArrayOf(
            -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,    //front
            1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right
            1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  //back
            -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left
            -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
            1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
        )
        val textureCoordinates = floatArrayOf(
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
        )

        glGenVertexArrays(numVAOs, vao, 0)
        glBindVertexArray(vao[0])
        glGenBuffers(numVBOs, vbo, 0)
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(GL_ARRAY_BUFFER, pyramidPositions.size * 4, pyramidPositions.toBuffer(), GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, vbo[1])
        glBufferData(GL_ARRAY_BUFFER, textureCoordinates.size * 4, textureCoordinates.toBuffer(), GL_STATIC_DRAW)
    }

    override fun vertexShaderSource(): String {
        return """
            #version 300 es

            layout (location=0) in vec3 pos;
            layout (location=1) in vec2 texCoord;
            out vec2 tc;

            uniform mat4 mv_matrix;
            uniform mat4 proj_matrix;
            uniform sampler2D samp;

            void main(void)
            {	gl_Position = proj_matrix * mv_matrix * vec4(pos,1.0);
            	tc = texCoord;
            }
        """.trimIndent()
    }

    override fun fragmentShaderSource(): String {
        return """
            #version 300 es
            precision mediump float;
            in vec2 tc;
            out vec4 color;
            
            uniform sampler2D samp;

            void main(void)
            {	color = texture(samp, tc);
            }
        """.trimIndent()
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        renderingProgram = GLUtil.createShaderProgram(vertexShaderSource(), fragmentShaderSource())
        glUseProgram(renderingProgram)
        mvLoc = glGetUniformLocation(renderingProgram, "mv_matrix")
        projLoc = glGetUniformLocation(renderingProgram, "proj_matrix")
        setupVertices()
        brickTexture = context.loadTexture(R.mipmap.brick1)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0,0,width,height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(pMat, 0, 60f, aspect, 0.1f, 1000f)
    }

    override fun onDrawFrame(p0: GL10?) {
        glClear(GL_DEPTH_BUFFER_BIT)
        glClearColor(0f,0f,0f,1f)
        glClear(GL_COLOR_BUFFER_BIT)

        Matrix.setIdentityM(vMat, 0)
        Matrix.translateM(vMat, 0, -cameraX, -cameraY, -cameraZ)

        Matrix.setIdentityM(mMat, 0)
        Matrix.translateM(mMat, 0, pyrLocX, pyrLocY, pyrLocZ)
        Matrix.rotateM(mMat, 0, Math.toDegrees(-0.45).toFloat(), 1f, 0f, 0f)
        Matrix.rotateM(mMat, 0, Math.toDegrees(0.61).toFloat(), 0f, 1f, 0f)
        Matrix.rotateM(mMat, 0, Math.toDegrees(0.0).toFloat(), 0f, 0f, 1f)

        Matrix.multiplyMM(mvMat, 0, vMat, 0, mMat, 0)
        glUniformMatrix4fv(mvLoc, 1, false, mvMat, 0)
        glUniformMatrix4fv(projLoc, 1, false, pMat, 0)

        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(0)

        glBindBuffer(GL_ARRAY_BUFFER, vbo[1])
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(1)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, brickTexture)

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
        glDrawArrays(GL_TRIANGLES, 0, 18)
    }
}