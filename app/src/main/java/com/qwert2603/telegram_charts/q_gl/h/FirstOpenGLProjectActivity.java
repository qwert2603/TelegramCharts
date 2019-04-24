package com.qwert2603.telegram_charts.q_gl.h;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Toast;

import com.qwert2603.telegram_charts.DataParser;
import com.qwert2603.telegram_charts.LogUtils;
import com.qwert2603.telegram_charts.entity.ChartData;

public class FirstOpenGLProjectActivity extends Activity {
    private GLSurfaceView mGLSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        LogUtils.d("configurationInfo.toString() " + configurationInfo.toString());
        LogUtils.d("configurationInfo reqGlEsVersion " + Integer.toHexString(configurationInfo.reqGlEsVersion));
        LogUtils.d("configurationInfo getGlEsVersion " + configurationInfo.getGlEsVersion());
        Toast.makeText(this, configurationInfo.getGlEsVersion(), Toast.LENGTH_LONG).show();

        mGLSurfaceView = new GLSurfaceView(this);

        ChartData chartData = DataParser.parseDataStage2(this).get(4);

        mGLSurfaceView.setEGLContextClientVersion(3);
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