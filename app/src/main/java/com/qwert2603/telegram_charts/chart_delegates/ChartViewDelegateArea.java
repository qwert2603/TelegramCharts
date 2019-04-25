package com.qwert2603.telegram_charts.chart_delegates;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.ViewGroup;

import com.qwert2603.telegram_charts.entity.ChartData;
import com.qwert2603.telegram_charts.gl.GLSurfaceViewArea;

public class ChartViewDelegateArea extends ChartViewDelegateLines {

    private GLSurfaceViewArea mGLSurfaceViewArea;

    private final Path periodSelectorOutClipPath = new Path();
    private final Paint periodSelectorOutClipPaint;

    public ChartViewDelegateArea(Context context, String title, ChartData chartData, Callbacks callbacks) {
        super(context, title, chartData, callbacks);

        for (int i = 0; i < chartData.lines.size(); i++) {
            linesPaints[i].setAntiAlias(false);
            linesPaints[i].setStyle(Paint.Style.FILL);
            periodSelectorLinesPaints[i].setAntiAlias(false);
            periodSelectorLinesPaints[i].setStyle(Paint.Style.FILL);
        }

        selectedXLinePaint.setStrokeWidth(lineWidth);

        periodSelectorOutClipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        periodSelectorOutClipPaint.setStyle(Paint.Style.FILL);
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
    public void setNightMode(boolean night) {
        super.setNightMode(night);
        if (mGLSurfaceViewArea != null) mGLSurfaceViewArea.setNight(night);
        periodSelectorOutClipPaint.setColor(night ? 0xFF242f3e : Color.WHITE);
    }

    @Override
    public int measureHeight(int width) {
        float drawingWidth = width - 2 * chartPadding;

        periodSelectorOutClipPath.rewind();
        periodSelectorOutClipPath.addRoundRect(
                0,
                0,
                drawingWidth,
                periodSelectorHeight,
                new float[]{psFillRadius, psFillRadius, psFillRadius, psFillRadius, psFillRadius, psFillRadius, psFillRadius, psFillRadius},
                Path.Direction.CW
        );
        periodSelectorOutClipPath.addRect(
                0,
                0,
                drawingWidth,
                periodSelectorHeight,
                Path.Direction.CW
        );
        periodSelectorOutClipPath.setFillType(Path.FillType.EVEN_ODD);


        return super.measureHeight(width);
    }

    @Override
    protected void drawChart(Canvas canvas) {
        if (mGLSurfaceViewArea == null) {
            mGLSurfaceViewArea = new GLSurfaceViewArea(context, chartData);
            int height = (int) (chartHeight + datesHeight + periodSelectorHeight);
            ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            final int marginHor = (int) this.chartPadding;
            final int marginTop = (int) getChartTitleHeight();
            layoutParams.setMargins(marginHor, marginTop, marginHor, 0);
            callbacks.addView(mGLSurfaceViewArea, layoutParams);

            mGLSurfaceViewArea.setNight(night);
            mGLSurfaceViewArea.setChartsSizes(chartHeight, datesHeight, periodSelectorHeight);
            mGLSurfaceViewArea.onResume();
        }

        mGLSurfaceViewArea.setPeriodIndices(startIndex, endIndex);
        for (int c = 0; c < chartData.lines.size(); c++) {
            final ChartData.Line line = chartData.lines.get(c);
            mGLSurfaceViewArea.setAlpha(c, line.alpha / 255f);
        }
        mGLSurfaceViewArea.requestRender();
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
        canvas.drawPath(periodSelectorOutClipPath, periodSelectorOutClipPaint);
    }
}
