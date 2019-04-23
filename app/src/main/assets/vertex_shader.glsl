uniform mat4 u_MVPMatrix;

attribute vec4 a_Position;
attribute vec4 a_Color;
uniform float a_step;

varying vec4 v_Color;

void main() {
    v_Color = vec4(a_step, 1.0 - a_step, 1, 1);
    gl_Position = u_MVPMatrix * a_Position;
}