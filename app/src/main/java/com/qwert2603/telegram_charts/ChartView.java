package com.qwert2603.telegram_charts;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;

public class ChartView extends View {

    private ChartData chartData;
    private Paint paint = new Paint();
    private float[] points;

    public ChartView(Context context) {
        super(context);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setChartData(ChartData chartData) {
        this.chartData = chartData;
        this.points = new float[chartData.xValues.length * 4];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (ChartData.Line line : chartData.lines) {
            for (int y : line.values) {
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
        }

        long maxX = chartData.xValues[chartData.xValues.length - 1];
        long minX = chartData.xValues[0];

        for (ChartData.Line line : chartData.lines) {
            paint.setColor(Color.parseColor(line.color));
            paint.setStrokeWidth(4);
            paint.setAntiAlias(true);

            for (int i = 0; i < chartData.xValues.length - 1; i++) {
                long bx = chartData.xValues[i];
                int by = line.values[i];

                long ex = chartData.xValues[i + 1];
                int ey = line.values[i + 1];

                points[i * 4] = ((float) (bx - minX)) / (maxX - minX) * getWidth();
                points[i * 4 + 1] = getHeight() - ((float) (by - minY)) / (maxY - minY) * getHeight();
                points[i * 4 + 2] = ((float) (ex - minX)) / (maxX - minX) * getWidth();
                points[i * 4 + 3] = getHeight() - ((float) (ey - minY)) / (maxY - minY) * getHeight();
            }

            LogUtils.d(Arrays.toString(points));

            canvas.drawLines(points, paint);
        }
    }
}
