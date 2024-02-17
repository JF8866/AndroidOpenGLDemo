package site.feiyuliuxing.ch2_1

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class MyGLSurfaceView : GLSurfaceView {
    private val renderer: Renderer

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        setEGLContextClientVersion(3)
        renderer = MyGLRenderer()
        setRenderer(renderer)
    }
}