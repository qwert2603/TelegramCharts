package com.qwert2603.telegram_charts.q_gl.h;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.qwert2603.telegram_charts.LogUtils;
import com.qwert2603.telegram_charts.R;

public class FirstOpenGLProjectActivity extends Activity {
    private AreaGLSurfaceView mGLSurfaceView;

    ValueAnimator[] animators = new ValueAnimator[8];
    boolean[] up = new boolean[8];

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
        ValueAnimator animator = animators[line];
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator
                .ofFloat(mGLSurfaceView.getAlpha(line), up[line] ? 0 : 1)
                .setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mGLSurfaceView.setAlpha(line, (float) animation.getAnimatedValue());
            }
        });
        animators[line] = animator;
        up[line] = !up[line];
        animator.start();
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