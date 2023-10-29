package site.feiyuliuxing.opengltest.nativelib

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import site.feiyuliuxing.opengltest.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class NativeGLRenderer(val context: Context) : GLSurfaceView.Renderer {
    private val nativeLib = NativeLib()

    private fun loadTexture(resourceId: Int) {
        val options = BitmapFactory.Options()
        //这里需要加载原图未经缩放的数据
        options.inScaled = false
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
        nativeLib.loadTexture(bitmap)
        bitmap.recycle()
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        nativeLib.onSurfaceCreated()
        loadTexture(R.mipmap.gougou)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        nativeLib.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        nativeLib.onDrawFrame()
    }
}