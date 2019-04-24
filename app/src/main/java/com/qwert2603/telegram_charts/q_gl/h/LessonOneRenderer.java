package com.qwert2603.telegram_charts.q_gl.h;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES31;
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

public class LessonOneRenderer implements GLSurfaceView.Renderer {

    private float[] mTranslateMatrix = new float[16];
    private float[] mScaleMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private FloatBuffer mY1;
    private FloatBuffer mY2;
    private FloatBuffer mX;
    private FloatBuffer mTop;
    private float[] alpha = {1, 1, 1, 1, 1, 1, 1, 1};

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
    private static final int FLOATS_PER_VERTEX = 2;
    private static final int VERTICES_PER_TRIANGLE = 3;
    private static final int TRIANGLES_PER_AREA = 2;

    private final int valuesCount;
    private Context context;
    private ChartData chartData;
    private final int linesCount;

    public void setAlpha(int line, float alpha) {
        this.alpha[line] = alpha;
    }

    public float getAlpha(int line) {
        return alpha[line];
    }

    private static FloatBuffer toFloatBuffer(long[] longs) {
        float[] floats = new float[longs.length];
        for (int i = 0; i < longs.length; i++) {
            floats[i] = longs[i];
        }
        return toFloatBuffer(floats);
    }

    private static FloatBuffer toFloatBuffer(int[] ints) {
        float[] floats = new float[ints.length];
        for (int i = 0; i < ints.length; i++) {
            floats[i] = ints[i];
        }
        return toFloatBuffer(floats);
    }

    private static FloatBuffer toFloatBuffer(float[] floats) {
        float[] twiced = new float[2 * floats.length];
        for (int i = 0; i < twiced.length; i++) {
            twiced[i] = floats[i / 2];
        }

        FloatBuffer result = ByteBuffer
                .allocateDirect(twiced.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        result.put(twiced);
        result.position(0);

        return result;
    }

    public LessonOneRenderer(Context context, ChartData chartData) {
        this.context = context;
        this.chartData = chartData;
        valuesCount = chartData.xValues.length;
        linesCount = chartData.lines.size();

        mX = toFloatBuffer(chartData.xValues);

        float[] floats = new float[valuesCount * 4];
        for (int i = 0; i < valuesCount; i++) {
            for (int j = 0; j < 4; j++) {
                floats[i * 4 + j] = chartData.lines.get(j).values[i];
            }
        }
        mY1 = toFloatBuffer(floats);

        floats = new float[valuesCount * 4];
        for (int i = 0; i < valuesCount; i++) {
            for (int j = 0; j < 4; j++) {
                if (4 + j < chartData.lines.size()) {
                    floats[i * 4 + j] = chartData.lines.get(4 + j).values[i];
                } else {
                    floats[i * 4 + j] = 0;
                }
            }
        }
        mY2 = toFloatBuffer(floats);

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
        GLES31.glClearColor(1f, 1f, 1f, 1f);

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 3, 0f, 0.0f, 0f, 0f, 1.0f, 0.0f);

        final String vertexShader = DataParser.readAsset(context, "shaders/vertex_shader.glsl");
        final String fragmentShader = DataParser.readAsset(context, "shaders/fragment_shader.glsl");

        int vertexShaderHandle = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER);
        GLES31.glShaderSource(vertexShaderHandle, vertexShader);
        GLES31.glCompileShader(vertexShaderHandle);

