package com.qwert2603.telegram_charts;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import com.qwert2603.telegram_charts.chart_delegates.Callbacks;
import com.qwert2603.telegram_charts.chart_delegates.ChartViewDelegateArea;
import com.qwert2603.telegram_charts.chart_delegates.ChartViewDelegateBars;
import com.qwert2603.telegram_charts.chart_delegates.ChartViewDelegateLines;
import com.qwert2603.telegram_charts.chart_delegates.ChartViewDelegateLines_2Y;
import com.qwert2603.telegram_charts.chart_delegates.Delegate;
import com.qwert2603.telegram_charts.entity.ChartData;

public class ChartCustomView extends View {

    private final Delegate delegate;

    public ChartCustomView(Context context, String title, ChartData chartData) {
        super(context);

        Callbacks callbacks = new Callbacks() {
            @Override
            public void invalidate() {
                ChartCustomView.this.invalidate();
            }

            @Override
            public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                ChartCustomView.this.getParent().requestDisallowInterceptTouchEvent(disallowIntercept);
            }

            @Override
            public float getWidth() {
                return getResources().getDisplayMetrics().widthPixels;
            }
        };
        switch (chartData.type) {
            case BARS:
                delegate = new ChartViewDelegateBars(context, title, chartData, callbacks);
                break;
            case LINES:
                delegate = new ChartViewDelegateLines(context, title, chartData, callbacks);
                break;
            case LINES_2_Y:
                delegate = new ChartViewDelegateLines_2Y(context, title, chartData, callbacks);
                break;
            case AREA:
                delegate = new ChartViewDelegateArea(context, title, chartData, callbacks);
                break;
            default:
                throw new RuntimeException();

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        delegate.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        delegate.onDraw(canvas);
    }

    public int measureHeight() {
        return delegate.measureHeight();
    }
}
