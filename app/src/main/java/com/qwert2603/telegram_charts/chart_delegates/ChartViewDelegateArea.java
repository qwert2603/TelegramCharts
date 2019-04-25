package com.qwert2603.telegram_charts.chart_delegates;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.ViewGroup;

import com.qwert2603.telegram_charts.entity.ChartData;
import com.qwert2603.telegram_charts.q_gl.h.AreaGLSurfaceView;

public class ChartViewDelegateArea extends ChartViewDelegateLines {

    private AreaGLSurfaceView areaGLSurfaceView;

    public ChartViewDelegateArea(Context context, String title, ChartData chartData, Callbacks callbacks) {
        super(context, title, chartData, callbacks);

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
    float getChartTitleHeight() {
        return dp12 * 6;
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
        if (areaGLSurfaceView == null) {
            areaGLSurfaceView = new AreaGLSurfaceView(context, chartData);
            int height = (int) (chartHeight + datesHeight + periodSelectorHeight);
            ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            final int marginHor = (int) this.chartPadding;
            final int marginTop = (int) getChartTitleHeight();
            layoutParams.setMargins(marginHor, marginTop, marginHor, 0);
            callbacks.addView(areaGLSurfaceView, layoutParams);

            areaGLSurfaceView.setChartsSizes(chartHeight, datesHeight, periodSelectorHeight);
            areaGLSurfaceView.onResume();
        }

        areaGLSurfaceView.setPeriodIndices(startIndex, endIndex);
        for (int c = 0; c < chartData.lines.size(); c++) {
            final ChartData.Line line = chartData.lines.get(c);
            areaGLSurfaceView.setAlpha(c, line.alpha / 255f);
        }
        areaGLSurfaceView.requestRender();
    }

    @Override
    protected void drawSelectionOnChart(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();
        final float titleHeight = getChartTitleHeight();

        final float wid = (maxX - minX) / drawingWidth;

        if (selectedIndex >= 0 && chartData.isAnyLineVisible()) {

            final float _x = chartPadding + (chartData.xValues[selectedIndex] - minX) / wid;

            if (0 < _x && _x < callbacks.getWidth()) {
                canvas.drawLine(_x, titleHeight, _x, titleHeight + chartHeight, selectedXLinePaint);
            }
        }
    }

    @Override
    protected void drawPeriodSelectorChart(Canvas canvas) {
    }
}
