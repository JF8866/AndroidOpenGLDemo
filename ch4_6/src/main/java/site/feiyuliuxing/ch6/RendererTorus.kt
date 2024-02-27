package site.feiyuliuxing.ch6

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.opengl.GLES30.*
import site.feiyuliuxing.ch4_6.R
import site.feiyuliuxing.gl_comm.GLUtil
import site.feiyuliuxing.gl_comm.GLUtil.loadTexture
import site.feiyuliuxing.gl_comm.IShaderProvider
import site.feiyuliuxing.gl_comm.Vec2
import site.feiyuliuxing.gl_comm.Vec3
import site.feiyuliuxing.gl_comm.ext.FloatArrayExt.toBuffer
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Torus @JvmOverloads constructor(
    inner: Float = 0.5f,
    outer: Float = 0.2f,
    prec: Int = 48,
) {
    val numVertices = (prec + 1) * (prec + 1)
    val numIndices = prec * prec * 6
    val indices = mutableListOf<Int>()
    val vertices = mutableListOf<Vec3>()
    val texCoords = mutableListOf<Vec2>()
    val normals = mutableListOf<Vec3>()
    val sTangents = mutableListOf<Vec3>()
    val tTangents = mutableListOf<Vec3>()

    init {
        for (i in 0 until numVertices) vertices.add(Vec3())
        for (i in 0 until numVertices) texCoords.add(Vec2())
        for (i in 0 until numVertices) normals.add(Vec3())
        for (i in 0 until numVertices) sTangents.add(Vec3())
        for (i in 0 until numVertices) tTangents.add(Vec3())
        for (i in 0 until numIndices) indices.add(0)

        for (i in 0..prec) {
            val amt = toRadians(i * 360.0 / prec)

            val rMat = FloatArray(16)
            Matrix.setIdentityM(rMat, 0)
            Matrix.rotateM(rMat, 0, toDegrees(amt).toFloat(), 0.0f, 0.0f, 1.0f)

            val resultVec = FloatArray(4)
            Matrix.multiplyMV(resultVec, 0, rMat, 0, floatArrayOf(0.0f, outer, 0.0f, 1.0f), 0)
            val initPos = Vec3(resultVec[0], resultVec[1], resultVec[2])

            vertices[i] = initPos + Vec3(inner, 0.0f, 0.0f)
            texCoords[i] = Vec2(0.0f, i.toFloat() / prec.toFloat())

            Matrix.setIdentityM(rMat, 0)
            Matrix.rotateM(rMat, 0, toDegrees(amt + 3.14159 / 2.0).toFloat(), 0.0f, 0.0f, 1.0f)
            Matrix.multiplyMV(resultVec, 0, rMat, 0, floatArrayOf(0.0f, -1.0f, 0.0f, 1.0f), 0)
            tTangents[i] = Vec3(resultVec[0], resultVec[1], resultVec[2])

            sTangents[i] = Vec3(0.0f, 0.0f, -1.0f)
            normals[i] = tTangents[i].cross(sTangents[i])
        }

        // rotate the first ring about Y to get the other rings
        for (ring in 1..prec) {
            for (i in 0..prec) {
                val amt = toRadians(ring * 360.0 / prec)

                val rMat = FloatArray(16)
                Matrix.setIdentityM(rMat, 0)
                Matrix.rotateM(rMat, 0, toDegrees(amt).toFloat(), 0.0f, 1.0f, 0.0f)

                val resultVec = FloatArray(4)
                val rhsVec = floatArrayOf(vertices[i].x, vertices[i].y, vertices[i].z, 1.0f)
                Matrix.multiplyMV(resultVec, 0, rMat, 0, rhsVec, 0)
                vertices[ring * (prec + 1) + i] = Vec3(resultVec[0], resultVec[1], resultVec[2])

                texCoords[ring * (prec + 1) + i] = Vec2(ring * 2.0f / prec, texCoords[i].t)

                rhsVec[0] = sTangents[i].x
                rhsVec[1] = sTangents[i].y
                rhsVec[2] = sTangents[i].z
                Matrix.multiplyMV(resultVec, 0, rMat, 0, rhsVec, 0)
                sTangents[ring * (prec + 1) + i] = Vec3(resultVec[0], resultVec[1], resultVec[2])

                rhsVec[0] = tTangents[i].x
                rhsVec[1] = tTangents[i].y
                rhsVec[2] = tTangents[i].z
                Matrix.multiplyMV(resultVec, 0, rMat, 0, rhsVec, 0)
                tTangents[ring * (prec + 1) + i] = Vec3(resultVec[0], resultVec[1], resultVec[2])

                rhsVec[0] = normals[i].x
                rhsVec[1] = normals[i].y
                rhsVec[2] = normals[i].z
                Matrix.multiplyMV(resultVec, 0, rMat, 0, rhsVec, 0)
                normals[ring * (prec + 1) + i] = Vec3(resultVec[0], resultVec[1], resultVec[2])
            }
        }
        // calculate triangle indices
        for (ring in 0 until prec) {
            for (i in 0 until prec) {
                indices[((ring * prec + i) * 2) * 3 + 0] = ring * (prec + 1) + i
                indices[((ring * prec + i) * 2) * 3 + 1] = (ring + 1) * (prec + 1) + i
                indices[((ring * prec + i) * 2) * 3 + 2] = ring * (prec + 1) + i + 1
                indices[((ring * prec + i) * 2 + 1) * 3 + 0] = ring * (prec + 1) + i + 1
                indices[((ring * prec + i) * 2 + 1) * 3 + 1] = (ring + 1) * (prec + 1) + i
                indices[((ring * prec + i) * 2 + 1) * 3 + 2] = (ring + 1) * (prec + 1) + i + 1
            }
        }
    }
}

