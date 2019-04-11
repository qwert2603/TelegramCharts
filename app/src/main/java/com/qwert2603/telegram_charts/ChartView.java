package com.qwert2603.telegram_charts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.qwert2603.telegram_charts.entity.ChartData;

import java.util.HashMap;
import java.util.Map;

public class ChartView extends View {

    private static final long ANIMATION_DURATION = 200L;

    public ChartView(Context context, String title, ChartData chartData) {
        super(context);

        this.title = title;

        linesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linesPaint.setStyle(Paint.Style.STROKE);

        this.chartData = chartData;
        points = new float[(chartData.xValues.length - 1) * 4];

        periodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        setPeriodIndices(0, chartData.xValues.length);

        totalMinX = minX;
        totalMaxX = maxX;

        int[] yLimits = chartData.calcYLimits(startIndex, endIndex);
        minY = yLimits[0];
        maxY = yLimits[1];
        totalMinY = yLimits[2];
        totalMaxY = yLimits[3];

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xBB888888);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(dp12);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.BLACK);
        titlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
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

    // period selector
    final float fillRadius = dp4;
    final Path path = new Path();
    final float[] radiiLeft = {fillRadius, fillRadius, 0, 0, 0, 0, fillRadius, fillRadius};
    final float[] radiiRight = {0, 0, fillRadius, fillRadius, fillRadius, fillRadius, 0, 0};

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

    private int selectedIndex = -1;
    private int prevSelectedIndex = -1;

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
    private ValueAnimator selectedIndexAnimator;

    private Map<String, ValueAnimator> opacityAnimators = new HashMap<>();

    public void setLineVisible(String name, boolean visible) {
        for (final ChartData.Line line : chartData.lines) {
            if (line.name.equals(name)) {

                ValueAnimator animator = opacityAnimators.get(name);
                if (animator == null) {
                    animator = ValueAnimator
                            .ofInt(0x00)
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
                }

                line.isVisibleOrWillBe = visible;

                animator.setIntValues(line.alpha, visible ? 0xFF : 0x00);
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

    private float pendingMaxY;
    private float pendingTotalMaxY;

    private void animateMaxY() {
        final int[] yLimits = chartData.calcYLimits(startIndex, endIndex);

        final float startMaxY = maxY;
        final float endMaxY = yLimits[1];
        final float startTotalMaxY = totalMaxY;
        final float endTotalMaxY = yLimits[3];

        if (pendingMaxY == endMaxY && pendingTotalMaxY == endTotalMaxY) {
            invalidate();
            return;
        }

        pendingMaxY = endMaxY;
        pendingTotalMaxY = endTotalMaxY;

        stepY = (float) (yLimits[1] * (1 / (HOR_LINES - 0.25)));
        final int stepYInt = (int) stepY;
        for (int i = 0; i < formattedYSteps.length; i++) {
            formattedYSteps[i] = formatY(stepYInt * i);
        }

        if (maxYAnimator == null) {
            maxYAnimator = ValueAnimator.ofFloat(0f, 1f);
            maxYAnimator.setInterpolator(new DecelerateInterpolator());
            maxYAnimator.setDuration(ANIMATION_DURATION);
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
        } else {
            maxYAnimator.removeAllUpdateListeners();
        }

        maxYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                maxY = (int) (startMaxY + (endMaxY - startMaxY) * animation.getAnimatedFraction());
                totalMaxY = (int) (startTotalMaxY + (endTotalMaxY - startTotalMaxY) * animation.getAnimatedFraction());
                invalidate();
            }
        });
        maxYAnimator.start();
    }

    private static final int DRAG_SELECTOR = 1;
    private static final int DRAG_START = 2;
    private static final int DRAG_END = 3;
    private static final int DRAG_SELECTED_INDEX = 4;
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
                } else if (chartTitleHeight < event.getY() && event.getY() < chartTitleHeight + chartHeight) {
                    dragPointerId = pointerId;
                    currentDrag = DRAG_SELECTED_INDEX;
                    updateSelectedIndex(x);
                    getParent().requestDisallowInterceptTouchEvent(true);
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
                        case DRAG_SELECTED_INDEX:
                            updateSelectedIndex(x);
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

    private void updateSelectedIndex(float x) {
        int newSelectedIndex = (int) (startIndex + (endIndex - 1 - startIndex) * x);
        if (newSelectedIndex < 0) newSelectedIndex = 0;
        if (newSelectedIndex > endIndex - 1) newSelectedIndex = endIndex - 1;

        if (newSelectedIndex == selectedIndex) return;
        prevSelectedIndex = selectedIndex;
        selectedIndex = newSelectedIndex;

        if (selectedIndexAnimator == null) {
            selectedIndexAnimator = ValueAnimator.ofFloat(0f, 1f);
            selectedIndexAnimator.setInterpolator(new DecelerateInterpolator());
            selectedIndexAnimator.setDuration(ANIMATION_DURATION);
            selectedIndexAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidate();
                }
            });
        }

        selectedIndexAnimator.start();
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

    @Override
    protected void onDraw(Canvas canvas) {

        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(dp12 + dp4);
        canvas.drawText(title, chartPadding, dp12 + dp12 + dp4, titlePaint);
        titlePaint.setTextSize(dp12 + dp2);
        final String text = chartData.fullDates[startIndex] + " - " + chartData.fullDates[endIndex - 1];
        final float measureText = titlePaint.measureText(text);
        canvas.drawText(text, getWidth() - chartPadding - measureText, dp12 + dp12 + dp4, titlePaint);

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

        float showingDatesCount = (endIndex - startIndex) * 1f / stepX;
        int oddDatesAlpha = 0xFF - (int) ((showingDatesCount - VER_DATES) / VER_DATES * 0xFF);
        for (int i = startIndex / stepX; i < (endIndex - 1) / stepX + 1; i++) {
            float x = (i * stepX - startIndex * 1f) / (endIndex - startIndex) * drawingWidth + chartPadding;
            textPaint.setAlpha(i * stepX % (stepX * 2) == 0 ? 0xFF : oddDatesAlpha);
            final float dateTextWidth = textPaint.measureText(chartData.dates[i * stepX]);
            canvas.drawText(chartData.dates[i * stepX], x - dateTextWidth / 2, chartHeight + dp12 + dp2, textPaint);
        }

        final float wid = (maxX - minX) / drawingWidth;
        final float hei = (maxY - 0/*minY*/) / chartHeight;
        final float widP = (totalMaxX - totalMinX) / drawingWidth;
        final float heiP = (totalMaxY - 0/*totalMinY*/) / periodSelectorHeight;
        final float dYP = chartHeight + datesHeight + periodSelectorHeight;
        final int div = 2;

        canvas.translate(chartPadding, 0);

        for (int c = 0; c < chartData.lines.size(); c++) {
            final ChartData.Line line = chartData.lines.get(c);
            if (line.isVisible()) {
                linesPaint.setColor(line.color);
                linesPaint.setAlpha(line.alpha);

                int q = 0;
                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _x = ((float) chartData.xValues[i] - minX) / wid;
                    final float _y = chartHeight - ((float) line.values[i] - 0/*minY*/) / hei;

                    points[q++] = _x;
                    points[q++] = _y;
                    if (i != 0 && i != chartData.xValues.length - 1) {
                        points[q++] = _x;
                        points[q++] = _y;
                    }
                }

                linesPaint.setStrokeWidth(lineWidth);

                linesPaint.setStrokeCap(Paint.Cap.SQUARE);
                canvas.drawLines(points, linesPaint);

                q = 0;
                for (int i = 0; i < chartData.xValues.length / div; i++) {
                    final int ii = i * div;
                    final float _x = ((float) chartData.xValues[ii] - totalMinX) / widP;
                    final float _y = dYP - ((float) line.values[ii] - 0/*totalMinY*/) / heiP;

                    points[q++] = _x;
                    points[q++] = _y;
                    if (i != 0 && i != chartData.xValues.length / div - 1) {
                        points[q++] = _x;
                        points[q++] = _y;
                    }
                }

                linesPaint.setStrokeWidth(lineWidth / 2f);

                linesPaint.setStrokeCap(Paint.Cap.BUTT);
                canvas.drawLines(points, 0, q, linesPaint);
            }
        }

        if (selectedIndex >= 0) {

            final float _x = ((float) chartData.xValues[selectedIndex] - minX) / wid;

            if (-chartPadding < _x && _x < getWidth() - chartPadding) {
                final float changeFraction = selectedIndexAnimator.getAnimatedFraction();

                linesPaint.setStrokeWidth(lineWidth / 2f);
                linesPaint.setColor(0x99CCCCCC);
                canvas.drawLine(_x, 0, _x, chartHeight, linesPaint);

                final boolean panelLefted = _x < getWidth() / 2 - chartPadding;
                final float prevX = prevSelectedIndex >= 0 ? ((float) chartData.xValues[prevSelectedIndex] - minX) / wid : _x;
                final float panelAnchor = _x + (prevX - _x) * (1 - changeFraction);

                final float panelLeft = panelAnchor + (panelLefted ? dp12 * -1 : dp12 * -11);
                final float panelRight = panelAnchor + (panelLefted ? dp12 * 11 : dp12 * 1);

                linesPaint.setStrokeWidth(lineWidth);
                periodPaint.setColor(Color.WHITE);

                final float lineHeight = dp12 * 2;
                float lineY = lineHeight;

                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);
                    if (line.isVisibleOrWillBe) {
                        linesPaint.setColor(line.color);
                        linesPaint.setAlpha(line.alpha);

                        final float _y = chartHeight - ((float) line.values[selectedIndex] - 0/*minY*/) / hei;
                        canvas.drawCircle(_x, _y, dp4, periodPaint);
                        canvas.drawCircle(_x, _y, dp4, linesPaint);

                        lineY += lineHeight;
                    }
                }

                final float panelPadding = dp12;
                canvas.drawRoundRect(
                        panelLeft - panelPadding,
                        0,
                        panelRight + panelPadding,
                        lineY + panelPadding,
                        dp4, dp4, periodPaint);
                linesPaint.setStrokeWidth(lineWidth / 2f);
                linesPaint.setColor(0x99CCCCCC);
                canvas.drawRoundRect(
                        panelLeft - panelPadding,
                        0,
                        panelRight + panelPadding,
                        lineY + panelPadding,
                        dp4, dp4, linesPaint);

                lineY = lineHeight;

                titlePaint.setAlpha((int) (0xFF * changeFraction));
                final boolean isBack = selectedIndex < prevSelectedIndex;
                final String selectedDate = chartData.selectedDates[selectedIndex];
                final String prevDate = prevSelectedIndex >= 0 ? chartData.selectedDates[prevSelectedIndex] : selectedDate;

                final int commonChars = Utils.commonEnd(prevDate, selectedDate);
                final String changeOfSelected = selectedDate.substring(0, selectedDate.length() - commonChars);

                final float translationAppear = (1 - changeFraction) * dp12 * (isBack ? -1 : 1);
                final float translationDisappear = -changeFraction * dp12 * (isBack ? -1 : 1);
                canvas.drawText(changeOfSelected, panelLeft, lineY + translationAppear, titlePaint);
                if (prevSelectedIndex >= 0) {
                    titlePaint.setAlpha(0XFF - (int) (0xFF * changeFraction));
                    canvas.drawText(prevDate.substring(0, prevDate.length() - commonChars), panelLeft, lineY + translationDisappear, titlePaint);
                }
                titlePaint.setAlpha(0xFF);

                canvas.drawText(selectedDate.substring(selectedDate.length() - commonChars), panelLeft + titlePaint.measureText(changeOfSelected), lineY, titlePaint);

                titlePaint.setTypeface(Typeface.DEFAULT);
                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);
                    if (line.isVisibleOrWillBe) {
                        lineY += lineHeight;
                        canvas.drawText(line.name, panelLeft, lineY, titlePaint);
                    }
                }
                titlePaint.setTypeface(Typeface.DEFAULT_BOLD);

                lineY = lineHeight;

                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);
                    if (line.isVisibleOrWillBe) {
                        titlePaint.setColor(line.color);
                        titlePaint.setAlpha((int) (0xFF * changeFraction));
                        lineY += lineHeight;
                        String formatY = formatY(line.values[selectedIndex]);
                        float valueWidth = titlePaint.measureText(formatY);
                        canvas.drawText(formatY, panelRight - valueWidth, lineY + translationAppear, titlePaint);
                    }
                }
                if (prevSelectedIndex >= 0) {
                    lineY = lineHeight;
                    for (int c = 0; c < chartData.lines.size(); c++) {
                        final ChartData.Line line = chartData.lines.get(c);
                        if (line.isVisibleOrWillBe) {
                            titlePaint.setColor(line.color);
                            titlePaint.setAlpha(0XFF - (int) (0xFF * changeFraction));
                            lineY += lineHeight;
                            String formatY = formatY(line.values[prevSelectedIndex]);
                            float valueWidth = titlePaint.measureText(formatY);
                            canvas.drawText(formatY, panelRight - valueWidth, lineY + translationDisappear, titlePaint);
                        }
                    }
                }
                titlePaint.setAlpha(0xFF);
            }
        }

        canvas.translate(0, chartHeight + datesHeight);

        float startX = startIndex * 1f / chartData.xValues.length * drawingWidth;
        float endX = endIndex * 1f / chartData.xValues.length * drawingWidth;

        periodPaint.setStyle(Paint.Style.FILL);
        periodPaint.setColor(0x18000000);

        final float borderHor = dp2 / 2;
        final float borderVer = dp12;

        // period's outside.
        path.addRoundRect(0, 0, startX + borderVer, periodSelectorHeight, radiiLeft, Path.Direction.CW);
        canvas.drawPath(path, periodPaint);
        path.rewind();
        path.addRoundRect(endX - borderVer, 0, drawingWidth, periodSelectorHeight, radiiRight, Path.Direction.CW);
        canvas.drawPath(path, periodPaint);
        path.rewind();

        periodPaint.setColor(0x88000000);

        // horizontal borders
        canvas.drawRect(startX + borderVer, -borderHor, endX - borderVer, 0, periodPaint);
        canvas.drawRect(startX + borderVer, periodSelectorHeight, endX - borderVer, periodSelectorHeight + borderHor, periodPaint);

        // vertical borders
        path.addRoundRect(startX, -borderHor, startX + borderVer, periodSelectorHeight + borderHor, radiiLeft, Path.Direction.CW);
        canvas.drawPath(path, periodPaint);
        path.rewind();
        path.addRoundRect(endX - borderVer, -borderHor, endX, periodSelectorHeight + borderHor, radiiRight, Path.Direction.CW);
        canvas.drawPath(path, periodPaint);
        path.rewind();

        // white drag rects
        periodPaint.setColor(0xD7FFFFFF);
        canvas.drawRoundRect(
                startX + borderVer / 2 - dp2,
                periodSelectorHeight / 2 - dp8,
                startX + borderVer / 2 + dp2,
                periodSelectorHeight / 2 + dp8,
                dp2,
                dp2,
                periodPaint
        );
        canvas.drawRoundRect(
                endX - borderVer / 2 - dp2,
                periodSelectorHeight / 2 - dp8,
                endX - borderVer / 2 + dp2,
                periodSelectorHeight / 2 + dp8,
                dp2,
                dp2,
                periodPaint
        );
    }

    public static String formatY(int y) {
        if (y < 1_000) {
            return formatYLessThousand(y);
        } else if (y < 1_000_000) {
            final int q = y / 100;
            if (FORMATTED_CACHE_K[q] != null) return FORMATTED_CACHE_K[q];
            final int div = y % 1_000 / 100;
            final String formatted = formatYLessThousand(y / 1_000) + "." + formatYLessThousand(div) + "K";
            FORMATTED_CACHE_K[q] = formatted;
            return formatted;
        } else {
            final int q = y / 100_000;
            if (FORMATTED_CACHE_M[q] != null) return FORMATTED_CACHE_M[q];
            final int div = y % 1_000_000 / 100_000;
            final String formatted = formatYLessThousand(y / 1_000_000) + "." + formatYLessThousand(div) + "M";
            FORMATTED_CACHE_M[q] = formatted;
            return formatted;
        }
    }

    private static String formatYLessThousand(int y) {
        if (FORMATTED_CACHE[y] != null) return FORMATTED_CACHE[y];
        final String formatted = Integer.toString(y);
        FORMATTED_CACHE[y] = formatted;
        return formatted;
    }

    private static final String[] FORMATTED_CACHE = new String[1000];
    private static final String[] FORMATTED_CACHE_K = new String[10000];
    private static final String[] FORMATTED_CACHE_M = new String[10000];
}
