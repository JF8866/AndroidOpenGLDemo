package site.feiyuliuxing.txt

import android.content.Context
import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import site.feiyuliuxing.gl_comm.GLUtil
import site.feiyuliuxing.gl_comm.IShaderProvider
import site.feiyuliuxing.jfreetype.FTBitmap
import site.feiyuliuxing.jfreetype.JFreeType
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class RendererText(private val context: Context) : GLSurfaceView.Renderer, IShaderProvider {
    private val numVAOs = 1
    private val numVBOs = 3

    private val vao = IntArray(numVAOs)
    private val vbo = IntArray(numVBOs)

    private var cameraX = 0f
    private var cameraY = 0f
    private var cameraZ = 2.5f

    private var renderingProgram = 0
    private var mvLoc = 0
    private var projLoc = 0

    private val pMat = FloatArray(16)
    private val vMat = FloatArray(16)
    private val mMat = FloatArray(16)
    private val mvMat = FloatArray(16)

    private val glChars = mutableMapOf<Char, GLChar>()
    private var glText = GLText("", glChars)

    private fun loadGLChars() {
        val ft = JFreeType()
        val faceBuffer = context.assets.open("fonts/SourceCodePro-Regular.ttf").use {
            ByteBuffer.allocateDirect(it.available())
                .put(it.readBytes()).apply { position(0) }
        }
        ft.init(faceBuffer)

        val chars = mutableListOf<Char>()
        fun putChar(char: Char) {
            chars.add(char)
        }

        fun putChars(range: IntRange) {
            for (charcode in range) putChar(charcode.toChar())
        }
        putChars('A'.code..'Z'.code)
        putChars('a'.code..'z'.code)
        putChars('0'.code..'9'.code)
        putChar('!')

        val ftBitmaps = chars.map {
            val ftBitmap = FTBitmap()
            ft.charBitmap(ftBitmap, it)
            ftBitmap
        }

        var maxAscent = 0
        var maxDescent = 0
        for (ftBmp in ftBitmaps) {
            if (ftBmp.bitmapTop > maxAscent) maxAscent = ftBmp.bitmapTop
            if (ftBmp.rows - ftBmp.bitmapTop > maxDescent) maxDescent = ftBmp.rows - ftBmp.bitmapTop
        }

        for (i in chars.indices) {
            ftBitmaps[i].toBitmap(maxAscent, maxDescent)?.let { bitmap ->
                glChars[chars[i]] = GLChar(bitmap)
            }
        }

        ft.close()

        glText = GLText("Hello World!", glChars)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        renderingProgram = GLUtil.createShaderProgram(vertexShaderSource(), fragmentShaderSource())
        glUseProgram(renderingProgram)
        mvLoc = glGetUniformLocation(renderingProgram, "mv_matrix")
        projLoc = glGetUniformLocation(renderingProgram, "proj_matrix")
        glGenVertexArrays(1, vao, 0)
        glBindVertexArray(vao[0])
        glGenBuffers(numVBOs, vbo, 0)
        loadGLChars()
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(pMat, 0, Math.toDegrees(1.0472).toFloat(), aspect, 0.1f, 1000f)
    }

    override fun onDrawFrame(p0: GL10?) {
        glClearColor(0f, 0f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)
        //下面两行代码，防止图片的透明部分被显示成黑色
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        Matrix.setIdentityM(vMat, 0)
        Matrix.translateM(vMat, 0, -cameraX, -cameraY, -cameraZ)
        Matrix.setIdentityM(mMat, 0)
        Matrix.multiplyMM(mvMat, 0, vMat, 0, mMat, 0)

        glUniformMatrix4fv(mvLoc, 1, false, mvMat, 0)
        glUniformMatrix4fv(projLoc, 1, false, pMat, 0)

        glText.draw(vbo) { xOffset, yOffset ->
            Matrix.setIdentityM(mMat, 0)
            Matrix.translateM(mMat, 0, xOffset, yOffset, 0f)
            Matrix.multiplyMM(mvMat, 0, vMat, 0, mMat, 0)
            glUniformMatrix4fv(mvLoc, 1, false, mvMat, 0)
        }
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
            {
            	gl_Position = proj_matrix * mv_matrix * vec4(position, 1.0);
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