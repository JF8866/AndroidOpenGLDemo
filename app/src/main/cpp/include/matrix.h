//
// Created by 贾捷飞 on 2023/10/28.
//

#ifndef OPENGLTEST_MATRIX_H
#define OPENGLTEST_MATRIX_H

//矩阵函数是翻译了Java层接口 android.opengl.Matrix 的静态方法，代码来自以下连接：
// https://stackoverflow.com/questions/5201709/c-matrix-libraries-suited-for-opengl-on-android-ndk

//创建单位矩阵
void matrixSetIdentityM(float *m);
//旋转矩阵
void matrixSetRotateM(float *m, float a, float x, float y, float z);
//矩阵相乘
void matrixMultiplyMM(float *m, float *lhs, float *rhs);
//缩放矩阵
void matrixScaleM(float *m, float x, float y, float z);
void matrixTranslateM(float *m, float x, float y, float z);
void matrixRotateM(float *m, float a, float x, float y, float z);
void matrixLookAtM(float *m,
                   float eyeX, float eyeY, float eyeZ,
                   float cenX, float cenY, float cenZ,
                   float  upX, float  upY, float  upZ);
void matrixFrustumM(float *m, float left, float right, float bottom, float top, float near, float far);

#endif //OPENGLTEST_MATRIX_H
