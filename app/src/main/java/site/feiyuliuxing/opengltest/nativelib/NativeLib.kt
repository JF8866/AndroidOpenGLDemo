package site.feiyuliuxing.opengltest.nativelib

import android.graphics.Bitmap

class NativeLib {
    companion object {
        init {
            System.loadLibrary("native-lib");
        }
    }

    external fun onSurfaceCreated()
    external fun onSurfaceChanged(width:Int, height:Int)
    external fun onDrawFrame()

    external fun loadTexture(bitmap: Bitmap): Int
}