        int fragmentShaderHandle = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);
        GLES31.glShaderSource(fragmentShaderHandle, fragmentShader);
        GLES31.glCompileShader(fragmentShaderHandle);

        int programHandle = GLES31.glCreateProgram();
        GLES31.glAttachShader(programHandle, vertexShaderHandle);
        GLES31.glAttachShader(programHandle, fragmentShaderHandle);

        GLES31.glLinkProgram(programHandle);

        mMVPMatrixHandle = GLES31.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mLinesCountHandle = GLES31.glGetUniformLocation(programHandle, "u_LinesCount");
        mLineIndexHandle = GLES31.glGetUniformLocation(programHandle, "u_LineIndex");
        mA1Handle = GLES31.glGetUniformLocation(programHandle, "u_A1");
        mA2Handle = GLES31.glGetUniformLocation(programHandle, "u_A2");
        mXHandle = GLES31.glGetAttribLocation(programHandle, "a_X");
        mY1Handle = GLES31.glGetAttribLocation(programHandle, "a_Y1");
        mY2Handle = GLES31.glGetAttribLocation(programHandle, "a_Y2");
        mTopHandle = GLES31.glGetAttribLocation(programHandle, "a_Top");
        mColorHandle = GLES31.glGetUniformLocation(programHandle, "u_Color");

        GLES31.glUseProgram(programHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES31.glViewport(0, 0, width, height);

//        final float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -1f, 1f, 3f, 3.000001f);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {

        long l = SystemClock.elapsedRealtime();

        GLES31.glClear(GLES31.GL_DEPTH_BUFFER_BIT | GLES31.GL_COLOR_BUFFER_BIT);

        Matrix.setIdentityM(mTranslateMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);

        float centerX = (chartData.xValues[0] + chartData.xValues[valuesCount - 1]) / 2f;
        float centerY = 0.5f;
        float dX = chartData.xValues[valuesCount - 1] - chartData.xValues[0];

        Matrix.translateM(mTranslateMatrix, 0, -centerX, -centerY, 0.0f);
        float a = 0.99f;
        Matrix.scaleM(mScaleMatrix, 0, 1f / dX * 2f * a, 1f / centerY * a, 1f);

        Matrix.multiplyMM(mMVPMatrix, 0, mScaleMatrix, 0, mTranslateMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES31.glUniform1i(mLinesCountHandle, linesCount);
        GLES31.glUniform4f(mA1Handle, alpha[0], alpha[1], alpha[2], alpha[3]);
        GLES31.glUniform4f(mA2Handle, alpha[4], alpha[5], alpha[6], alpha[7]);

        for (int i = 0; i < linesCount; i++) {
            drawValues(i);
        }

//        LogUtils.d("SystemClock.elapsedRealtime()-l " + (SystemClock.elapsedRealtime() - l));
    }

    private void drawValues(int index) {
        int color = chartData.lines.get(index).color;
        GLES31.glUniform4f(mColorHandle, Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, 1f);
        GLES31.glUniform1i(mLineIndexHandle, index);

        GLES31.glEnableVertexAttribArray(mXHandle);
        GLES31.glEnableVertexAttribArray(mY1Handle);
        GLES31.glEnableVertexAttribArray(mY2Handle);
        GLES31.glEnableVertexAttribArray(mTopHandle);

        mX.position(0);
        mY1.position(0);
        mY2.position(0);
        mTop.position(0);

        GLES31.glVertexAttribPointer(mXHandle, 1, GLES31.GL_FLOAT, false, BYTES_PER_FLOAT, mX);
        GLES31.glVertexAttribPointer(mY1Handle, 4, GLES31.GL_FLOAT, false, 4 * BYTES_PER_FLOAT, mY1);
        GLES31.glVertexAttribPointer(mY2Handle, 4, GLES31.GL_FLOAT, false, 4 * BYTES_PER_FLOAT, mY2);
        GLES31.glVertexAttribPointer(mTopHandle, 1, GLES31.GL_FLOAT, false, BYTES_PER_FLOAT, mTop);

        GLES31.glDrawArrays(GLES31.GL_TRIANGLE_STRIP, 0, valuesCount * 2);

        GLES31.glDisableVertexAttribArray(mXHandle);
        GLES31.glDisableVertexAttribArray(mY1Handle);
        GLES31.glDisableVertexAttribArray(mY2Handle);
        GLES31.glDisableVertexAttribArray(mTopHandle);
    }

}