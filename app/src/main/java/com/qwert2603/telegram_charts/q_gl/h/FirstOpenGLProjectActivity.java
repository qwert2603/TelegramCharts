package com.qwert2603.telegram_charts.q_gl.h;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.qwert2603.telegram_charts.LogUtils;
import com.qwert2603.telegram_charts.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirstOpenGLProjectActivity extends Activity {
    private AreaGLSurfaceView mGLSurfaceView;

    ExecutorService[] animators = new ExecutorService[8];
    boolean[] down = new boolean[8];
    float[] a = new float[8];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        LogUtils.d("configurationInfo.toString() " + configurationInfo.toString());
        LogUtils.d("configurationInfo reqGlEsVersion " + Integer.toHexString(configurationInfo.reqGlEsVersion));
        LogUtils.d("configurationInfo getGlEsVersion " + configurationInfo.getGlEsVersion());
        Toast.makeText(this, configurationInfo.getGlEsVersion(), Toast.LENGTH_LONG).show();

        setContentView(R.layout.activity_gl);

        mGLSurfaceView = findViewById(R.id.areaGLSurfaceView);

        findViewById(R.id.line0_Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateLine(0);
            }
        });
        findViewById(R.id.line1_Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateLine(1);
            }
        });
        findViewById(R.id.line2_Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateLine(2);
            }
        });
        findViewById(R.id.line3_Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateLine(3);
            }
        });
        findViewById(R.id.line4_Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateLine(4);
            }
        });
        findViewById(R.id.line5_Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateLine(5);
            }
        });
    }

    private void animateLine(final int line) {
        ExecutorService animator = animators[line];
        if (animator == null) {
            animator = Executors.newSingleThreadExecutor();
            animators[line] = animator;
            a[line] = 1;
            animator.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            if (down[line]) {
                                a[line] -= 0.05;
                                if (a[line] < 0) a[line] = 0;
                            } else {
                                a[line] += 0.05;
                                if (a[line] > 1) a[line] = 1;
                            }

                            mGLSurfaceView.setAlpha(line, a[line]);

                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        down[line] = !down[line];
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