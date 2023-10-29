package site.feiyuliuxing.opengltest.shapes

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin

private const val DEGREE_SPAN = 6
private const val VERTEX_COUNT = 360 / DEGREE_SPAN + 2

class Circular(cx:Float, cy:Float, radius: Float) {
    private val vertexBuffer = ByteBuffer.allocateDirect(VERTEX_COUNT * 2 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    init {
        update(cx, cy, radius)
    }

    fun update(cx:Float, cy:Float, radius: Float) {
        vertexBuffer.apply {
            position(0)
            put(cx)
            put(cy)
            var degree = 0.0
            var radians: Double
            while (degree <= 360f) {
                radians = Math.toRadians(degree)
                put(cx + (radius * cos(radians)).toFloat())
                put(cy + (radius * sin(radians)).toFloat())
                degree += DEGREE_SPAN
            }
            position(0)
        }
    }

    fun drawBorder(positionHandle: Int, colorHandle: Int, r:Float,g:Float,b:Float, lineWidth:Float) {
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)
        GLES20.glUniform4f(colorHandle, r, g, b, 1f)
        GLES20.glLineWidth(lineWidth)
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 1, VERTEX_COUNT - 1)
    }

    fun draw(positionHandle: Int, colorHandle: Int, r:Float,g:Float,b:Float) {
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)
        GLES20.glUniform4f(colorHandle, r, g, b, 1f)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, VERTEX_COUNT)
    }
}