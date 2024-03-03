package site.feiyuliuxing.txt

import android.graphics.Bitmap
import android.opengl.GLES30.*
import site.feiyuliuxing.gl_comm.GLUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GLChar(bitmap: Bitmap) {
    private var positionVertex = FloatArray(15)

    private val vertexBuffer = ByteBuffer.allocateDirect(positionVertex.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(positionVertex)
        .apply{ position(0) }

    private val texVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.size * 4)
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

    var glWidth: Float = 0f
        private set
    var glHeight: Float = 0f
        private set

    init {
        textureId = GLUtil.loadTexture(bitmap)

        val cx = 0f
        val cy = 0f
        val xOffset = 0.0005f * bitmap.width
        val yOffset = 0.0005f * bitmap.height

        glWidth = xOffset * 2f
        glHeight = yOffset * 2f

        positionVertex = floatArrayOf(
            cx, cy, 0f,
            xOffset, yOffset, 0f,
            -xOffset, yOffset, 0f,
            -xOffset, -yOffset, 0f,
            xOffset, -yOffset, 0f
        )
        vertexBuffer.position(0)
        vertexBuffer.put(positionVertex)
        vertexBuffer.position(0)
    }

    fun draw(vbo: IntArray) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(0)

        glBindBuffer(GL_ARRAY_BUFFER, vbo[1])
        glBufferData(GL_ARRAY_BUFFER, texVertexBuffer.capacity() * 4, texVertexBuffer, GL_STATIC_DRAW)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(1)
        //激活纹理
        glActiveTexture(GL_TEXTURE0)
        //绑定纹理
        glBindTexture(GL_TEXTURE_2D, textureId)
        // 绘制
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[2])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, VERTEX_INDEX.size * 2, vertexIndexBuffer, GL_STATIC_DRAW)
        glDrawElements(GL_TRIANGLES, VERTEX_INDEX.size, GL_UNSIGNED_SHORT, 0)
    }

    companion object {
        private const val TAG = "GLChar"

        /**
         * 绘制顺序索引
         */
        private val VERTEX_INDEX = shortArrayOf(
            0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
            0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
            0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
            0, 4, 1   //V0,V4,V1 三个顶点组成一个三角形
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
    }
}