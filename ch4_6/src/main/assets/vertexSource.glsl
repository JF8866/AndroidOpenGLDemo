#version 300 es
//uniform mat4 uMVPMatrix;//没用到
layout(location=0) in vec3 position;
uniform mat4 v_matrix;
uniform mat4 proj_matrix;
uniform float tf;//时间因子
out vec4 varyingColor;

mat4 buildTranslate(float x, float y, float z);
mat4 buildRotateX(float rad);
mat4 buildRotateY(float rad);
mat4 buildRotateZ(float rad);

void main(void) {
    float i = float(gl_InstanceID) + tf;
    //a b c是用来平移的x y z分量
    /*float a = sin(2.0 * i) * 8.0;
    float b = sin(3.0 * i) * 8.0;
    float c = sin(4.0 * i) * 8.0;*/
    float a = sin(203.0 * i / 1000.0) * 103.0;
    float b = sin(301.0 * i / 501.0) * 101.0;
    float c = sin(400.0 * i / 1503.0) * 105.0;
    mat4 localRotX = buildRotateX(1000.0 * i);
    mat4 localRotY = buildRotateY(1000.0 * i);
    mat4 localRotZ = buildRotateZ(1000.0 * i);
    mat4 localTrans = buildTranslate(a, b, c);
    mat4 newM_matrix = localTrans * localRotX * localRotY * localRotZ;
    mat4 mv_matrix = v_matrix * newM_matrix;
    gl_Position = proj_matrix * mv_matrix * vec4(position, 1.0);
    // 将位置坐标乘1/2，然后加1/2，以将取值区间从[−1, +1]转换为[0, 1]
    varyingColor = vec4(position, 1.0) * 0.5 + vec4(0.5, 0.5, 0.5, 0.5);
}

mat4 buildTranslate(float x, float y, float z) {
    return mat4(
    1.0, 0.0, 0.0, 0.0,
    0.0, 1.0, 0.0, 0.0,
    0.0, 0.0, 1.0, 0.0,
    x, y, z, 1.0);
}

mat4 buildRotateX(float rad) {
    return mat4(
    1.0, 0.0, 0.0, 0.0,
    0.0, cos(rad), -sin(rad), 0.0,
    0.0, sin(rad), cos(rad), 0.0,
    0.0, 0.0, 0.0, 1.0);
}

mat4 buildRotateY(float rad) {
    return mat4(
    cos(rad), 0.0, sin(rad), 0.0,
    0.0, 1.0, 0.0, 0.0,
    -sin(rad), 0.0, cos(rad), 0.0,
    0.0, 0.0, 0.0, 1.0);
}

mat4 buildRotateZ(float rad) {
    return mat4(
    cos(rad), -sin(rad), 0.0, 0.0,
    sin(rad), cos(rad), 0.0, 0.0,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0);
}

mat4 buildScale(float x, float y, float z) {
    return mat4(
    x, 0.0, 0.0, 0.0,
    0.0, y, 0.0, 0.0,
    0.0, 0.0, z, 0.0,
    0.0, 0.0, 0.0, 1.0);
}