//
// Created by 贾捷飞 on 2023/10/28.
//
#include <android/bitmap.h>
#include <malloc.h>
#include <iosfwd>
#include "gl_bitmap.h"
#include "log_util.h"

const int VERTEX_INDEX_COUNT = 12;
//绘制顺序索引
const uint16_t VERTEX_INDEX[VERTEX_INDEX_COUNT] = {
        0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
        0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
        0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
        0, 4, 1   //V0,V4,V1 三个顶点组成一个三角形
};

//顶点坐标 图片尺寸 1000 x 562
const float POSITION_VERTEX[15] = {
        0.0f, 0.0f, 0.0f,       //顶点坐标V0
        1.0f, 0.562f, 0.0f,     //顶点坐标V1
        -1.0f, 0.562f, 0.0f,    //顶点坐标V2
        -1.0f, -0.562f, 0.0f,  //顶点坐标V3
        1.0f, -0.562f, 0.0f   //顶点坐标V4
};

//纹理坐标
const float TEX_VERTEX[10] = {
        0.5f, 0.5f,   //纹理坐标V0
        1.0f, 0.0f,   //纹理坐标V1
        0.0f, 0.0f,   //纹理坐标V2
        0.0f, 1.0f,   //纹理坐标V3
        1.0f, 1.0f    //纹理坐标V4
};

GLuint textureId;

const char* bitmapFormatString(int32_t format) {
    switch (format) {
        case ANDROID_BITMAP_FORMAT_NONE:
            return "None";
        case ANDROID_BITMAP_FORMAT_A_8:
            return "A_8";
        case ANDROID_BITMAP_FORMAT_RGBA_1010102:
            return "RGBA_1010102";
        case ANDROID_BITMAP_FORMAT_RGBA_4444:
            return "RGBA_4444";
        case ANDROID_BITMAP_FORMAT_RGBA_8888:
            return "RGBA_8888";
        case ANDROID_BITMAP_FORMAT_RGBA_F16:
            return "RGBA_F16";
        case ANDROID_BITMAP_FORMAT_RGB_565:
            return "RGB_565";
        default:
            return "Unknown";
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_site_feiyuliuxing_opengltest_nativelib_NativeLib_loadTexture(
        JNIEnv *env, jobject thiz, jobject bitmap) {
    //创建一个纹理对象
    glGenTextures(1, &textureId);
    if (textureId == 0) {
        LOG_E("%s", "Could not generate a new OpenGL textureId object.");
        return 0;
    }

    //绑定纹理到OpenGL
    glBindTexture(GL_TEXTURE_2D, textureId);
    //设置默认的纹理过滤参数
    glTexParameteri(GL_TEXTURE_2D,
                    GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR_MIPMAP_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER,
                    GL_LINEAR);
    //加载bitmap到纹理中

    // C++ 访问 Java 层 Bitmap 的接口见官方文档
    // https://developer.android.com/ndk/reference/group/bitmap
    AndroidBitmapInfo bitmapInfo;
    AndroidBitmap_getInfo(env, bitmap, &bitmapInfo);
    uint8_t *pixels;
    AndroidBitmap_lockPixels(env, bitmap, (void **) (&pixels));

    auto width = (GLsizei) bitmapInfo.width;
    auto height = (GLsizei) bitmapInfo.height;
    // AndroidBitmapFormat
    LOG_I("bitmap format: %s, size: %dx%d", bitmapFormatString(bitmapInfo.format), width, height);

    //加载bitmap到纹理中，bitmap 格式是 RGBA_8888
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,
                 width, height, 0, GL_RGBA,
                 GL_UNSIGNED_BYTE, pixels);

    //生成MIP贴图
    glGenerateMipmap(GL_TEXTURE_2D);
    AndroidBitmap_unlockPixels(env, bitmap);
    //取消绑定纹理
    glBindTexture(GL_TEXTURE_2D, 0);
    LOG_I("textureId=%d", textureId);
    return (jint) textureId;
}

void drawBitmap(GLint positionHandle, GLint textureCoordHandle) {
    glVertexAttribPointer(positionHandle, 3, GL_FLOAT,
                          false, 0, POSITION_VERTEX);

    glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT,
                          false, 0, TEX_VERTEX);
    //激活纹理
    glActiveTexture(GL_TEXTURE0);
    //绑定纹理
    glBindTexture(GL_TEXTURE_2D, textureId);
    // 绘制
    glDrawElements(GL_TRIANGLES, VERTEX_INDEX_COUNT,
                   GL_UNSIGNED_SHORT, VERTEX_INDEX);
}