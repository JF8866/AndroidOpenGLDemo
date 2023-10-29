package site.feiyuliuxing.opengltest.shapes

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Rectangle(var left: Float, var top: Float, var right: Float, var bottom: Float) {
    private val indices = shortArrayOf(
        0, 1, 2, 0, 2, 3
    )
    private val indicesBuffer = ByteBuffer.allocateDirect(indices.size * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .put(indices)
        .position(0)

    //4个顶点，8个Float，即32字节
    private val vertexBuffer = ByteBuffer.allocateDirect(32)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    init {
        update(left, top, right, bottom)
    }

    fun update(left: Float, top: Float, right: Float, bottom: Float) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
        vertexBuffer.apply {
            position(0)
            put(left)
            put(top)
            put(left)
            put(bottom)
            put(right)
            put(bottom)
            put(right)
            put(top)
            position(0)
        }
    }

    fun drawBorder(positionHandle: Int, colorHandle:Int, r:Float, g:Float, b:Float, lineWidth:Float) {
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)
        GLES20.glUniform4f(colorHandle, r, g, b, 1f)
        GLES20.glLineWidth(lineWidth)
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4)
    }
    fun draw(positionHandle: Int, colorHandle:Int, r:Float, g:Float, b:Float) {
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)
        GLES20.glUniform4f(colorHandle, r, g, b, 1f)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, indicesBuffer)
    }

}