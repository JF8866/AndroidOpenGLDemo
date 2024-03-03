package site.feiyuliuxing.jfreetype

import java.nio.ByteBuffer

class JFreeType {

    /**
     * A native method that is implemented by the 'jfreetype' native library,
     * which is packaged with this application.
     */
    external fun init(faceBuffer: ByteBuffer): Int

    external fun charBitmap(ftBitmap: FTBitmap, char: Char): Int

    external fun close()

    companion object {
        // Used to load the 'jfreetype' library on application startup.
        init {
            System.loadLibrary("jfreetype")
        }
    }
}