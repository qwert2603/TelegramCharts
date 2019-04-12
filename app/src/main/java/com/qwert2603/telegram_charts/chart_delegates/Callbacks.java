package com.qwert2603.telegram_charts.chart_delegates;

public interface Callbacks {
    void invalidate();

    void requestDisallowInterceptTouchEvent(boolean disallowIntercept);

    float getWidth();
}