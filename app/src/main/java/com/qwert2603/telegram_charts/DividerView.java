package com.qwert2603.telegram_charts;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public class DividerView extends View {
    public DividerView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(MainActivity.NIGHT_MODE ? 0xFF1b2433 : 0xFFf0f0f0);
    }
}
