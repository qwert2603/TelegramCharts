package com.qwert2603.telegram_charts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.qwert2603.telegram_charts.entity.ChartData;

import java.util.HashMap;
import java.util.Map;

public class ChartView extends View {

    private static final long ANIMATION_DURATION = 170L;

    public ChartView(Context context, String title, ChartData chartData) {
        super(context);

        this.title = title;

        linesPaint = new Paint();
        linesPaint.setAntiAlias(true);
        linesPaint.setStyle(Paint.Style.STROKE);

//        this.setLayerType(LAYER_TYPE_HARDWARE, linesPaint);

        this.chartData = chartData;
        points = new float[(chartData.xValues.length - 1) * 4];

        periodPaint = new Paint();

        setPeriodIndices(0, chartData.xValues.length);

        totalMinX = minX;
        totalMaxX = maxX;

        int[] yLimits = chartData.calcYLimits(startIndex, endIndex);
        minY = yLimits[0];
        maxY = yLimits[1];
        totalMinY = yLimits[2];
        totalMaxY = yLimits[3];

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(0xBB888888);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(dp12);

        titlePaint = new Paint();
        titlePaint.setAntiAlias(true);
        titlePaint.setColor(0xFF2332DC);
        titlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        titlePaint.setTextSize(dp12 + dp4);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private final float chartHeight = getResources().getDimension(R.dimen.chart_height);
    private final float datesHeight = getResources().getDimension(R.dimen.dates_height);
    private final float chartTitleHeight = getResources().getDimension(R.dimen.chart_title_height);
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

    private final String title;
    private final ChartData chartData;
    private final Paint linesPaint;
    private final float[] points;

    private final Paint periodPaint;
    private final Paint textPaint;
    private final Paint titlePaint;

    private float periodStartX = 0;
    private float periodEndX = 1;
    private int startIndex;
    private int endIndex;

    private long minX;
    private long maxX;

    private final long totalMinX;
    private final long totalMaxX;

    private int stepX;
    private float stepY;
    private float prevStepY;

    private final String[] formattedYSteps = new String[HOR_LINES];
    private final String[] formattedPrevYSteps = new String[HOR_LINES];

    private int minY;
    private int maxY;

    private int totalMinY;
    private int totalMaxY;

    private ValueAnimator maxYAnimator;

    private Map<String, ValueAnimator> opacityAnimators = new HashMap<>();

    public void setLineVisible(String name, boolean visible) {
        for (final ChartData.Line line : chartData.lines) {
            if (line.name.equals(name)) {

                ValueAnimator prev = opacityAnimators.get(name);
                if (prev != null) prev.cancel();

                line.isVisibleOrWillBe = visible;
                ValueAnimator animator = ValueAnimator
                        .ofInt(line.alpha, visible ? 0xFF : 0x00)
                        .setDuration(ANIMATION_DURATION);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        line.alpha = (int) animation.getAnimatedValue();
                        invalidate();
                    }
                });
                opacityAnimators.put(name, animator);
                animator.start();

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

        stepX = (endIndex - startIndex) / VER_DATES;
        int m = 0;
        while (stepX > 1) {
            stepX >>= 1;
            ++m;
        }
        for (int i = 0; i < m; i++) {
            stepX <<= 1;
        }

        animateMaxY();
    }

    private static final int HOR_LINES = 6;
    private static final int VER_DATES = 4;

