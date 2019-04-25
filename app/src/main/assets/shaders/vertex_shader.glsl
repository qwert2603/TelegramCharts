uniform mat4 u_MVPMatrix;
uniform float u_LinesCount;
uniform float u_LineIndex;
uniform mat4 u_A;

attribute float a_X;
attribute vec4 a_Y1;
attribute vec4 a_Y2;
attribute float a_Top;

void main() {
    if (a_Top > 0.5) {
        vec4 pos = vec4(a_X, 1.0, 0.0, 1.0);
        gl_Position = u_MVPMatrix * pos;
    } else {
        float ys[8];
        ys[0] = a_Y1[0] * u_A[0][0];
        ys[1] = a_Y1[1] * u_A[0][1];
        ys[2] = a_Y1[2] * u_A[0][2];
        ys[3] = a_Y1[3] * u_A[0][3];
        ys[4] = a_Y2[0] * u_A[1][0];
        ys[5] = a_Y2[1] * u_A[1][1];
        ys[6] = a_Y2[2] * u_A[1][2];
        ys[7] = a_Y2[3] * u_A[1][3];

        int lineIndex = int(u_LineIndex + 0.5);
        int linesCount = int(u_LinesCount + 0.5);

        float y = 0.0;
        for (int i = 0; i < lineIndex; ++i) {
            y += ys[i];
        }

        float sum = 0.0;
        for (int i = 0; i < linesCount; ++i) {
            sum += ys[i];
        }
        y /= sum;

        vec4 pos = vec4(a_X, y, 0.0, 1.0);
        gl_Position = u_MVPMatrix * pos;
    }
}