package site.feiyuliuxing.opengltest.shapes

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

//画1/4圆的每个三角形的角度跨度，90度就是15个三角形
private const val DEGREE_SPAN = 6

/**
 * 扇形的角度跨度
 */
private const val DEGREE_SPAN_OF_SECTOR = 90

/**
 * 每个扇形的顶点数
 */
private const val VERTEX_COUNT = DEGREE_SPAN_OF_SECTOR / DEGREE_SPAN + 2

class RoundRectangle(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radius: Float
) {
    private val indices = shortArrayOf(
        0, 1, 2, 0, 2, 3, //中间矩形
        4, 5, 1, 4, 1, 0,//左侧矩形
        1, 6, 7, 1, 7, 2,//底部矩形
        3, 2, 8, 3, 8, 9,//右侧矩形
        11, 0, 3, 11, 3, 10,//顶部矩形
    )
    private val indicesBuffer = ByteBuffer.allocateDirect(indices.size * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .put(indices)
        .position(0)

    private val borderIndices = shortArrayOf(
        4, 5,//左侧边线
        6, 7,//底部边线
        8, 9,//右侧边线
        10, 11,//顶部边线
    )
    private val borderIndicesBuffer = ByteBuffer.allocateDirect(borderIndices.size * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .put(borderIndices)
        .position(0)

    //12个顶点，24个Float，即96字节
    private val vertexBuffer = ByteBuffer.allocateDirect(96)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    /*
    * VERTEX_COUNT * 2 * 4
    * 2 是指每个顶点两个Float（X/Y坐标）
    * 4 是指每个Float占4字节
    * */

    //右上角1/4圆
    private val vertexBufferRT = ByteBuffer.allocateDirect(VERTEX_COUNT * 2 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    //左上角1/4圆
    private val vertexBufferLT = ByteBuffer.allocateDirect(VERTEX_COUNT * 2 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    //左下角1/4圆
    private val vertexBufferLB = ByteBuffer.allocateDirect(VERTEX_COUNT * 2 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    //右下角1/4圆
    private val vertexBufferRB = ByteBuffer.allocateDirect(VERTEX_COUNT * 2 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    init {
        update(left, top, right, bottom, radius)
    }

    /**
     * 画1/4圆用的顶点数据
     */
    private fun sector(
        buffer: FloatBuffer,
        cx: Float,
        cy: Float,
        radius: Float,
        startDegree: Float
    ) {
        buffer.apply {
            position(0)
            buffer.put(cx)
            buffer.put(cy)
            var i = startDegree.toDouble()
            var radians: Double
            while (i <= startDegree + DEGREE_SPAN_OF_SECTOR) {
                radians = Math.toRadians(i)
                put(cx + (radius * cos(radians)).toFloat())
                put(cy + (radius * sin(radians)).toFloat())
                i += DEGREE_SPAN
            }
            position(0)
        }
    }

    fun update(left: Float, top: Float, right: Float, bottom: Float, radius: Float) {
        vertexBuffer.apply {
            //中间矩形的4个顶点
            position(0)
            //索引-0
            put(left + radius)
            put(top - radius)
            //索引-1
            put(left + radius)
            put(bottom + radius)
            //索引-2
            put(right - radius)
            put(bottom + radius)
            //索引-3
            put(right - radius)
            put(top - radius)
            //索引-4
            put(left)
            put(top - radius)
            //索引-5
            put(left)
            put(bottom + radius)
            //索引-6
            put(left + radius)
            put(bottom)
            //索引-7
            put(right - radius)
            put(bottom)
            //索引-8
            put(right)
            put(bottom + radius)
            //索引-9
            put(right)
            put(top - radius)
            //索引-10
            put(right - radius)
            put(top)
            //索引-11
            put(left + radius)
            put(top)
            position(0)
        }
        sector(vertexBufferRT, right - radius, top - radius, radius, 0f)
        sector(vertexBufferLT, left + radius, top - radius, radius, 90f)
        sector(vertexBufferLB, left + radius, bottom + radius, radius, 180f)
        sector(vertexBufferRB, right - radius, bottom + radius, radius, 270f)
    }

    fun drawBorder(
        positionHandle: Int,
        colorHandle: Int,
        r: Float,
        g: Float,
        b: Float,
        lineWidth: Float
    ) {
        GLES20.glUniform4f(colorHandle, r, g, b, 1f)
        GLES20.glLineWidth(lineWidth)

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)
        GLES20.glDrawElements(
            GLES20.GL_LINES,
            borderIndices.size,
            GLES20.GL_UNSIGNED_SHORT,
            borderIndicesBuffer
        )

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBufferRT)
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 1, VERTEX_COUNT - 1)

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBufferLT)
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 1, VERTEX_COUNT - 1)

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBufferLB)
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 1, VERTEX_COUNT - 1)

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBufferRB)
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 1, VERTEX_COUNT - 1)
    }

    fun draw(positionHandle: Int, colorHandle: Int, r: Float, g: Float, b: Float) {
        GLES20.glUniform4f(colorHandle, r, g, b, 1f)

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indices.size,
            GLES20.GL_UNSIGNED_SHORT,
            indicesBuffer
        )

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBufferRT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, VERTEX_COUNT)

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBufferLT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, VERTEX_COUNT)

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBufferLB)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, VERTEX_COUNT)

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBufferRB)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, VERTEX_COUNT)
    }

}