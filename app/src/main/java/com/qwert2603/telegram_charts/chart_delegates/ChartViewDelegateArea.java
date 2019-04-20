package com.qwert2603.telegram_charts.chart_delegates;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.qwert2603.telegram_charts.entity.ChartData;

public class ChartViewDelegateArea extends ChartViewDelegateLines {

    private final float[] sums;
    private final float[] totalSums;
    private final float[] allLinesSums;

    private final Path barsPath = new Path();

    public ChartViewDelegateArea(Context context, String title, ChartData chartData, Callbacks callbacks) {
        super(context, title, chartData, callbacks);

        sums = new float[chartData.xValues.length];
        totalSums = new float[chartData.xValues.length];
        allLinesSums = new float[chartData.xValues.length];

        for (int i = 0; i < chartData.lines.size(); i++) {
            linesPaints[i].setAntiAlias(false);
            linesPaints[i].setStyle(Paint.Style.FILL);
            periodSelectorLinesPaints[i].setAntiAlias(false);
            periodSelectorLinesPaints[i].setStyle(Paint.Style.FILL);
        }

        selectedXLinePaint.setStrokeWidth(lineWidth);
    }

    @Override
    boolean isSelectionPanelWithPercents() {
        return true;
    }

    @Override
    float getPanelMarginTop() {
        return dp12;
    }

    @Override
    float getYLinesCompressFactor() {
        return 1f;
    }

    @Override
    protected void drawChart(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        final float wid = (maxX - minX) / drawingWidth;
        final float dX = chartPadding;
        final float dY = chartTitleHeight + chartHeight;

        for (int i = 0; i < chartData.xValues.length; i++) {
            sums[i] = 0;
            allLinesSums[i] = 0;
        }
        for (int c = 0; c < chartData.lines.size(); c++) {
            final ChartData.Line line = chartData.lines.get(c);
            if (line.isVisible()) {
                for (int i = 0; i < chartData.xValues.length; i++) {
                    allLinesSums[i] += line.values[i] * (line.alpha / 255f);
                }
            }
        }

        for (int c = 0; c < chartData.lines.size(); c++) {
            final ChartData.Line line = chartData.lines.get(c);
            if (line.isVisible()) {
                int q = 0;
                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _x = dX + ((float) chartData.xValues[i] - minX) / wid;
                    final float _yBottom = dY - (sums[i] / allLinesSums[i]) * chartHeight;
                    sums[i] += line.values[i] * (line.alpha / 255f);
                    final float _yTop = dY - (sums[i] / allLinesSums[i]) * chartHeight;

                    if (_x < -dp12 || _x > callbacks.getWidth() + dp12) {
                        continue;
                    }

                    points[q++] = _x;
                    points[q++] = _yTop;
                    points[q++] = _yBottom;
                }

                barsPath.moveTo(points[0], chartHeight);
                for (int i = 0; i < q / 3; i++) {
                    barsPath.lineTo(points[i * 3], points[i * 3 + 1]);

                }
                for (int i = q / 3 - 1; i >= 0; i--) {
                    barsPath.lineTo(points[i * 3], points[i * 3 + 2]);
                }
                barsPath.close();
                canvas.drawPath(barsPath, linesPaints[c]);
                barsPath.rewind();
            }
        }
    }

    @Override
    protected void drawSelectionOnChart(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        final float wid = (maxX - minX) / drawingWidth;

        if (selectedIndex >= 0 && chartData.isAnyLineVisible()) {

            final float _x = chartPadding + (chartData.xValues[selectedIndex] - minX) / wid;

            if (0 < _x && _x < callbacks.getWidth()) {
                canvas.drawLine(_x, 0, _x, chartHeight, selectedXLinePaint);
            }
        }
    }

    @Override
    protected void drawPeriodSelectorChart(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        final float wid = (totalMaxX - totalMinX) / drawingWidth;

        for (int i = 0; i < chartData.xValues.length; i++) {
            totalSums[i] = 0;
        }
        // allLinesSums are calculated in drawChart().

        canvas.save();
        canvas.clipPath(periodSelectorClipPath);

        for (int c = 0; c < chartData.lines.size(); c++) {
            final ChartData.Line line = chartData.lines.get(c);
            if (line.isVisible()) {
                int q = 0;

                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _x = ((float) chartData.xValues[i] - totalMinX) / wid;
                    final float _yBottom = periodSelectorHeight - (totalSums[i] / allLinesSums[i]) * periodSelectorHeight;
                    totalSums[i] += line.values[i] * (line.alpha / 255f);
                    final float _yTop = periodSelectorHeight - (totalSums[i] / allLinesSums[i]) * periodSelectorHeight;

                    points[q++] = _x;
                    points[q++] = _yTop;
                    points[q++] = _yBottom;
                }

                barsPath.moveTo(points[0], chartHeight);
                for (int i = 0; i < q / 3; i++) {
                    barsPath.lineTo(points[i * 3], points[i * 3 + 1]);

                }
                for (int i = q / 3 - 1; i >= 0; i--) {
                    barsPath.lineTo(points[i * 3], points[i * 3 + 2]);
                }
                barsPath.close();
                canvas.drawPath(barsPath, linesPaints[c]);
                barsPath.rewind();
            }
        }

        canvas.restore();
    }
}
