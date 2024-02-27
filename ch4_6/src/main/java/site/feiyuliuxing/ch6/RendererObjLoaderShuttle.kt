package site.feiyuliuxing.ch6

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

/**
 * 加载obj模型
 */
class RendererObjLoaderShuttle(private val context: Context) : GLSurfaceView.Renderer,
    IShaderProvider {
    private val numVAOs = 1
    private val numVBOs = 3

    private val vao = IntArray(numVAOs)
    private val vbo = IntArray(numVBOs)

    private var cameraX = 0f
    private var cameraY = 0f
    private var cameraZ = 2.6f

    private var objLocX = 0f
    private var objLocY = 0f
    private var objLocZ = 0f

    private var renderingProgram = 0
    private var shuttleTexture = 0
    private var mvLoc = 0
    private var projLoc = 0

    private val pMat = FloatArray(16)
    private val vMat = FloatArray(16)
    private val mMat = FloatArray(16)
    private val mvMat = FloatArray(16)

    private val myModel = ImportedModel(context.assets.open("shuttle.obj"))

    private fun setupVertices() {
        val vert = myModel.getVertices()
        val tex = myModel.getTextureCoords()
        val norm = myModel.getNormals()

        val pvalues = mutableListOf<Float>()
        val tvalues = mutableListOf<Float>()
        val nvalues = mutableListOf<Float>()

        for (i in 0 until myModel.getNumVertices()) {
            pvalues.add((vert[i]).x)
            pvalues.add((vert[i]).y)
            pvalues.add((vert[i]).z)
            tvalues.add((tex[i]).s)
            tvalues.add((tex[i]).t)
            nvalues.add((norm[i]).x)
            nvalues.add((norm[i]).y)
            nvalues.add((norm[i]).z)
        }

        glGenVertexArrays(1, vao, 0)
        glBindVertexArray(vao[0])
        glGenBuffers(numVBOs, vbo, 0)

        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(GL_ARRAY_BUFFER, pvalues.size * 4, pvalues.toBuffer(), GL_STATIC_DRAW)

        glBindBuffer(GL_ARRAY_BUFFER, vbo[1])
        glBufferData(GL_ARRAY_BUFFER, tvalues.size * 4, tvalues.toBuffer(), GL_STATIC_DRAW)

        glBindBuffer(GL_ARRAY_BUFFER, vbo[2])
        glBufferData(GL_ARRAY_BUFFER, nvalues.size * 4, nvalues.toBuffer(), GL_STATIC_DRAW)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        renderingProgram = GLUtil.createShaderProgram(vertexShaderSource(), fragmentShaderSource())
        mvLoc = glGetUniformLocation(renderingProgram, "mv_matrix")
        projLoc = glGetUniformLocation(renderingProgram, "proj_matrix")
        shuttleTexture = context.loadTexture(R.mipmap.spstob_1)
        setupVertices()
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(pMat, 0, 60f, aspect, 0.1f, 1000f)
    }

    override fun onDrawFrame(p0: GL10?) {
        glClear(GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        glUseProgram(renderingProgram)

        Matrix.setIdentityM(vMat, 0)
        Matrix.translateM(vMat, 0, -cameraX, -cameraY, -cameraZ)
        Matrix.setIdentityM(mMat, 0)
        Matrix.translateM(mMat, 0, objLocX, objLocY, objLocZ)
        Matrix.rotateM(mMat, 0, 0.0f, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(mMat, 0, 135.0f, 0.0f, 1.0f, 0.0f)
        Matrix.rotateM(mMat, 0, 35.0f, 0.0f, 0.0f, 1.0f)
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
        glBindTexture(GL_TEXTURE_2D, shuttleTexture)

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
        glDrawArrays(GL_TRIANGLES, 0, myModel.getNumVertices())
    }

    override fun vertexShaderSource(): String {
        return """
            #version 300 es

            layout (location = 0) in vec3 position;
            layout (location = 1) in vec2 tex_coord;
            out vec2 tc;

            uniform mat4 mv_matrix;
            uniform mat4 proj_matrix;
            uniform sampler2D s;

            void main(void)
            {	gl_Position = proj_matrix * mv_matrix * vec4(position,1.0);
            	tc = tex_coord;
            }
        """.trimIndent()
    }

    override fun fragmentShaderSource(): String {
        return """
            #version 300 es
            precision mediump float;
            in vec2 tc;
            out vec4 color;

            uniform sampler2D s;

            void main(void)
            {
                //OpenGL纹理坐标默认左下角是(0, 0)，图片默认左上角是(0, 0)，教程上使
                //用 SOIL_FLAG_INVERT_Y 来翻转Y坐标，这里使用着色器代码来翻转Y坐标
                vec2 flipped_tc = vec2(tc.s, 1.0 - tc.t);
            	color = texture(s,flipped_tc);
            }
        """.trimIndent()
    }
}