    private void animateMaxY() {
        if (maxYAnimator == null) {
            maxYAnimator = ValueAnimator.ofFloat(0f, 1f);
            maxYAnimator.setInterpolator(new DecelerateInterpolator());
            maxYAnimator.setDuration(ANIMATION_DURATION);
        } else {
            maxYAnimator.cancel();
            maxYAnimator.removeAllUpdateListeners();
            maxYAnimator.removeAllListeners();
        }

        final int[] yLimits = chartData.calcYLimits(startIndex, endIndex);

        stepY = (float) (yLimits[1] * (1 / (HOR_LINES - 0.25)));
        for (int i = 0; i < formattedYSteps.length; i++) {
            formattedYSteps[i] = formatY(stepY * i);
        }

        final float startMaxY = maxY;
        final float endMaxY = yLimits[1];
        final float startTotalMaxY = totalMaxY;
        final float endTotalMaxY = yLimits[3];

        maxYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                maxY = (int) (startMaxY + (endMaxY - startMaxY) * animation.getAnimatedFraction());
                totalMaxY = (int) (startTotalMaxY + (endTotalMaxY - startTotalMaxY) * animation.getAnimatedFraction());
                invalidate();
            }
        });
        maxYAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean isCanceled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                isCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isCanceled) {
                    prevStepY = stepY;
                    System.arraycopy(formattedYSteps, 0, formattedPrevYSteps, 0, HOR_LINES);
                }

            }
        });
        maxYAnimator.start();
    }

    private static final int DRAG_SELECTOR = 1;
    private static final int DRAG_START = 2;
    private static final int DRAG_END = 3;
    private int dragPointerId = -1;
    private int currentDrag = 0;
    private float selectorDragCenterOffset = 0f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int pointerId = event.getPointerId(event.getActionIndex());

        final float selectorCenter = (periodStartX + periodEndX) / 2f;
        final float selectorWidth = periodEndX - periodStartX;
        final float selectorWidthPixels = getWidth() - 2 * chartPadding;
        float x = (event.getX() - chartPadding) / selectorWidthPixels;

        final float minSelectorWidthRel = minPeriodSelectorWidth * 1f / selectorWidthPixels;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (chartTitleHeight + chartHeight + datesHeight < event.getY() && event.getY() < chartTitleHeight + chartHeight + datesHeight + periodSelectorHeight) {
                    if (Math.abs(periodStartX - x) * selectorWidthPixels < periodSelectorDraggableWidth) {
                        dragPointerId = pointerId;
                        currentDrag = DRAG_START;
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else if (Math.abs(periodEndX - x) * selectorWidthPixels < periodSelectorDraggableWidth) {
                        dragPointerId = pointerId;
                        currentDrag = DRAG_END;
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else if (periodStartX < x && x < periodEndX) {
                        dragPointerId = pointerId;
                        currentDrag = DRAG_SELECTOR;
                        selectorDragCenterOffset = selectorCenter - x;
                        movePeriodSelectorTo(x + selectorDragCenterOffset, selectorWidth);
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointerId == dragPointerId) {
                    switch (currentDrag) {
                        case DRAG_START:
                            if (x > selectorCenter - minSelectorWidthRel) {
                                x = selectorCenter - minSelectorWidthRel;
                            }
                            periodStartX = x;
                            int newStartIndex = (int) (x * chartData.xValues.length);
                            setPeriodIndices(newStartIndex, endIndex);
                            break;
                        case DRAG_END:
                            if (x < selectorCenter + minSelectorWidthRel) {
                                x = selectorCenter + minSelectorWidthRel;
                            }
                            periodEndX = x;
                            int newEndIndex = (int) (x * chartData.xValues.length);
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
        if (newMax > 1) {
            newMin = 1 - selectorWidth;
            newMax = 1;
        }

        periodStartX = newMin;
        periodEndX = newMax;

        int newStartIndex = (int) (newMin * chartData.xValues.length);
        int newEndIndex = (int) (newMax * chartData.xValues.length);
        setPeriodIndices(newStartIndex, newEndIndex);
    }

    private long nanos;

    private void logMillis(String s) {
        long nanoTime = System.nanoTime();
        LogUtils.d(s + " " + (nanoTime - nanos) / 1_000_000.0);
        nanos = nanoTime;
    }

    float[] floats = new float[26000];

    @Override
    protected void onDraw(Canvas canvas) {
        LogUtils.d("onDraw " + canvas.isHardwareAccelerated());

        final long onDrawBegin = System.nanoTime();

//        int q = 0;
//        for (int i = 0; i < floats.length / 4; i++) {
//            final float _x = i % 2 == 0 ? i / 10f : 600;
//            final float _y = i / 20f;
//
//            floats[q++] = _x;
//            floats[q++] = _y;
//            if (i != 0 && i != floats.length / 4 - 1) {
//                floats[q++] = _x;
//                floats[q++] = _y;
//            }
//        }
//
//        linesPaint.setColor(Color.RED);
//        canvas.drawLines(floats, linesPaint);

        nanos = System.nanoTime();
        logMillis("start");

        canvas.drawText(title, chartPadding, dp12 + dp12 + dp4, titlePaint);

        canvas.translate(0, chartTitleHeight);

        float drawingWidth = getWidth() - 2 * chartPadding;

        linesPaint.setStrokeWidth(lineWidth / 2f);
        linesPaint.setColor(0x99CCCCCC);

        linesPaint.setAlpha((int) (maxYAnimator.getAnimatedFraction() * 0xFF));
        textPaint.setAlpha((int) (maxYAnimator.getAnimatedFraction() * 0xFF));

        for (int i = 0; i < HOR_LINES; i++) {
            float y = (1 - (stepY * i / maxY)) * chartHeight;
            canvas.drawLine(chartPadding, y, getWidth() - chartPadding, y, linesPaint);
            canvas.drawText(formattedYSteps[i], chartPadding, y - dp6, textPaint);
        }
        if (prevStepY > 0) {
            linesPaint.setAlpha((int) (0xFF - maxYAnimator.getAnimatedFraction() * 0xFF));
            textPaint.setAlpha((int) (0xFF - maxYAnimator.getAnimatedFraction() * 0xFF));

            for (int i = 0; i < HOR_LINES; i++) {
                float y = (1 - (prevStepY * i / maxY)) * chartHeight;
                canvas.drawLine(chartPadding, y, getWidth() - chartPadding, y, linesPaint);
                canvas.drawText(formattedPrevYSteps[i], chartPadding, y - dp6, textPaint);
            }
        }

        linesPaint.setAlpha(0xFF);
        textPaint.setAlpha(0xFF);

        logMillis("lines Y");

        float showingDatesCount = (endIndex - startIndex) * 1f / stepX;
        int oddDatesAlpha = 0xFF - (int) ((showingDatesCount - VER_DATES) / VER_DATES * 0xFF);
        for (int i = startIndex / stepX; i < (endIndex - 1) / stepX + 1; i++) {
            float x = (i * stepX - startIndex * 1f) / (endIndex - startIndex) * drawingWidth + chartPadding;
            textPaint.setAlpha(i * stepX % (stepX * 2) == 0 ? 0xFF : oddDatesAlpha);
            canvas.drawText(chartData.dates[i * stepX], x, chartHeight + dp12 + dp2, textPaint);
        }

        logMillis("dates");

        for (int c = 0; c < chartData.lines.size(); c++) {
            final ChartData.Line line = chartData.lines.get(c);
            if (line.isVisible()) {
                linesPaint.setColor(line.color);
                linesPaint.setAlpha(line.alpha);

                int q = 0;
                final float wid = (maxX - minX) / drawingWidth;
                final float hei = (maxY - minY) / chartHeight;
                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _x = ((float) chartData.xValues[i] - minX) / wid;
                    final float _y = chartHeight - ((float) line.values[i] - minY) / hei;

                    points[q++] = _x;
                    points[q++] = _y;
                    if (i != 0 && i != chartData.xValues.length - 1) {
                        points[q++] = _x;
                        points[q++] = _y;
                    }
                }

                logMillis("points[]");

                linesPaint.setStrokeWidth(lineWidth);

                canvas.save();
                canvas.translate(chartPadding, 0);
                linesPaint.setStrokeCap(Paint.Cap.SQUARE);
                canvas.drawLines(points, linesPaint);
                canvas.restore();

                logMillis("drawLines[]");

                q = 0;
                final float widP = (totalMaxX - totalMinX) / drawingWidth;
                final float heiP = (totalMaxY - totalMinY) / periodSelectorHeight;
                final float dYP = chartHeight + datesHeight + periodSelectorHeight;
                final int div = 2;
                for (int i = 0; i < chartData.xValues.length / div; i++) {
                    final int ii = i * div;
                    final float _x = ((float) chartData.xValues[ii] - totalMinX) / widP;
                    final float _y = dYP - ((float) line.values[ii] - totalMinY) / heiP;

                    points[q++] = _x;
                    points[q++] = _y;
                    if (ii != 0 && ii != chartData.xValues.length / div - 1) {
                        points[q++] = _x;
                        points[q++] = _y;
                    }
                }

                logMillis("points period");

                linesPaint.setStrokeWidth(lineWidth / 2f);

                canvas.save();
                canvas.translate(chartPadding, 0);
                linesPaint.setStrokeCap(Paint.Cap.BUTT);
                canvas.drawLines(points, 0, q, linesPaint);
                canvas.restore();

                logMillis("drawLines period");
            }
        }

        canvas.save();
        canvas.translate(chartPadding, chartHeight + datesHeight);

        float startX = startIndex * 1f / chartData.xValues.length * drawingWidth;
        float endX = endIndex * 1f / chartData.xValues.length * drawingWidth;

        periodPaint.setStyle(Paint.Style.FILL);
        periodPaint.setColor(0x18000000);

        canvas.drawRect(0, 0, startX, periodSelectorHeight, periodPaint);
        canvas.drawRect(endX, 0, drawingWidth, periodSelectorHeight, periodPaint);

        periodPaint.setColor(0x88000000);

        final float borderHor = dp2;
        final float borderVer = dp12;

        canvas.drawRect(startX + borderVer, 0, endX - borderVer, borderHor, periodPaint);
        canvas.drawRect(startX + borderVer, periodSelectorHeight - borderHor, endX - borderVer, periodSelectorHeight, periodPaint);

        canvas.drawRect(startX, 0, startX + borderVer, periodSelectorHeight, periodPaint);
        canvas.drawRect(endX - borderVer, 0, endX, periodSelectorHeight, periodPaint);

        periodPaint.setColor(0xD7FFFFFF);

        canvas.drawRect(
                startX + borderVer / 2 - dp2,
                periodSelectorHeight / 2 - dp8,
                startX + borderVer / 2 + dp2,
                periodSelectorHeight / 2 + dp8,
                periodPaint
        );
        canvas.drawRect(
                endX - borderVer / 2 - dp2,
                periodSelectorHeight / 2 - dp8,
                endX - borderVer / 2 + dp2,
                periodSelectorHeight / 2 + dp8,
                periodPaint
        );

        canvas.restore();

        logMillis("period selector");

        LogUtils.d("onDraw " + title + " " + (System.nanoTime() - onDrawBegin) / 1_000_000.0);
    }

    private static String formatY(float y) {
        if (y > 1000000) return String.format("%.1fM", y / 1000000);
        if (y > 1000) return String.format("%.1fK", y / 1000);
        return String.format("%.0f", y);
    }
}
