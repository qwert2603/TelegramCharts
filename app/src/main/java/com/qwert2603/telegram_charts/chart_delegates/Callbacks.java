package com.qwert2603.telegram_charts.chart_delegates;

import android.view.View;
import android.view.ViewGroup;

public interface Callbacks {
    void invalidate();

    void requestDisallowInterceptTouchEvent(boolean disallowIntercept);

    float getWidth();

    void addView(View view, ViewGroup.LayoutParams layoutParams);
}