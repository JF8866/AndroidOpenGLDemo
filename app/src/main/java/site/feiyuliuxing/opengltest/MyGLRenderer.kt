package site.feiyuliuxing.opengltest

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import site.feiyuliuxing.opengltest.shapes.Circular
import site.feiyuliuxing.opengltest.shapes.GLBitmap
import site.feiyuliuxing.opengltest.shapes.GLText
import site.feiyuliuxing.opengltest.shapes.Rectangle
import site.feiyuliuxing.opengltest.shapes.RoundRectangle
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

private const val TAG = "MyGLRenderer"

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    //todo OpenGL是在正方形区域绘制图像，手机屏幕上显示就会拉伸，要解决此问题，
    // 可以应用OpenGL投影模式和相机视图来变换坐标，以便图形对象在任何显示器上都具有正确的比例。
    // https://developer.android.com/develop/ui/views/graphics/opengl/about-opengl?hl=en#proj-es2

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform mat4 translateMatrix;
        uniform bool isTranslate;
        attribute vec4 vPosition;
        attribute vec2 aTextureCoord;
        varying vec2 vTextureCoord;
        void main() {
            if(isTranslate){
                gl_Position = uMVPMatrix * translateMatrix * vPosition;
            } else {
                gl_Position = uMVPMatrix * vPosition;
            }
            vTextureCoord = aTextureCoord;
        }
    """.trimIndent()

    // gl_FragColor = vColor;
    private val fragmentShaderSource = """
        precision mediump float;
        uniform bool isTexture;
        uniform vec4 vColor;
        uniform sampler2D uTextureUnit;
        varying vec2 vTextureCoord;
        varying vec4 vFragColor;
        void main(void) {
            if(isTexture) {
                gl_FragColor = texture2D(uTextureUnit,vTextureCoord);
            } else {
                gl_FragColor = vColor;
            }
        }
    """.trimIndent()


    private var muMVPMatrixHandle = 0
    private var programHandle = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var isTextureHandle = 0
    private var textureCoordHandle = 0
    private var translateMatrixHandle = 0
    private var isTranslateHandle = 0

    //(相机)视图矩阵
    private val vMatrix = FloatArray(16)
    private val vPMatrix = FloatArray(16)

    //投影矩阵
    private val projMatrix = FloatArray(16)
    private val translateMatrix = FloatArray(16)

    private val colors: Array<FloatArray>
    private val rect = Rectangle(-0.8f, 1.8f, 0.8f, 0.6f)
    private val roundRect = RoundRectangle(-0.5f, 1.7f, 0.5f, 0.7f, 0.1f)
    private val circular = Circular(0f, 1.2f, 0.4f)
    private val glBitmap = GLBitmap()
    private val glText = GLText()

    private fun colorToFloatArray(colorId: Int): FloatArray {
        val color = context.resources.getColor(colorId)
        return floatArrayOf(
            color.shr(16).and(0xff) / 255f,
            color.shr(8).and(0xff) / 255f,
            color.and(0xff) / 255f,
        )
    }

    init {
        colors = intArrayOf(
            R.color.solid_1,
            R.color.solid_2,
            R.color.solid_3,
            R.color.border
        ).map { colorToFloatArray(it) }.toTypedArray()
    }

    //todo  GLES20 API
    // https://developer.android.com/reference/android/opengl/GLES20
    private fun compileShaders(): Int {
        val status = intArrayOf(0)

        println(vertexShaderCode)
        println(fragmentShaderSource)

        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertexShader, vertexShaderCode)
        GLES20.glCompileShader(vertexShader)
        GLES20.glGetShaderiv(vertexShader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e(TAG, "顶点着色器编译失败：" + GLES20.glGetShaderInfoLog(vertexShader))
        }

        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShader, fragmentShaderSource)
        GLES20.glCompileShader(fragmentShader)
        GLES20.glGetShaderiv(fragmentShader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e(TAG, "片段着色器编译失败：" + GLES20.glGetShaderInfoLog(fragmentShader))
        }

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e(TAG, "链接源程序失败：" + GLES20.glGetProgramInfoLog(program))
        }

        // Delete the shaders as the program has them now
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)

        return program
    }

    private fun drawFrame() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //下面两行代码，防止图片的透明部分被显示成黑色
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(textureCoordHandle)//启用纹理坐标属性

        //画几何图形
        GLES20.glUniform1i(isTextureHandle, 0)
        GLES20.glUniform1i(isTranslateHandle, 0)
        rect.draw(positionHandle, colorHandle, colors[0][0], colors[0][1], colors[0][2])
        rect.drawBorder(positionHandle, colorHandle, colors[3][0], colors[3][1], colors[3][2], 9f)
        roundRect.draw(positionHandle, colorHandle, colors[1][0], colors[1][1], colors[1][2])
        roundRect.drawBorder(
            positionHandle,
            colorHandle,
            colors[3][0],
            colors[3][1],
            colors[3][2],
            9f
        )
        circular.draw(positionHandle, colorHandle, colors[2][0], colors[2][1], colors[2][2])
        circular.drawBorder(
            positionHandle,
            colorHandle,
            colors[3][0],
            colors[3][1],
            colors[3][2],
            9f
        )

        //画bitmap
        GLES20.glUniform1i(isTextureHandle, 1)
        glBitmap.draw(positionHandle, textureCoordHandle)

        GLES20.glUniform1i(isTranslateHandle, 1)
        GLES20.glUniformMatrix4fv(translateMatrixHandle, 1, false, translateMatrix, 0)
        glText.draw(positionHandle, textureCoordHandle)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureCoordHandle)
    }

    private fun updateTextLocation() {
        val degrees = (System.currentTimeMillis() / 10 % 360).toDouble()
        val radians = Math.toRadians(degrees).toFloat()
        Matrix.setIdentityM(translateMatrix, 0)
        Matrix.translateM(translateMatrix, 0, cos(radians), sin(radians) / 2f, 0f)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        println("onSurfaceCreated")
        programHandle = compileShaders()
        glBitmap.loadTexture(context, R.mipmap.gougou)
        glText.loadTexture("Hello World!", Color.RED)
        GLES20.glUseProgram(programHandle)
        muMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix")
        positionHandle = GLES20.glGetAttribLocation(programHandle, "vPosition")
        textureCoordHandle = GLES20.glGetAttribLocation(programHandle, "aTextureCoord")
        colorHandle = GLES20.glGetUniformLocation(programHandle, "vColor")
        isTextureHandle = GLES20.glGetUniformLocation(programHandle, "isTexture")
        translateMatrixHandle = GLES20.glGetUniformLocation(programHandle, "translateMatrix")
        isTranslateHandle = GLES20.glGetUniformLocation(programHandle, "isTranslate")

        // 第5个参数 eyeZ 为正值，视点在屏幕前方，负值则在屏幕后方，其符号会影响X轴方向
        // 其绝对值影响绘制元素的大小，绝对值越大，视点离屏幕越远，物体越小
        // 倒数第2个参数 upY 的符号是影响Y轴坐标是否反转的，如果发现图片上下或者左右翻转了，就要调整这俩参数了
        // Create a camera view matrix
        Matrix.setLookAtM(
            vMatrix, 0, 0f, 0f, 6f,
            0f, 0f, 0f, 0f, 1.0f, 0.0f
        )
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        println("onSurfaceChanged() - ${width}x${height}")

        val ratio: Float = width.toFloat() / height.toFloat()
        // create a projection matrix from device screen geometry
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(p0: GL10?) {
        // Combine the projection and camera view matrices
        Matrix.multiplyMM(vPMatrix, 0, projMatrix, 0, vMatrix, 0)
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, vPMatrix, 0)

        updateTextLocation()
        drawFrame()
    }
}
