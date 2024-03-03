package site.feiyuliuxing.jfreetype

import android.graphics.Bitmap
import android.graphics.Color

class FTBitmap @JvmOverloads constructor(
    var rows: Int = 0,
    var width: Int = 0,
    var buffer: ByteArray? = null,
    var bitmapLeft: Int = 0,
    var bitmapTop: Int = 0,
) {
    fun toBitmap(maxAscent: Int, maxDescent: Int): Bitmap? {
        if (buffer == null) return null

        val xOffset = bitmapLeft
        val yOffset = maxAscent - bitmapTop
        val width = this.width + xOffset
        val height = rows + yOffset + maxDescent - (rows - bitmapTop)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (y in 0 until rows) {
            for (x in 0 until this.width) {
                val index = y * this.width + x
                val pixelValue = buffer!![index].toInt() and 0xff
                bitmap.setPixel(x + xOffset, y + yOffset, Color.rgb(pixelValue, pixelValue, pixelValue))
            }
        }
        return bitmap
    }
}