package com.qwert2603.telegram_charts;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.qwert2603.telegram_charts.entity.ChartData;

public class ChartView extends View {

    public ChartView(Context context, ChartData chartData) {
        super(context);

        linesPaint = new Paint();
        linesPaint.setAntiAlias(true);
//        linesPaint.setStrokeJoin(Paint.Join.ROUND);
        linesPaint.setStrokeCap(Paint.Cap.ROUND);
        linesPaint.setStyle(Paint.Style.STROKE);

        this.setLayerType(LAYER_TYPE_HARDWARE, null);

        this.chartData = chartData;
        points = new float[chartData.xValues.length * 4];

        setPeriodIndices(0, chartData.xValues.length);

        totalMinX = minX;
        totalMaxX = maxX;

        int[] yLimits = chartData.calcYLimits(startIndex, endIndex);
        minY = yLimits[0];
        maxY = yLimits[1];
        totalMinY = yLimits[2];
        totalMaxY = yLimits[3];
    }

    private final float chartHeight = getResources().getDimension(R.dimen.chart_height);
    private final float periodSelectorHeight = getResources().getDimension(R.dimen.period_selector_height);
    private final float minPeriodSelectorWidth = getResources().getDimension(R.dimen.min_period_selector_width);
    private final float periodSelectorDraggableWidth = getResources().getDimension(R.dimen.period_selector_draggable_width);
    private final float lineWidth = getResources().getDimension(R.dimen.line_width);
    private final float chartPadding = getResources().getDimension(R.dimen.chart_padding);
    private final float dp2 = getResources().getDimension(R.dimen.dp2);
    private final float dp4 = getResources().getDimension(R.dimen.dp4);
    private final float dp6 = getResources().getDimension(R.dimen.dp6);
    private final float dp8 = getResources().getDimension(R.dimen.dp8);
    private final float dp12 = getResources().getDimension(R.dimen.dp12);

    private final ChartData chartData;
    private final Paint linesPaint;
    private final float[] points;

    private int startIndex;
    private int endIndex;

    private long minX;
    private long maxX;

    private final long totalMinX;
    private final long totalMaxX;

    private int minY;
    private int maxY;

    private int totalMinY;
    private int totalMaxY;

    private ObjectAnimator maxYAnimator;
    private ObjectAnimator totalMaxYAnimator;

    public void setLineVisible(String name, boolean visible) {
        for (ChartData.Line line : chartData.lines) {
            if (line.name.equals(name)) {
                line.isVisible = visible;
                break;
            }
        }

        animateMaxY();
    }

    private void setPeriodIndices(int start, int end) {
        if (start < 0) start = 0;
        if (end > chartData.xValues.length) end = chartData.xValues.length;
        startIndex = start;
        endIndex = end;

        minX = chartData.xValues[startIndex];
        maxX = chartData.xValues[endIndex - 1];

        animateMaxY();
    }

    private void animateMaxY() {
        if (maxYAnimator != null) maxYAnimator.cancel();
        if (totalMaxYAnimator != null) totalMaxYAnimator.cancel();

        int[] yLimits = chartData.calcYLimits(startIndex, endIndex);

        ObjectAnimator animator = ObjectAnimator
                .ofInt(this, "maxY", yLimits[1])
                .setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        maxYAnimator = animator;
        maxYAnimator.start();

        ObjectAnimator animator1 = ObjectAnimator
                .ofInt(this, "totalMaxY", yLimits[3])
                .setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        totalMaxYAnimator = animator1;
        totalMaxYAnimator.start();
    }

    private static final int DRAG_SELECTOR = 1;
    private static final int DRAG_START = 2;
    private static final int DRAG_END = 3;
    private int dragPointerId = -1;
    private int currentDrag = 0;
    private float selectorDragCenterOffset = 0f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int pointerId = event.getPointerId(event.getActionIndex());

