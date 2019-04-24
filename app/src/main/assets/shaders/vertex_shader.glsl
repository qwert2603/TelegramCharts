uniform mat4 u_MVPMatrix;
uniform int u_LinesCount;
uniform int u_LineIndex;
uniform vec4 u_A1;
uniform vec4 u_A2;

attribute float a_X;
attribute vec4 a_Y1;
attribute vec4 a_Y2;
attribute float a_Top;

void main() {
    float ys[8];
    ys[0] = a_Y1.x * u_A1.x;
    ys[1] = a_Y1.y * u_A1.y;
    ys[2] = a_Y1.z * u_A1.z;
    ys[3] = a_Y1.w * u_A1.w;
    ys[4] = a_Y2.x * u_A2.x;
    ys[5] = a_Y2.y * u_A2.y;
    ys[6] = a_Y2.z * u_A2.z;
    ys[7] = a_Y2.w * u_A2.w;

    float y = 0.0;
    for (int i = 0; i < u_LineIndex; ++i) {
        y += ys[i];
    }
    if (a_Top > 0.5) {
        y += ys[u_LineIndex];
    }

    float sum = 0.0;
    for (int i = 0; i < u_LinesCount; ++i) {
        sum += ys[i];
    }
    y /= sum;

    vec4 pos = vec4(a_X, y, 0.0, 1.0);
    gl_Position = u_MVPMatrix * pos;
}