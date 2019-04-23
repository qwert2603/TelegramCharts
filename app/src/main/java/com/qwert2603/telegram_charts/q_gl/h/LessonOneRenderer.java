package com.qwert2603.telegram_charts.q_gl.h;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.qwert2603.telegram_charts.DataParser;
import com.qwert2603.telegram_charts.LogUtils;
import com.qwert2603.telegram_charts.entity.ChartData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LessonOneRenderer implements GLSurfaceView.Renderer {

    private float[] mTranslateMatrix = new float[16];
    private float[] mScaleMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private List<FloatBuffer> mPositions = new ArrayList<>();
    private List<float[]> yyy = new ArrayList<>();
    private final float[] sums;

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int FLOATS_PER_VERTEX = 3;
    private static final int VERTICES_PER_TRIANGLE = 3;
    private static final int TRIANGLES_PER_AREA = 2;

    private final int valuesCount;
    private Context context;
    private ChartData chartData;

    public LessonOneRenderer(Context context, ChartData chartData) {
        this.context = context;
        this.chartData = chartData;
        valuesCount = chartData.xValues.length;

        sums = new float[valuesCount];

        for (int i = 0; i < chartData.xValues.length; i++) {
            sums[i] = 0;
            for (int c = 0; c < chartData.lines.size(); c++) {
                sums[i] += chartData.lines.get(c).values[i];
            }
        }

        for (int i = 0; i < chartData.lines.size(); i++) {
            fillArrays(i);
        }
    }

    private void fillArrays(int index) {
        final float[] valuesX = new float[valuesCount];
        final float[] valuesY = new float[valuesCount];

        yyy.add(valuesY);

        for (int i = 0; i < valuesCount; i++) {
            valuesX[i] = chartData.xValues[i];
            valuesY[i] = 0;
            for (int j = 0; j <= index; j++) {
                valuesY[i] += chartData.lines.get(j).values[i];
            }
            valuesY[i] /= sums[i];
        }

        final float[] valuesData = new float[(valuesCount - 1) * FLOATS_PER_VERTEX * VERTICES_PER_TRIANGLE * TRIANGLES_PER_AREA];

        for (int u = 0; u < valuesCount - 1; u++) {
            final float low = index == 0 ? 0f : yyy.get(index - 1)[u];
            final float lowNext = index == 0 ? 0f : yyy.get(index - 1)[u + 1];

            valuesData[u * 18] = valuesX[u];
            valuesData[u * 18 + 1] = low;
            valuesData[u * 18 + 2] = 0f;
            valuesData[u * 18 + 3] = valuesX[u + 1];
            valuesData[u * 18 + 4] = lowNext;
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

        FloatBuffer floatBuffer = ByteBuffer
                .allocateDirect(valuesData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        floatBuffer.put(valuesData).position(0);

        mPositions.add(floatBuffer);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES20.glClearColor(1f, 1f, 1f, 1f);

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 3, 0f, 0.0f, 0f, 0f, 1.0f, 0.0f);

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

//        final float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -1f, 1f, 3f, 3.000001f);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {

        long l = SystemClock.elapsedRealtime();

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.setIdentityM(mTranslateMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);

        float centerX = (chartData.xValues[0] + chartData.xValues[chartData.xValues.length - 1]) / 2f;
        float centerY = 0.5f;
        float dX = chartData.xValues[chartData.xValues.length - 1] - chartData.xValues[0];

        Matrix.translateM(mTranslateMatrix, 0, -centerX, -centerY, 0.0f);
        Matrix.scaleM(mScaleMatrix, 0, 1f / dX * 2f * 0.99f, 1f / centerY * 0.99f, 1f);

        Matrix.multiplyMM(mMVPMatrix, 0, mScaleMatrix, 0, mTranslateMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        drawSquare();

        for (int i = 0; i < chartData.lines.size(); i++) {
            drawValues(i);
        }

        LogUtils.d("SystemClock.elapsedRealtime()-l " + (SystemClock.elapsedRealtime() - l));
    }

    private void drawSquare() {
        int color = 0xFFFF0000;
        GLES20.glUniform4f(mColorHandle, Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, 1f);

        FloatBuffer floatBuffer = ByteBuffer
                .allocateDirect(BYTES_PER_FLOAT * FLOATS_PER_VERTEX * VERTICES_PER_TRIANGLE * TRIANGLES_PER_AREA)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        float a = 0.99f;

        floatBuffer.put(new float[]{
                -a, -a, 0,
                a, -a, 0,
                a, a, 0,
                a, a, 0,
                -a, a, 0,
                -a, -a, 0
        });

        floatBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, FLOATS_PER_VERTEX, GLES20.GL_FLOAT, false, 3 * BYTES_PER_FLOAT, floatBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTICES_PER_TRIANGLE * TRIANGLES_PER_AREA);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    private void drawValues(int index) {
        int color = chartData.lines.get(index).color;
        GLES20.glUniform4f(mColorHandle, Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, 1f);

        FloatBuffer floatBuffer = mPositions.get(index);
        floatBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, FLOATS_PER_VERTEX, GLES20.GL_FLOAT, false, 3 * BYTES_PER_FLOAT, floatBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, (valuesCount - 1) * 6);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}