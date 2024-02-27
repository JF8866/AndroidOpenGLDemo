package site.feiyuliuxing.ch6

import android.content.Context
import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import site.feiyuliuxing.ch4_6.R
import site.feiyuliuxing.gl_comm.GLUtil
import site.feiyuliuxing.gl_comm.GLUtil.loadTexture
import site.feiyuliuxing.gl_comm.IShaderProvider
import site.feiyuliuxing.gl_comm.Vec2
import site.feiyuliuxing.gl_comm.Vec3
import site.feiyuliuxing.gl_comm.ext.FloatArrayExt.toBuffer
import java.lang.Math.toRadians
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

class Sphere @JvmOverloads constructor(prec: Int = 48) {
    val numVertices = (prec + 1) * (prec + 1)
    val numIndices = prec * prec * 6
    val indices = mutableListOf<Int>()
    val vertices = mutableListOf<Vec3>()
    val texCoords = mutableListOf<Vec2>()
    val normals = mutableListOf<Vec3>()
    val tangents = mutableListOf<Vec3>()//切向量没用到

    init {
        for (i in 0 until numVertices) vertices.add(Vec3())
        for (i in 0 until numVertices) texCoords.add(Vec2())
        for (i in 0 until numVertices) normals.add(Vec3())
        for (i in 0 until numVertices) tangents.add(Vec3())
        for (i in 0 until numIndices) indices.add(0)

        // calculate triangle vertices
        for (i in 0..prec) {
            for (j in 0..prec) {
                val y = cos(toRadians(180.0 - i * 180.0 / prec)).toFloat()
                val x = -(cos(toRadians(j * 360.0 / prec)) * abs(cos(asin(y)))).toFloat()
                val z = (sin(toRadians(j * 360.0 / prec)) * abs(cos(asin(y)))).toFloat()
                vertices[i * (prec + 1) + j] = Vec3(x, y, z)
                texCoords[i * (prec + 1) + j] = Vec2(j.toFloat() / prec, i.toFloat() / prec)
                normals[i * (prec + 1) + j] = Vec3(x, y, z)

                // calculate tangent vector
                if (((x == 0f) && (y == 1f) && (z == 0f)) || ((x == 0f) && (y == -1f) && (z == 0f))) {
                    tangents[i * (prec + 1) + j] = Vec3(0.0f, 0.0f, -1.0f)
                } else {
                    tangents[i * (prec + 1) + j] = Vec3(0.0f, 1.0f, 0.0f).cross(Vec3(x, y, z))
                }
            }
        }
        // calculate triangle indices
        for (i in 0 until prec) {
            for (j in 0 until prec) {
                indices[6 * (i * prec + j) + 0] = i * (prec + 1) + j
                indices[6 * (i * prec + j) + 1] = i * (prec + 1) + j + 1
                indices[6 * (i * prec + j) + 2] = (i + 1) * (prec + 1) + j
                indices[6 * (i * prec + j) + 3] = i * (prec + 1) + j + 1
                indices[6 * (i * prec + j) + 4] = (i + 1) * (prec + 1) + j + 1
                indices[6 * (i * prec + j) + 5] = (i + 1) * (prec + 1) + j
            }
        }
    }
}

class RendererSphere(private val context: Context) : GLSurfaceView.Renderer, IShaderProvider {
    private val numVAOs = 1
    private val numVBOs = 3
    private val vao = IntArray(numVAOs)
    private val vbo = IntArray(numVBOs)

    private var cameraX = 0f
    private var cameraY = 0f
    private var cameraZ = 4f

    private var sphLocX = 0f
    private var sphLocY = 0f
    private var sphLocZ = -1f

    private var renderingProgram = 0
    private var earthTexture = 0
    private var mvLoc = 0
    private var projLoc = 0

    private val pMat = FloatArray(16)
    private val vMat = FloatArray(16)
    private val mMat = FloatArray(16)
    private val mvMat = FloatArray(16)

    private val mySphere = Sphere(48)

    private fun setupVertices() {
        val ind = mySphere.indices
        val vert = mySphere.vertices
        val tex = mySphere.texCoords
        val norm = mySphere.normals

        val pvalues = mutableListOf<Float>()
        val tvalues = mutableListOf<Float>()
        val nvalues = mutableListOf<Float>()

        val numIndices = mySphere.numIndices
        for (i in 0 until numIndices) {
            pvalues.add((vert[ind[i]]).x)
            pvalues.add((vert[ind[i]]).y)
            pvalues.add((vert[ind[i]]).z)
            tvalues.add((tex[ind[i]]).s)
            tvalues.add((tex[ind[i]]).t)
            nvalues.add((norm[ind[i]]).x)
            nvalues.add((norm[ind[i]]).y)
            nvalues.add((norm[ind[i]]).z)
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
        setupVertices()
        earthTexture = context.loadTexture(R.mipmap.brick1)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(pMat, 0, 60f, aspect, 0.1f, 1000.0f)
    }

    override fun onDrawFrame(p0: GL10?) {
        glClear(GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        glUseProgram(renderingProgram);

        mvLoc = glGetUniformLocation(renderingProgram, "mv_matrix")
        projLoc = glGetUniformLocation(renderingProgram, "proj_matrix")

        Matrix.setIdentityM(vMat, 0)
        Matrix.translateM(vMat, 0, -cameraX, -cameraY, -cameraZ)

        Matrix.setIdentityM(mMat, 0)
        Matrix.translateM(mMat, 0, sphLocX, sphLocY, sphLocZ)
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
        glBindTexture(GL_TEXTURE_2D, earthTexture)

        glEnable(GL_CULL_FACE)
        glFrontFace(GL_CCW)
        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
        glDrawArrays(GL_TRIANGLES, 0, mySphere.numIndices)
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
            {	color = texture(s,tc);
            }
        """.trimIndent()
    }
}