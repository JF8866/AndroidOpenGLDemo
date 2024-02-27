package site.feiyuliuxing.ch4_6

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import site.feiyuliuxing.ch5.RendererTexturedPyramid
import site.feiyuliuxing.ch6.RendererObjLoaderShuttle
import site.feiyuliuxing.ch6.RendererSphere
import site.feiyuliuxing.ch6.RendererTorus

class MyGLSurfaceView(context: Context, rendererIndex: Int) : GLSurfaceView(context) {
    private val renderer: Renderer

    init {
        setEGLContextClientVersion(3)
        renderer = when(rendererIndex) {
            1 -> RendererMultipleModels() //4.7 渲染多个不同的模型
            2 -> RendererMatrixStack() //4.8 矩阵栈
            3 -> RendererTexturedPyramid(context) //5.7 纹理贴图
            4 -> RendererSphere(context) //6.1 程序构建模型---球体
            5 -> RendererTorus(context) //6.2 OpenGL索引---环面
            6 -> RendererObjLoaderShuttle(context) //6.3 加载外部构建的模型
            else -> MyGLRenderer(context) //4.6 渲染一个对象的多个副本
        }
        setRenderer(renderer)
    }
}