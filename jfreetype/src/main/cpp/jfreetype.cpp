#include <jni.h>
#include <string>
#include <android//log.h>
#include "ft2build.h"
#include FT_FREETYPE_H

#define LOG_I(...) __android_log_print(ANDROID_LOG_INFO, "NDK FT", __VA_ARGS__)
#define LOG_W(...) __android_log_print(ANDROID_LOG_WARN, "NDK FT", __VA_ARGS__)
#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR, "NDK FT", __VA_ARGS__)

// https://freetype.org/freetype2/docs/tutorial

FT_Library library;   /* handle to library     */
FT_Face face;      /* handle to face object */

extern "C" JNIEXPORT jint JNICALL
Java_site_feiyuliuxing_jfreetype_JFreeType_init(
        JNIEnv *env,
        jobject, jobject face_buffer) {
    std::string hello = "Hello from C++";
    FT_Error error = FT_Init_FreeType(&library);
    if (error) {
        LOG_E("an error occurred during library initialization, error: %d", error);
        return error;
    }
    jbyte *buffer = (jbyte *) (env->GetDirectBufferAddress(face_buffer));
    jlong size = env->GetDirectBufferCapacity(face_buffer);
    error = FT_New_Memory_Face(library,
                               (FT_Byte *) buffer,    /* first byte in memory */
                               size,      /* size in bytes        */
                               0,         /* face_index           */
                               &face);
    if (error) {
        LOG_E("an error occurred during FT_New_Memory_Face, error: %d", error);
        return error;
    }
    error = FT_Set_Pixel_Sizes(
            face,   /* handle to face object */
            0,      /* pixel_width           */
            128);   /* pixel_height          */
    if (error) {
        LOG_E("an error occurred during FT_Set_Pixel_Sizes, error: %d", error);
        return error;
    }
    return 0;
}


extern "C"
JNIEXPORT jint JNICALL
Java_site_feiyuliuxing_jfreetype_JFreeType_charBitmap(
        JNIEnv *env, jobject thiz,
        jobject ft_bitmap, jchar charcode) {
    FT_UInt glyph_index = FT_Get_Char_Index(face, charcode);
    FT_Error error = FT_Load_Glyph(
            face,          /* handle to face object */
            glyph_index,   /* glyph index           */
            FT_LOAD_DEFAULT);  /* load flags, see below */
    if (error) {
        LOG_E("an error occurred during FT_Get_Char_Index, error: %d", error);
        return error;
    }
    error = FT_Render_Glyph(face->glyph,   /* glyph slot  */
                            FT_RENDER_MODE_NORMAL); /* render mode */
    if (error) {
        LOG_E("an error occurred during FT_Render_Glyph, error: %d", error);
        return error;
    }
    FT_Bitmap bitmap = face->glyph->bitmap;

    LOG_I("--------------- %c ---------------", charcode);
    LOG_I("FT_Bitmap size: %d x %d", bitmap.width, bitmap.rows);
    LOG_I("FT_Bitmap pixel mode: %d", bitmap.pixel_mode);
    LOG_I("FT_Bitmap bitmap top: %d", face->glyph->bitmap_top);
    LOG_I("metrics.height: %ld", face->glyph->metrics.height);
    LOG_I("metrics.horiBearingY: %ld", face->glyph->metrics.horiBearingY);

    jclass bmpCls = env->GetObjectClass(ft_bitmap);
    jfieldID rowsID = env->GetFieldID(bmpCls, "rows", "I");
    jfieldID widthID = env->GetFieldID(bmpCls, "width", "I");
    jfieldID bufferID = env->GetFieldID(bmpCls, "buffer", "[B");
    jfieldID leftID = env->GetFieldID(bmpCls, "bitmapLeft", "I");
    jfieldID topID = env->GetFieldID(bmpCls, "bitmapTop", "I");

    env->SetIntField(ft_bitmap, rowsID, (int) bitmap.rows);
    env->SetIntField(ft_bitmap, widthID, (int) bitmap.width);
    env->SetIntField(ft_bitmap, leftID, face->glyph->bitmap_left);
    env->SetIntField(ft_bitmap, topID, face->glyph->bitmap_top);

    int dataLength = bitmap.rows * bitmap.width;
    jbyteArray buf = env->NewByteArray(dataLength);
    jbyte *data = env->GetByteArrayElements(buf, nullptr);

    for (int i = 0; i < dataLength; ++i) {
        data[i] = bitmap.buffer[i];
    }
    env->ReleaseByteArrayElements(buf, data, 0);
    env->SetObjectField(ft_bitmap, bufferID, buf);

    return 0;
}


extern "C"
JNIEXPORT void JNICALL
Java_site_feiyuliuxing_jfreetype_JFreeType_close(JNIEnv *env, jobject thiz) {
    FT_Done_FreeType(library);
}