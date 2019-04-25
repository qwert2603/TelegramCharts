package com.qwert2603.telegram_charts.gl;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.qwert2603.telegram_charts.DataParser;
import com.qwert2603.telegram_charts.entity.ChartData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RendererArea implements GLSurfaceView.Renderer {

    private float[] mTranslateXMatrix = new float[16];
    private float[] mScaleXMatrix = new float[16];
    private float[] mTranslateYMatrix = new float[16];
    private float[] mScaleYMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private FloatBuffer mY1;
    private FloatBuffer mY2;
    private FloatBuffer mX;
    private FloatBuffer mTop;

    public final float[] alpha = new float[8];
    private int startIndex;
    private int endIndex;

    private float mainHeight;
    private float periodSelectorHeight;

    private boolean isNight;

    public void setPeriodIndices(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public void setChartsSizes(float main, float divider, float periodSelector) {
        float sum = main + divider + periodSelector;
        mainHeight = main / sum;
        periodSelectorHeight = periodSelector / sum;
    }

    public void setNight(boolean night) {
        isNight = night;
    }

    private int mMVPMatrixHandle;
    private int mLinesCountHandle;
    private int mLineIndexHandle;
    private int mA1Handle;
    private int mA2Handle;
    private int mXHandle;
    private int mY1Handle;
    private int mY2Handle;
    private int mTopHandle;
    private int mColorHandle;

    private static final int BYTES_PER_FLOAT = 4;

    private final int valuesCount;
    private Context context;
    private ChartData chartData;
    private final int linesCount;

    private static FloatBuffer toFloatBuffer(float[] floats) {
        FloatBuffer result = ByteBuffer
                .allocateDirect(floats.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        result.put(floats);
        result.position(0);

        return result;
    }

    public RendererArea(Context context, ChartData chartData) {
        this.context = context;
        this.chartData = chartData;
        valuesCount = chartData.xValues.length;
        linesCount = chartData.lines.size();

        for (int i = 0; i < 8; i++) {
            alpha[i] = 1f;
        }
        startIndex = 0;
        endIndex = valuesCount;
        setChartsSizes(1, 0, 0);

        float[] xs = new float[valuesCount * 2];
        for (int i = 0; i < valuesCount; i++) {
            xs[i * 2] = chartData.xValues[i];
            xs[i * 2 + 1] = chartData.xValues[i];
        }
        mX = toFloatBuffer(xs);

        float[] floats1 = new float[valuesCount * 4 * 2];
        for (int i = 0; i < valuesCount; i++) {
            for (int j = 0; j < Math.min(linesCount, 4); j++) {
                floats1[i * 8 + j] = chartData.lines.get(j).values[i];
            }
        }
        mY1 = toFloatBuffer(floats1);

        float[] floats2 = new float[valuesCount * 4 * 2];
        for (int i = 0; i < valuesCount; i++) {
            for (int j = 0; j < 4; j++) {
                if (4 + j < linesCount) {
                    floats2[i * 8 + j] = chartData.lines.get(4 + j).values[i];
                } else {
                    floats2[i * 8 + j] = 0f;
                }
            }
        }
        mY2 = toFloatBuffer(floats2);

        mTop = ByteBuffer
                .allocateDirect(valuesCount * BYTES_PER_FLOAT * 2)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        float[] isTop = new float[valuesCount * 2];
        for (int i = 0; i < valuesCount * 2; i++) {
            isTop[i] = i % 2;
        }
        mTop.put(isTop);
        mTop.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 3, 0f, 0.0f, 0f, 0f, 1.0f, 0.0f);

        final String vertexShader = DataParser.readAsset(context, "shaders/vertex_shader.glsl");
        final String fragmentShader = DataParser.readAsset(context, "shaders/fragment_shader.glsl");

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
        mLinesCountHandle = GLES20.glGetUniformLocation(programHandle, "u_LinesCount");
        mLineIndexHandle = GLES20.glGetUniformLocation(programHandle, "u_LineIndex");
        mA1Handle = GLES20.glGetUniformLocation(programHandle, "u_A1");
        mA2Handle = GLES20.glGetUniformLocation(programHandle, "u_A2");
        mXHandle = GLES20.glGetAttribLocation(programHandle, "a_X");
        mY1Handle = GLES20.glGetAttribLocation(programHandle, "a_Y1");
        mY2Handle = GLES20.glGetAttribLocation(programHandle, "a_Y2");
        mTopHandle = GLES20.glGetAttribLocation(programHandle, "a_Top");
        mColorHandle = GLES20.glGetUniformLocation(programHandle, "u_Color");

        GLES20.glUseProgram(programHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.frustumM(mProjectionMatrix, 0, -1, 1, 0f, 1f, 3f, 3.000001f);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        long l = SystemClock.elapsedRealtime();

        final int color = isNight ? 0xFF242f3e : Color.WHITE;
        GLES20.glClearColor(Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, 1f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUniform1f(mLinesCountHandle, linesCount);
        GLES20.glUniform4f(mA1Handle, alpha[0], alpha[1], alpha[2], alpha[3]);
        GLES20.glUniform4f(mA2Handle, alpha[4], alpha[5], alpha[6], alpha[7]);

        drawChart(startIndex, endIndex, 1 - mainHeight, mainHeight);
        drawChart(0, chartData.xValues.length - 1, 0, periodSelectorHeight);

//        LogUtils.d("SystemClock.elapsedRealtime()-l " + (SystemClock.elapsedRealtime() - l));
    }

    private void drawChart(int startIndex, int endIndex, float dY, float sY) {
        Matrix.setIdentityM(mTranslateXMatrix, 0);
        Matrix.setIdentityM(mScaleXMatrix, 0);
        Matrix.setIdentityM(mTranslateYMatrix, 0);
        Matrix.setIdentityM(mScaleYMatrix, 0);

        float centerX = (chartData.xValues[startIndex] + chartData.xValues[endIndex - 1]) / 2f;
        float dX = chartData.xValues[endIndex - 1] - chartData.xValues[startIndex];

        Matrix.translateM(mTranslateXMatrix, 0, -centerX, 0, 0.0f);
        Matrix.scaleM(mScaleXMatrix, 0, 1f / dX * 2f, 1, 1f);
        Matrix.translateM(mTranslateYMatrix, 0, 0, dY, 0.0f);
        Matrix.scaleM(mScaleYMatrix, 0, 1, sY, 1f);

        Matrix.multiplyMM(mMVPMatrix, 0, mScaleXMatrix, 0, mTranslateXMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mScaleYMatrix, 0, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mTranslateYMatrix, 0, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        for (int i = 0; i < linesCount; i++) {
            drawValues(i);
        }
    }

    private void drawValues(int index) {
        int color = chartData.lines.get(index).color;
        GLES20.glUniform4f(mColorHandle, Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, 1f);
        GLES20.glUniform1f(mLineIndexHandle, index);

        GLES20.glEnableVertexAttribArray(mXHandle);
        GLES20.glEnableVertexAttribArray(mY1Handle);
        GLES20.glEnableVertexAttribArray(mY2Handle);
        GLES20.glEnableVertexAttribArray(mTopHandle);

        mX.position(0);
        mY1.position(0);
        mY2.position(0);
        mTop.position(0);

        GLES20.glVertexAttribPointer(mXHandle, 1, GLES20.GL_FLOAT, false, BYTES_PER_FLOAT, mX);
        GLES20.glVertexAttribPointer(mY1Handle, 4, GLES20.GL_FLOAT, false, 4 * BYTES_PER_FLOAT, mY1);
        GLES20.glVertexAttribPointer(mY2Handle, 4, GLES20.GL_FLOAT, false, 4 * BYTES_PER_FLOAT, mY2);
        GLES20.glVertexAttribPointer(mTopHandle, 1, GLES20.GL_FLOAT, false, BYTES_PER_FLOAT, mTop);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, valuesCount * 2);

        GLES20.glDisableVertexAttribArray(mXHandle);
        GLES20.glDisableVertexAttribArray(mY1Handle);
        GLES20.glDisableVertexAttribArray(mY2Handle);
        GLES20.glDisableVertexAttribArray(mTopHandle);
    }
}