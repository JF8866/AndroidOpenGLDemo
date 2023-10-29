package site.feiyuliuxing.opengltest.nativelib

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import site.feiyuliuxing.opengltest.nativelib.NativeGLRenderer

class NativeGLSurfaceView : GLSurfaceView {

    private val renderer: Renderer

    constructor(context: Context):super(context)
    constructor(context: Context, attr: AttributeSet):super(context, attr)

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer = NativeGLRenderer(context)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
        // 将渲染模式设置为仅在绘制数据发生变化时绘制视图
//        renderMode = RENDERMODE_WHEN_DIRTY
    }
}
