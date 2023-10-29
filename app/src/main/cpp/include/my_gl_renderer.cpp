//
// Created by 贾捷飞 on 2023/10/28.
//
#include "my_gl_renderer.h"
#include <GLES2/gl2.h>
//#include <GLES2/gl2ext.h>
#include <cstring>
#include "matrix.h"
#include "log_util.h"
#include "gl_bitmap.h"

const char *vertexShaderCode =
        "uniform mat4 uMVPMatrix;"
        "attribute vec4 vPosition;"
        "attribute vec2 aTextureCoord;"
        "varying vec2 vTextureCoord;"
        "void main() {"
        "    gl_Position = uMVPMatrix * vPosition;"
        "    vTextureCoord = aTextureCoord;"
        "}";

const char *fragmentShaderSource =
        "precision mediump float;"
        "uniform bool isTexture;"
        "uniform vec4 vColor;"
        "uniform sampler2D uTextureUnit;"
        "varying vec2 vTextureCoord;"
        "varying vec4 vFragColor;"
        "void main(void) {"
        "    if(isTexture) {"
        "        gl_FragColor = texture2D(uTextureUnit,vTextureCoord);"
        "    } else {"
        "        gl_FragColor = vColor;"
        "    }"
        "}";


GLint muMVPMatrixHandle;
GLuint programHandle;
GLint positionHandle;
GLint colorHandle;
GLint isTextureHandle;
GLint textureCoordHandle;

//(相机)视图矩阵
float vMatrix[16];
float vPMatrix[16];
//投影矩阵
float projMatrix[16];

GLuint compileShaders() {
    GLint status;

    GLuint vertexShader = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertexShader, 1, &vertexShaderCode, nullptr);
    glCompileShader(vertexShader);
    glGetShaderiv(vertexShader, GL_COMPILE_STATUS, &status);
    if (status != 1) {
        LOG_E("顶点着色器编译失败：");
        printShaderLog(vertexShader);
    }

    GLuint fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragmentShader, 1, &fragmentShaderSource, nullptr);
    glCompileShader(fragmentShader);
    glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, &status);
    if (status != 1) {
        LOG_E("片段着色器编译失败：");
        printShaderLog(fragmentShader);
    }

    GLuint program = glCreateProgram();
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glLinkProgram(program);
    glGetProgramiv(program, GL_LINK_STATUS, &status);
    if (status != 1) {
        LOG_E("链接源程序失败：");
        printProgramLog(program);
    }

    // Delete the shaders as the program has them now
    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);

    return program;
}

void drawFrame() {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    //下面两行代码，防止图片的透明部分被显示成黑色
    /*glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);*/

    glEnableVertexAttribArray(positionHandle);
    glEnableVertexAttribArray(textureCoordHandle);//启用纹理坐标属性
    //画bitmap
    glUniform1i(isTextureHandle, 1);//使用纹理
    drawBitmap(positionHandle, textureCoordHandle);

    glDisableVertexAttribArray(positionHandle);
    glDisableVertexAttribArray(textureCoordHandle);
}


void on_surface_created() {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    programHandle = compileShaders();
    glUseProgram(programHandle);
    muMVPMatrixHandle = glGetUniformLocation(programHandle, "uMVPMatrix");
    positionHandle = glGetAttribLocation(programHandle, "vPosition");
    textureCoordHandle = glGetAttribLocation(programHandle, "aTextureCoord");
    colorHandle = glGetUniformLocation(programHandle, "vColor");
    isTextureHandle = glGetUniformLocation(programHandle, "isTexture");
    // Create a camera view matrix
    matrixLookAtM(
            vMatrix, 0.0f, 0.0f, 3.1f,
            0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    );
}

void on_surface_changed(int width, int height) {
    glViewport(0, 0, width, height);
    float ratio = ((float) width/* / 2.0f*/) / (float)height;
    // create a projection matrix from device screen geometry
    matrixFrustumM(projMatrix, -ratio, ratio, -1.0f, 1.0f, 3.0f, 7.0f);
}

void on_draw_frame() {
    // Combine the projection and camera view matrices
    matrixMultiplyMM(vPMatrix, projMatrix, vMatrix);
    glUniformMatrix4fv(muMVPMatrixHandle, 1, false, vPMatrix);

    drawFrame();
}
