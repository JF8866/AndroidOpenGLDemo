package site.feiyuliuxing.opengltest.shapes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder


class GLText {

    companion object {
        private const val TAG = "GLText"

        /**
         * 绘制顺序索引
         */
        private val VERTEX_INDEX = shortArrayOf(
            0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
            0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
            0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
            0, 4, 1   //V0,V4,V1 三个顶点组成一个三角形
        )
    }

    /**
     * 顶点坐标 RoundRectangle(-0.5f, 1.7f, 0.5f, 0.7f, 0.1f)
     * (x,y,z)
     */
    private val POSITION_VERTEX = floatArrayOf(
        0f, -1.2f, 0f,     //顶点坐标V0
        0.8f, -0.8f, 0f,     //顶点坐标V1
        -0.8f, -0.8f, 0f,    //顶点坐标V2
        -0.8f, -1.6f, 0f,   //顶点坐标V3
        0.8f, -1.6f, 0f     //顶点坐标V4
    )

    /**
     * 纹理坐标
     * (s,t)
     */
    private val TEX_VERTEX = floatArrayOf(
        0.5f, 0.5f, //纹理坐标V0
        1f, 0f,     //纹理坐标V1
        0f, 0f,     //纹理坐标V2
        0f, 1.0f,   //纹理坐标V3
        1f, 1.0f    //纹理坐标V4
    )

    private val vertexBuffer = ByteBuffer.allocateDirect(POSITION_VERTEX.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(POSITION_VERTEX)
        .position(0)

    private val textureVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(TEX_VERTEX)
        .position(0)

    private val vertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.size * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .put(VERTEX_INDEX)
        .position(0)

    private var textureId = 0
    var width = 0
        private set
    var height = 0
        private set

    fun loadTexture(text: String, color: Int) {
        val textureIds = intArrayOf(0)
        //创建一个纹理对象
        GLES20.glGenTextures(1, textureIds, 0)
        if (textureIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL textureId object.")
            return
        }

        val bitmap = Bitmap.createBitmap(256, 128, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)

        // Draw the text
        val textPaint = Paint()
        textPaint.textSize = 32f
        textPaint.isAntiAlias = true
        textPaint.color = color
        canvas.drawText(text, 16f, 64f, textPaint)

        width = bitmap.width
        height = bitmap.height
        //绑定纹理到OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        //设置默认的纹理过滤参数
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR_MIPMAP_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        //加载bitmap到纹理中
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        //生成MIP贴图
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        //数据如果已经被加载进OpenGL,则可以回收该bitmap
        bitmap.recycle()
        //取消绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        textureId = textureIds[0]
    }

    fun draw(positionHandle: Int, textureCoordHandle: Int) {
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glVertexAttribPointer(
            textureCoordHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            textureVertexBuffer
        )
        //激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        // 绘制
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            VERTEX_INDEX.size,
            GLES20.GL_UNSIGNED_SHORT,
            vertexIndexBuffer
        )
    }
}