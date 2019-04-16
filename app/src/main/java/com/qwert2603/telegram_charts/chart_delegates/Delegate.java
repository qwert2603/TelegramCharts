package com.qwert2603.telegram_charts.chart_delegates;

import android.graphics.Canvas;
import android.view.MotionEvent;

public interface Delegate {
    boolean onTouchEvent(MotionEvent event);
    void onDraw(Canvas canvas);
    int measureHeight(int width);
}
