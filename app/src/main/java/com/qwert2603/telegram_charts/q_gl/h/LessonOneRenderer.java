package com.qwert2603.telegram_charts.q_gl.h;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.qwert2603.telegram_charts.DataParser;

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

    private final FloatBuffer mTrianglesVertices;
    private final FloatBuffer mTrianglesColors;

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;

    private final int mBytesPerFloat = 4;

    private final int mPositionDataSize = 3;
    private final int mColorDataSize = 4;

    private float x;

    private final int trianglesCount = 100;
    private Context context;

    public LessonOneRenderer(Context context) {
        this.context = context;
        final float[] trianglesVerticesData = new float[mPositionDataSize * 3 * trianglesCount];
        final float[] trianglesColorsData = new float[mColorDataSize * 3 * trianglesCount];

        for (int i = 0; i < trianglesCount; i++) {
            float q = i - trianglesCount / 2f;
            trianglesVerticesData[i * 9] = q / trianglesCount;
            trianglesVerticesData[i * 9 + 1] = 0f;
            trianglesVerticesData[i * 9 + 2] = 0f;
            trianglesVerticesData[i * 9 + 3] = (q + 1) / trianglesCount;
            trianglesVerticesData[i * 9 + 4] = 0f;
            trianglesVerticesData[i * 9 + 5] = 0f;
            trianglesVerticesData[i * 9 + 6] = q / trianglesCount;
            trianglesVerticesData[i * 9 + 7] = 0.5f;
            trianglesVerticesData[i * 9 + 8] = 0f;
        }

        for (int i = 0; i < trianglesCount * 3; i++) {
            trianglesColorsData[i * 4] = i * 1f / (trianglesCount * 3);
            trianglesColorsData[i * 4 + 1] = 1f - i * 1f / (trianglesCount * 3);
            trianglesColorsData[i * 4 + 2] = 0f;
            trianglesColorsData[i * 4 + 3] = 0.5f;
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

        final String vertexShader = DataParser.readAsset(context, "vertex_shader");
        final String fragmentShader = DataParser.readAsset(context, "fragment_shader");

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
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

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
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, 0.0f, 0.0f);
        drawTriangle();

//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, x + 0.3f, 0.0f, 0.0f);
//        drawTriangle(mTriangle2Vertices);

//        if (x <= 1) {
//            x = x + 0.001f;
//        } else {
//            x = 0;
//        }

//        Matrix.setIdentityM(mModelMatrix, 0);
//        drawTriangle(mTriangle3Vertices);
//
//        Matrix.setIdentityM(mModelMatrix, 0);
//        drawTriangle(mTriangle4Vertices);
//
//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, x, 0.0f, 0.0f);
//        drawTriangle(mTriangle5Vertices);
//
//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, x, 0.0f, 0.0f);
//        drawTriangle(mTriangle6Vertices);

    }

    private void drawTriangle(/*final FloatBuffer aTriangleBuffer*/) {
        mTrianglesVertices.position(0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                3 * mBytesPerFloat, mTrianglesVertices);

        GLES20.glEnableVertexAttribArray(mColorHandle);
        mTrianglesColors.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                4 * mBytesPerFloat, mTrianglesColors);


        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, trianglesCount * 3);
    }
}