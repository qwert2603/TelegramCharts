package com.qwert2603.telegram_charts.q_gl.h;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.qwert2603.telegram_charts.DataParser;
import com.qwert2603.telegram_charts.LogUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LessonOneRenderer implements GLSurfaceView.Renderer {
    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /**
     * Store the projection matrix. This is used to project the scene onto a 2D viewport.
     */
    private float[] mProjectionMatrix = new float[16];

    /**
     * Allocate storage for the final combined matrix. This will be passed into the shader program.
     */
    private float[] mMVPMatrix = new float[16];

    private FloatBuffer mTrianglesVertices;
    private FloatBuffer mTrianglesColors;

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    //    private int mColorHandle;
    private int mStepHandle;

    private final int mBytesPerFloat = 4;

    private final int mPositionDataSize = 3;
    private final int mColorDataSize = 4;

    private float x;
    private float step;

    private final int trianglesCount = 15000;
    private Context context;

    public LessonOneRenderer(Context context) {
        this.context = context;
    }

    private void fillArrays() {
        final float[] trianglesVerticesData = new float[mPositionDataSize * 3 * trianglesCount];
        final float[] trianglesColorsData = new float[mColorDataSize * 3 * trianglesCount];

        float z = 1f / trianglesCount;

        for (int u = 0; u < trianglesCount; u++) {
            int i = u - trianglesCount / 2;
            float y1 = 0.5f + (i % 10) / 100f;
            float y0 = -1.0f + Math.abs(step) / 3f;
            if (i % 2 == 0) {
                trianglesVerticesData[u * 9] = i * z;
                trianglesVerticesData[u * 9 + 1] = y0;
                trianglesVerticesData[u * 9 + 2] = 0;
                trianglesVerticesData[u * 9 + 3] = (i + 2) * z;
                trianglesVerticesData[u * 9 + 4] = y0;
                trianglesVerticesData[u * 9 + 5] = 0;
                trianglesVerticesData[u * 9 + 6] = i * z;
                trianglesVerticesData[u * 9 + 7] = y1;
                trianglesVerticesData[u * 9 + 8] = 0;
            } else {
                trianglesVerticesData[u * 9] = (i + 1) * z;
                trianglesVerticesData[u * 9 + 1] = y1;
                trianglesVerticesData[u * 9 + 2] = 0;
                trianglesVerticesData[u * 9 + 3] = (i - 1) * z;
                trianglesVerticesData[u * 9 + 4] = y1;
                trianglesVerticesData[u * 9 + 5] = 0;
                trianglesVerticesData[u * 9 + 6] = (i + 1) * z;
                trianglesVerticesData[u * 9 + 7] = y0;
                trianglesVerticesData[u * 9 + 8] = 0;
            }
        }

        for (int i = 0; i < trianglesCount * 3; i++) {
            trianglesColorsData[i * 4] = i * 1f / (trianglesCount * 3);
            trianglesColorsData[i * 4 + 1] = 1f - i * 1f / (trianglesCount * 3);
            trianglesColorsData[i * 4 + 2] = 0.01f;
            trianglesColorsData[i * 4 + 3] = 0.51f;
        }


        mTrianglesVertices = ByteBuffer
                .allocateDirect(trianglesVerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mTrianglesVertices.put(trianglesVerticesData).position(0);

        mTrianglesColors = ByteBuffer
                .allocateDirect(trianglesColorsData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mTrianglesColors.put(trianglesColorsData).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES20.glClearColor(1f, 1f, 1f, 1f);

        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 1.5f, 0.0f, 0.0f, -5.0f, 0.0f, 1.0f, 0.0f);

        final String vertexShader = DataParser.readAsset(context, "vertex_shader.glsl");
        final String fragmentShader = DataParser.readAsset(context, "fragment_shader.glsl");

        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShaderHandle, vertexShader);
        GLES20.glCompileShader(vertexShaderHandle);

        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
        GLES20.glCompileShader(fragmentShaderHandle);

        int programHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(programHandle, vertexShaderHandle);
        GLES20.glAttachShader(programHandle, fragmentShaderHandle);

        GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
        GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

        GLES20.glLinkProgram(programHandle);


        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
//        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
        mStepHandle = GLES20.glGetUniformLocation(programHandle, "a_step");

        GLES20.glUseProgram(programHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        fillArrays();
        long l = SystemClock.elapsedRealtime();

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, 0.0f, 0.0f);
        drawTriangle();

        LogUtils.d("SystemClock.elapsedRealtime()-l " + (SystemClock.elapsedRealtime() - l));
    }

    private void drawTriangle(/*final FloatBuffer aTriangleBuffer*/) {


        mTrianglesVertices.position(0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                3 * mBytesPerFloat, mTrianglesVertices);

//        GLES20.glEnableVertexAttribArray(mColorHandle);
//        mTrianglesColors.position(0);
//        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
//                4 * mBytesPerFloat, mTrianglesColors);


        GLES20.glUniform1f(mStepHandle, Math.abs(step));
        step += 0.09;
        if (step > 1) step = -1;

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, trianglesCount * 3);
    }
}