        float selectorCenter = (endIndex + startIndex - 1) / 2f / chartData.xValues.length * getWidth();
        float selectorWidth = (endIndex - startIndex) * 1f / chartData.xValues.length * getWidth();
        float x = event.getX();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getY() > chartHeight) {
                    if (Math.abs(selectorCenter - selectorWidth / 2 - x) < periodSelectorDraggableWidth) {
                        dragPointerId = pointerId;
                        currentDrag = DRAG_START;
                    } else if (Math.abs(selectorCenter + selectorWidth / 2 - x) < periodSelectorDraggableWidth) {
                        dragPointerId = pointerId;
                        currentDrag = DRAG_END;
                    } else if (Math.abs(selectorCenter - x) < (selectorWidth - periodSelectorDraggableWidth) / 2) {
                        dragPointerId = pointerId;
                        currentDrag = DRAG_SELECTOR;
                        selectorDragCenterOffset = selectorCenter - x;
                        movePeriodSelectorTo(x + selectorDragCenterOffset, selectorWidth);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointerId == dragPointerId) {
                    switch (currentDrag) {
                        case DRAG_START:
                            if (x > selectorCenter - minPeriodSelectorWidth / 2) {
                                x = selectorCenter - minPeriodSelectorWidth / 2;
                            }
                            int newStartIndex = (int) (x / getWidth() * chartData.xValues.length);
                            setPeriodIndices(newStartIndex, endIndex);
                            break;
                        case DRAG_END:
                            if (x < selectorCenter + minPeriodSelectorWidth / 2) {
                                x = selectorCenter + minPeriodSelectorWidth / 2;
                            }
                            int newEndIndex = (int) (x / getWidth() * chartData.xValues.length);
                            setPeriodIndices(startIndex, newEndIndex);
                            break;
                        case DRAG_SELECTOR:
                            movePeriodSelectorTo(x + selectorDragCenterOffset, selectorWidth);
                            break;
                    }
                }
                break;
            default:
                if (pointerId == dragPointerId) {
                    currentDrag = 0;
                    dragPointerId = -1;
                }
                break;
        }

        return true;
    }

    private void movePeriodSelectorTo(float x, float selectorWidth) {
        float newMin = x - selectorWidth / 2;
        float newMax = x + selectorWidth / 2;
        if (newMin < 0) {
            newMin = 0;
            newMax = selectorWidth;
        }
        if (newMax > getWidth()) {
            newMin = getWidth() - selectorWidth;
            newMax = getWidth();
        }

        int newStartIndex = (int) (newMin / getWidth() * chartData.xValues.length);
        int newEndIndex = (int) (newMax / getWidth() * chartData.xValues.length);
        setPeriodIndices(newStartIndex, newEndIndex);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float drawingWidth = getWidth() - 2 * chartPadding;

        for (int c = 0; c < chartData.lines.size(); c++) {
            ChartData.Line line = chartData.lines.get(c);
            if (line.isVisible) {
                linesPaint.setColor(line.color);

                int q = 0;
                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _x = ((float) chartData.xValues[i] - minX) / (maxX - minX) * drawingWidth;
                    final float _y = (1 - ((float) line.values[i] - minY) / (maxY - minY)) * chartHeight;

                    points[q++] = _x;
                    points[q++] = _y;
                    if (i != 0 && i != chartData.xValues.length - 1) {
                        points[q++] = _x;
                        points[q++] = _y;
                    }
                }

                linesPaint.setStrokeWidth(lineWidth);

                canvas.save();
                canvas.translate(chartPadding, 0);
                canvas.drawLines(points, linesPaint);
                canvas.restore();

                q = 0;
                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _x = ((float) chartData.xValues[i] - totalMinX) / (totalMaxX - totalMinX) * drawingWidth;
                    final float _y = chartHeight + (1 - ((float) line.values[i] - totalMinY) / (totalMaxY - totalMinY)) * periodSelectorHeight;

                    points[q++] = _x;
                    points[q++] = _y;
                    if (i != 0 && i != chartData.xValues.length - 1) {
                        points[q++] = _x;
                        points[q++] = _y;
                    }
                }

                linesPaint.setStrokeWidth(lineWidth / 2f);

                canvas.save();
                canvas.translate(chartPadding, 0);
                canvas.drawLines(points, linesPaint);
                canvas.restore();
            }
        }

        canvas.save();
        canvas.translate(chartPadding, 0);

        float startX = startIndex * 1f / chartData.xValues.length * drawingWidth;
        float endX = endIndex * 1f / chartData.xValues.length * drawingWidth;

        Paint periodPaint = new Paint();
        periodPaint.setStyle(Paint.Style.FILL);
        periodPaint.setColor(0x18000000);

        canvas.drawRect(0, chartHeight, startX, chartHeight + periodSelectorHeight, periodPaint);
        canvas.drawRect(endX, chartHeight, drawingWidth, chartHeight + periodSelectorHeight, periodPaint);

        periodPaint.setColor(0x88000000);

        final float borderHor = dp2;
        final float borderVer = dp12;

        canvas.drawRect(startX + borderVer, chartHeight, endX - borderVer, chartHeight + borderHor, periodPaint);
        canvas.drawRect(startX + borderVer, chartHeight + periodSelectorHeight - borderHor, endX - borderVer, chartHeight + periodSelectorHeight, periodPaint);

        canvas.drawRect(startX, chartHeight, startX + borderVer, chartHeight + periodSelectorHeight, periodPaint);
        canvas.drawRect(endX - borderVer, chartHeight, endX, chartHeight + periodSelectorHeight, periodPaint);

        periodPaint.setColor(0xD7FFFFFF);

        canvas.drawRect(
                startX + borderVer / 2 - dp2,
                chartHeight + periodSelectorHeight / 2 - dp8,
                startX + borderVer / 2 + dp2,
                chartHeight + periodSelectorHeight / 2 + dp8,
                periodPaint
        );
        canvas.drawRect(
                endX - borderVer / 2 - dp2,
                chartHeight + periodSelectorHeight / 2 - dp8,
                endX - borderVer / 2 + dp2,
                chartHeight + periodSelectorHeight / 2 + dp8,
                periodPaint
        );

        canvas.restore();
    }

    public long getMinX() {
        return minX;
    }

    public void setMinX(long minX) {
        this.minX = minX;
    }

    public long getMaxX() {
        return maxX;
    }

    public void setMaxX(long maxX) {
        this.maxX = maxX;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
        invalidate();
    }

    public long getTotalMinX() {
        return totalMinX;
    }

    public long getTotalMaxX() {
        return totalMaxX;
    }

    public int getTotalMinY() {
        return totalMinY;
    }

    public void setTotalMinY(int totalMinY) {
        this.totalMinY = totalMinY;
    }

    public int getTotalMaxY() {
        return totalMaxY;
    }

    public void setTotalMaxY(int totalMaxY) {
        this.totalMaxY = totalMaxY;
    }
}
