package com.qwert2603.telegram_charts;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.qwert2603.telegram_charts.entity.ChartData;

public class ChartCustomViewGroup extends FrameLayout {

    private final ChartCustomView chartCustomView;

    public ChartCustomViewGroup(final Context context, String title, ChartData chartData) {
        super(context);

        chartCustomView = new ChartCustomView(context, title, chartData);

        addView(
                chartCustomView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
    }

    public void setNightMode(boolean night) {
        chartCustomView.setNightMode(night);
    }

    public void onResume() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof GLSurfaceView) {
                ((GLSurfaceView) view).onResume();
            }
        }
    }

    public void onPause() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof GLSurfaceView) {
                ((GLSurfaceView) view).onPause();
            }
        }
    }
}
