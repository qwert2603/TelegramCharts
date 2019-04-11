package com.qwert2603.telegram_charts;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import com.qwert2603.telegram_charts.entity.ChartData;

public class ChartCustomView extends View {

    private final ChartViewDelegate chartViewDelegate;

    public ChartCustomView(Context context, String title, ChartData chartData) {
        super(context);

        chartViewDelegate = new ChartViewDelegate(context, title, chartData, new ChartViewDelegate.Callbacks() {
            @Override
            public void invalidate() {
                ChartCustomView.this.invalidate();
            }

            @Override
            public void requestDisallowInterceptTouchEvent() {
                ChartCustomView.this.getParent().requestDisallowInterceptTouchEvent(true);
            }

            @Override
            public float getWidth() {
                return ChartCustomView.this.getWidth();
            }
        });
    }

    public void setLineVisible(String name, boolean visible) {
        chartViewDelegate.setLineVisible(name, visible);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        chartViewDelegate.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        chartViewDelegate.onDraw(canvas);
    }
}
