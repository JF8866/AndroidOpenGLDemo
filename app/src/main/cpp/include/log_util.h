//
// Created by 贾捷飞 on 2023/10/28.
//

#ifndef OPENGLTEST_LOG_UTIL_H
#define OPENGLTEST_LOG_UTIL_H

#include <android/log.h>
#include <GLES2/gl2.h>

#define LOG_I(...) __android_log_print(ANDROID_LOG_INFO, "NDK GLES", __VA_ARGS__)
#define LOG_W(...) __android_log_print(ANDROID_LOG_WARN, "NDK GLES", __VA_ARGS__)
#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR, "NDK GLES", __VA_ARGS__)

bool checkOpenGLError();
void printProgramLog(GLuint program);
void printShaderLog(GLuint shader);

#endif //OPENGLTEST_LOG_UTIL_H
