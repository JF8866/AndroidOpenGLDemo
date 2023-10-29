#include <jni.h>
#include <android/log.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <EGL/egl.h>
#include <EGL/eglext.h> //EGL用于分配和管理 OpenGL ES 上下文和 Surface
#include "my_gl_renderer.h"
#include "log_util.h"

extern "C"
JNIEXPORT void JNICALL
Java_site_feiyuliuxing_opengltest_nativelib_NativeLib_onSurfaceCreated(JNIEnv *env, jobject thiz) {
    //onSurfaceCreated
    __android_log_print(ANDROID_LOG_INFO, "NDK GLES", "%s", glGetString(GL_VERSION));
    LOG_I("##### %s", glGetString(GL_VERSION));
    on_surface_created();
}
extern "C"
JNIEXPORT void JNICALL
Java_site_feiyuliuxing_opengltest_nativelib_NativeLib_onSurfaceChanged(
        JNIEnv *env, jobject thiz, jint width, jint height) {
    // onSurfaceChanged
    on_surface_changed(width, height);
}
extern "C"
JNIEXPORT void JNICALL
Java_site_feiyuliuxing_opengltest_nativelib_NativeLib_onDrawFrame(JNIEnv *env, jobject thiz) {
    // onDrawFrame
    on_draw_frame();
}