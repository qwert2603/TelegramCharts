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
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LessonOneRenderer implements GLSurfaceView.Renderer {

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private FloatBuffer mPositions;

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int FLOATS_PER_VERTEX = 3;
    private static final int VERTICES_PER_TRIANGLE = 3;
    private static final int TRIANGLES_PER_AREA = 3;

    private final int valuesCount = 5000;
    private Context context;

    public LessonOneRenderer(Context context) {
        this.context = context;
    }

    private void fillArrays() {
        final float[] valuesX = new float[valuesCount];
        final float[] valuesY = new float[valuesCount];

        Random random = new Random(42);

        for (int i = 0; i < valuesCount; i++) {
            valuesX[i] = (i * 1f / (valuesCount - 1)) * 1.4f - 0.7f;
            valuesY[i] = random.nextFloat() + 0.1f;
        }

        final float[] valuesData = new float[(valuesCount - 1) * FLOATS_PER_VERTEX * VERTICES_PER_TRIANGLE * TRIANGLES_PER_AREA];

        final float low = 0f;

        for (int u = 0; u < valuesCount - 1; u++) {
            valuesData[u * 18] = valuesX[u];
            valuesData[u * 18 + 1] = low;
            valuesData[u * 18 + 2] = 0f;
            valuesData[u * 18 + 3] = valuesX[u + 1];
            valuesData[u * 18 + 4] = low;
            valuesData[u * 12 + 5] = 0f;
            valuesData[u * 18 + 6] = valuesX[u + 1];
            valuesData[u * 18 + 7] = valuesY[u + 1];
            valuesData[u * 18 + 8] = 0f;

            valuesData[u * 18 + 9] = valuesX[u + 1];
            valuesData[u * 18 + 10] = valuesY[u + 1];
            valuesData[u * 18 + 11] = 0f;
            valuesData[u * 18 + 12] = valuesX[u];
            valuesData[u * 18 + 13] = valuesY[u];
            valuesData[u * 18 + 14] = 0f;
            valuesData[u * 18 + 15] = valuesX[u];
            valuesData[u * 18 + 16] = low;
            valuesData[u * 18 + 17] = 0f;
        }

        mPositions = ByteBuffer
                .allocateDirect(valuesData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mPositions.put(valuesData).position(0);
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

        GLES20.glLinkProgram(programHandle);


        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetUniformLocation(programHandle, "u_Color");

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

        fillArrays();
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {

        long l = SystemClock.elapsedRealtime();

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0, -0.8f, 0.0f);
        Matrix.scaleM(mModelMatrix, 0, 0.5f, 1, 1);

        mPositions.position(0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, FLOATS_PER_VERTEX, GLES20.GL_FLOAT, false, 3 * BYTES_PER_FLOAT, mPositions);

        GLES20.glUniform4f(mColorHandle, 1, 0, 1, 1);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, (valuesCount - 1) * 6);

        GLES20.glDisableVertexAttribArray(mPositionHandle);

        LogUtils.d("SystemClock.elapsedRealtime()-l " + (SystemClock.elapsedRealtime() - l));
    }

}