class RendererTorus(private val context: Context) : GLSurfaceView.Renderer, IShaderProvider {
    private val numVAOs = 1
    private val numVBOs = 4

    private val vao = IntArray(numVAOs)
    private val vbo = IntArray(numVBOs)

    private var cameraX = 0f
    private var cameraY = 0f
    private var cameraZ = 2.5f

    private var torLocX = 0f
    private var torLocY = 0f
    private var torLocZ = -0.5f

    private var renderingProgram = 0
    private var brickTexture = 0
    private var mvLoc = 0
    private var projLoc = 0

    private val pMat = FloatArray(16)
    private val vMat = FloatArray(16)
    private val mMat = FloatArray(16)
    private val mvMat = FloatArray(16)

    private val myTorus = Torus(0.5f, 0.2f, 48)

    private fun setupVertices() {
        val ind = myTorus.indices.toIntArray()
        val vert = myTorus.vertices
        val tex = myTorus.texCoords
        val norm = myTorus.normals

        val pvalues = mutableListOf<Float>()
        val tvalues = mutableListOf<Float>()
        val nvalues = mutableListOf<Float>()

        for (i in 0 until myTorus.numVertices) {
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

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ind.size * 4, IntBuffer.wrap(ind), GL_STATIC_DRAW)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        renderingProgram = GLUtil.createShaderProgram(vertexShaderSource(), fragmentShaderSource())
        glUseProgram(renderingProgram)
        mvLoc = glGetUniformLocation(renderingProgram, "mv_matrix")
        projLoc = glGetUniformLocation(renderingProgram, "proj_matrix")
        brickTexture = context.loadTexture(R.mipmap.brick1)
        setupVertices()
        glBindTexture(GL_TEXTURE_2D, brickTexture)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(pMat, 0, toDegrees(1.0472).toFloat(), aspect, 0.1f, 1000f)
    }

    override fun onDrawFrame(p0: GL10?) {
        glClear(GL_DEPTH_BUFFER_BIT)
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        Matrix.setIdentityM(vMat, 0)
        Matrix.translateM(vMat, 0, -cameraX, -cameraY, -cameraZ)
        Matrix.setIdentityM(mMat, 0)
        Matrix.translateM(mMat, 0, torLocX, torLocY, torLocZ)
        Matrix.rotateM(mMat, 0, 30f, 1.0f, 0.0f, 0.0f)
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

        glEnable(GL_CULL_FACE)
        glFrontFace(GL_CCW)
        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3])
        glDrawElements(GL_TRIANGLES, myTorus.indices.size, GL_UNSIGNED_INT, 0)
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
            	color = texture(s,tc);
            }
        """.trimIndent()
    }
}