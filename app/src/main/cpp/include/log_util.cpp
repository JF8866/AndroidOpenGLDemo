//
// Created by 贾捷飞 on 2023/10/28.
//
#include "log_util.h"
#include <cstdlib>

void printShaderLog(GLuint shader) {
    int len = 0;
    int chWrittn = 0;
    char *log;
    glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &len);
    if(len > 0) {
        log = (char*)malloc(len);
        glGetShaderInfoLog(shader, len, &chWrittn, log);
        LOG_E("Shader Info Log: %s", log);
        free(log);
    }
}

void printProgramLog(GLuint program) {
    int len = 0;
    int chWrittn = 0;
    char *log;
    glGetProgramiv(program, GL_INFO_LOG_LENGTH, &len);
    if(len > 0) {
        log = (char*) malloc(len);
        glGetProgramInfoLog(program, len, &chWrittn, log);
        LOG_E("Program Info Log: %s", log);
        free(log);
    }
}

bool checkOpenGLError() {
    bool foundError = false;
    GLenum glErr = glGetError();
    while (glErr != GL_NO_ERROR) {
        LOG_E("glError: %d", glErr);
        foundError = true;
        glErr = glGetError();
    }
    return foundError;
}