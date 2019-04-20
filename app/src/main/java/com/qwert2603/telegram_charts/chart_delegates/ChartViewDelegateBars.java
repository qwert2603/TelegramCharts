package com.qwert2603.telegram_charts.chart_delegates;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.qwert2603.telegram_charts.entity.ChartData;

public class ChartViewDelegateBars extends ChartViewDelegateLines {

    private final float[] sums;
    private final float[] totalSums;

    private final Paint lightenMaskPaint;

    public ChartViewDelegateBars(Context context, String title, ChartData chartData, Callbacks callbacks) {
        super(context, title, chartData, callbacks);

        sums = new float[chartData.xValues.length];
        totalSums = new float[chartData.xValues.length];

        lightenMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightenMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        for (int i = 0; i < chartData.lines.size(); i++) {
            linesPaints[i].setAntiAlias(false);
            linesPaints[i].setStyle(Paint.Style.FILL_AND_STROKE);
            periodSelectorLinesPaints[i].setAntiAlias(false);
            periodSelectorLinesPaints[i].setStyle(Paint.Style.FILL_AND_STROKE);
        }
    }

    @Override
    boolean isSelectionPanelWithAllString() {
        return chartData.lines.size() > 1;
    }

    @Override
    public void setNightMode(boolean night) {
        super.setNightMode(night);
        lightenMaskPaint.setColor(night ? 0x80242F3E : 0x80FFFFFF);
    }

    @Override
    protected void drawChart(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        final float wid = (maxX - minX) / drawingWidth;
        final float hei = (maxY - minY) / chartHeight;

        final float dX = chartPadding;
        final float dY = getChartTitleHeight() + chartHeight;

        for (int i = 0; i < chartData.xValues.length; i++) {
            sums[i] = 0;
        }

        for (int c = 0; c < chartData.lines.size(); c++) {
            final ChartData.Line line = chartData.lines.get(c);
            if (line.isVisible()) {
                int q = 0;
                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _x = dX + ((float) chartData.xValues[i] - minX) / wid;
                    final float _yBottom = dY - (sums[i] - minY) / hei;
                    sums[i] += line.values[i] * (line.alpha / 255f);
                    final float _yTop = dY - (sums[i] - minY) / hei;

                    if (_x < -dp12 || _x > callbacks.getWidth() + dp12) {
                        continue;
                    }

                    points[q++] = _x;
                    points[q++] = _yTop;
                    points[q++] = _x;
                    points[q++] = _yBottom;
                }

                final Paint paint = linesPaints[c];
                paint.setStrokeWidth(chartData.xStep / wid);
                canvas.drawLines(points, 0, q, paint);
            }
        }
    }

    @Override
    protected void drawSelectionOnChart(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        final float wid = (maxX - minX) / drawingWidth;

        if (selectedIndex >= 0 && chartData.isAnyLineVisible()) {
            canvas.drawRect(
                    chartPadding + ((float) chartData.xValues[0] - minX - chartData.xStep / 2) / wid,
                    -getChartTitleHeight(),
                    chartPadding + ((float) chartData.xValues[selectedIndex] - minX - chartData.xStep / 2) / wid,
                    chartHeight,
                    lightenMaskPaint
            );
            canvas.drawRect(
                    chartPadding + ((float) chartData.xValues[selectedIndex] - minX + chartData.xStep / 2) / wid,
                    -getChartTitleHeight(),
                    chartPadding + ((float) chartData.xValues[chartData.xValues.length - 1] - minX + chartData.xStep / 2) / wid,
                    chartHeight,
                    lightenMaskPaint
            );
        }
    }

    @Override
    protected void drawPeriodSelectorChart(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        final float wid = (totalMaxX - totalMinX) / drawingWidth;
        final float hei = (totalMaxY - totalMinY) / periodSelectorHeight;

        for (int i = 0; i < chartData.xValues.length; i++) {
            totalSums[i] = 0;
        }

        canvas.save();
        canvas.clipPath(periodSelectorClipPath);

        for (int c = 0; c < chartData.lines.size(); c++) {
            final ChartData.Line line = chartData.lines.get(c);
            if (line.isVisible()) {
                int q = 0;
                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _x = ((float) chartData.xValues[i] - totalMinX) / wid;
                    final float _yBottom = periodSelectorHeight - (totalSums[i] - totalMinY) / hei;
                    totalSums[i] += line.values[i] * (line.alpha / 255f);
                    final float _yTop = periodSelectorHeight - (totalSums[i] - totalMinY) / hei;

                    points[q++] = _x;
                    points[q++] = _yTop;
                    points[q++] = _x;
                    points[q++] = _yBottom;
                }

                final Paint paint = periodSelectorLinesPaints[c];
                paint.setStrokeWidth(chartData.xStep / wid);
                canvas.drawLines(points, 0, q, paint);
            }
        }

        canvas.restore();
    }
}
