package com.qwert2603.telegram_charts.q_gl.h;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.qwert2603.telegram_charts.DataParser;
import com.qwert2603.telegram_charts.entity.ChartData;

public class FirstOpenGLProjectActivity extends Activity {
    private GLSurfaceView mGLSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurfaceView = new GLSurfaceView(this);

        ChartData chartData = DataParser.parseDataStage2(this).get(4);

        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(new LessonOneRenderer(this, chartData));

        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }
}