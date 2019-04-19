package com.qwert2603.telegram_charts.chart_delegates;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import com.qwert2603.telegram_charts.MainActivity;
import com.qwert2603.telegram_charts.R;
import com.qwert2603.telegram_charts.Utils;
import com.qwert2603.telegram_charts.entity.ChartData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartViewDelegateLines_2Y implements Delegate {
    @Override
    public void setNightMode(boolean night) {

    }
    private static final long ANIMATION_DURATION = 200L;

    private final Resources resources;
    private final Context context;

    private final Callbacks callbacks;

    public ChartViewDelegateLines_2Y(Context context, String title, final ChartData chartData, final Callbacks callbacks) {
        this.callbacks = callbacks;
        this.context = context;
        this.resources = context.getResources();

        this.title = title;

        linesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linesPaint.setStyle(Paint.Style.STROKE);

        this.chartData = chartData;
        points = new float[(chartData.xValues.length - 1) * 4];

        periodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        periodPaint.setStyle(Paint.Style.FILL);

        int[] yLimits = chartData.calcYLimits(startIndex, endIndex);
        minY = new float[]{yLimits[0], yLimits[4]};
        maxY = new float[]{yLimits[1], yLimits[5]};
        totalMinY = new float[]{yLimits[2], yLimits[6]};
        totalMaxY = new float[]{yLimits[3], yLimits[7]};

        setPeriodIndices(0, chartData.xValues.length);

        totalMinX = minX;
        totalMaxX = maxX;

        legendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legendPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        dp12 = getResources().getDimension(R.dimen.dp12);
        legendPaint.setTextSize(dp12 - dp12 / 12f);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.BLACK);
        titlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        chartHeight = getResources().getDimension(R.dimen.chart_height);
        datesHeight = getResources().getDimension(R.dimen.dates_height);
        chartTitleHeight = getResources().getDimension(R.dimen.chart_title_height);
        periodSelectorHeight = getResources().getDimension(R.dimen.period_selector_height);
        minPeriodSelectorWidth = getResources().getDimension(R.dimen.min_period_selector_width);
        periodSelectorDraggableWidth = getResources().getDimension(R.dimen.period_selector_draggable_width);
        lineWidth = getResources().getDimension(R.dimen.line_width);
        chartPadding = getResources().getDimension(R.dimen.chart_padding);
        dp2 = getResources().getDimension(R.dimen.dp2);
        dp4 = getResources().getDimension(R.dimen.dp4);
        dp6 = getResources().getDimension(R.dimen.dp6);
        dp8 = getResources().getDimension(R.dimen.dp8);

        // period selector
        final float fillRadius = dp6;
        radiiLeft = new float[]{fillRadius, fillRadius, 0, 0, 0, 0, fillRadius, fillRadius};
        radiiRight = new float[]{0, 0, fillRadius, fillRadius, fillRadius, fillRadius, 0, 0};

        //chips
        chipsMarginTop = dp6;
        chipMargin = dp12;
        chipPadding = dp12 + dp12;
        chipTextSize = dp12 + dp2;
        chipHeight = chipPadding + chipTextSize;

        chipWhiteTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chipWhiteTextPaint.setColor(Color.WHITE);
        chipWhiteTextPaint.setTextSize(chipTextSize);

        for (ChartData.Line line : chartData.lines) {
            line.linePaint.setColor(line.color);
            line.linePaint.setStrokeWidth(lineWidth * 0.9f);
            line.linePaint.setStrokeCap(Paint.Cap.SQUARE);

            line.linePeriodPaint.setColor(line.color);
            line.linePeriodPaint.setStrokeWidth(lineWidth / 2f * 0.9f);
            line.linePeriodPaint.setStrokeCap(Paint.Cap.BUTT);

            line.chipTextPaint.setColor(line.color);
            line.chipTextPaint.setTextSize(chipTextSize);
            line.chipTextWidth = line.chipTextPaint.measureText(line.name);

            line.chipBorderPaint.setColor(line.color);
            line.chipBorderPaint.setStyle(Paint.Style.STROKE);
            line.chipBorderPaint.setStrokeWidth(lineWidth / 2f);

            line.panelTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
            line.panelTextPaint.setColor(line.color);
            line.panelTextPaint.setTextSize(dp12 + dp2);
        }

        gestureDetector = new GestureDetector(this.context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (chartTitleHeight + chartHeight + datesHeight + periodSelectorHeight < e.getY()) {
                    for (ChartData.Line line : chartData.lines) {
                        if (line.chipRectOnScreen.contains(e.getX(), e.getY())) {
                            setLineVisible(line.name, !line.isVisibleOrWillBe);
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (chartTitleHeight + chartHeight + datesHeight + periodSelectorHeight < e.getY()) {
                    for (ChartData.Line line : chartData.lines) {
                        if (line.chipRectOnScreen.contains(e.getX(), e.getY())) {
                            setOnlyOneLineVisible(line.name);
                            break;
                        }
                    }
                    lastDownX = -1;
                    lastDownY = -1;
                    callbacks.invalidate();
                }
            }
        });

        drawableCheck = resources.getDrawable(R.drawable.ic_done_black_24dp);

        periodSelectorClipPath = new Path();
        periodSelectorClipPath.addRoundRect(
                0,
                chartHeight + datesHeight,
                getDrawingWidth(),
                chartHeight + datesHeight + periodSelectorHeight,
                new float[]{fillRadius, fillRadius, fillRadius, fillRadius, fillRadius, fillRadius, fillRadius, fillRadius},
                Path.Direction.CW
        );
    }

    private Resources getResources() {
        return resources;
    }

    private final GestureDetector gestureDetector;
    private final RectF panelRectOnScreen = new RectF();

    private final float chartHeight;
    private final float datesHeight;
    private final float chartTitleHeight;
    private final float periodSelectorHeight;
    private final float minPeriodSelectorWidth;
    private final float periodSelectorDraggableWidth;
    private final float lineWidth;
    private final float chartPadding;
    private final float dp2;
    private final float dp4;
    private final float dp6;
    private final float dp8;
    private final float dp12;
    private final float chipsMarginTop;
    private final float chipMargin;
    private final float chipTextSize;
    private final float chipPadding;
    private final float chipHeight;

    private final Path path = new Path();
    private final float[] radiiLeft;
    private final float[] radiiRight;

    private final String title;
    private final ChartData chartData;
    private final Paint linesPaint;
    private final float[] points;

    private final Paint periodPaint;
    private final Paint legendPaint;
    private final Paint titlePaint;
    private final Paint chipWhiteTextPaint;

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
    private float[] stepY = new float[2];
    private float[] prevStepY = new float[2];

    private final float[][] stepsY = new float[2][HOR_LINES];
    private final float[][] prevStepsY = new float[2][HOR_LINES];

    private float[] minY;
    private float[] maxY;

    private float[] totalMinY;
    private float[] totalMaxY;

    private ValueAnimator[] yLimitsAnimator = new ValueAnimator[2];
    private ValueAnimator selectedIndexAnimator;

    private Map<String, ValueAnimator> opacityAnimators = new HashMap<>();

    private final Drawable drawableCheck;

    private final Path periodSelectorClipPath;

    @Override
    public int measureHeight(int width) {
        final float drawingWidth = getDrawingWidth();
        float currentLineX = chartPadding;
        float currentLineY = chartTitleHeight + chartHeight + datesHeight + periodSelectorHeight + chipsMarginTop + chipMargin;
        if (chartData.lines.size() > 1) {
            titlePaint.setTextSize(chipTextSize);
            for (int c = 0; c < chartData.lines.size(); c++) {
                final ChartData.Line line = chartData.lines.get(c);
                final float chipWidth = line.chipTextWidth + 2 * chipPadding;

                if (chipWidth > drawingWidth - currentLineX) {
                    currentLineX = chartPadding;
                    currentLineY += chipHeight + chipMargin;
                }

                currentLineX += chipWidth + chipMargin;
            }

            // plus last chips line.
            currentLineY += chipHeight + chipMargin;
        }

        return (int) (currentLineY + dp6);
    }

    private void setOnlyOneLineVisible(String name) {
        for (final ChartData.Line line : chartData.lines) {
            final boolean visible = line.name.equals(name);

            ValueAnimator animator = opacityAnimators.get(line.name);
            if (animator == null) {
                animator = ValueAnimator
                        .ofInt(0x00)
                        .setDuration(ANIMATION_DURATION);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        line.alpha = (int) animation.getAnimatedValue();
                        callbacks.invalidate();
                    }
                });
                opacityAnimators.put(line.name, animator);
            }

            line.isVisibleOrWillBe = visible;

            animator.setIntValues(line.alpha, visible ? 0xFF : 0x00);
            animator.start();
        }

        animateYLimits();
    }

    private void setLineVisible(String name, boolean visible) {
        List<ChartData.Line> lines = chartData.lines;
        for (int c = 0; c < lines.size(); c++) {
            final ChartData.Line line = lines.get(c);
            if (line.name.equals(name)) {

                ValueAnimator animator = opacityAnimators.get(line.name);
                if (animator == null) {
                    animator = ValueAnimator
                            .ofInt(0x00)
                            .setDuration(ANIMATION_DURATION);
                    animator.setInterpolator(new DecelerateInterpolator());
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            line.alpha = (int) animation.getAnimatedValue();
                            callbacks.invalidate();
                        }
                    });
                    opacityAnimators.put(line.name, animator);
                }

                line.isVisibleOrWillBe = visible;

                animator.setIntValues(line.alpha, visible ? 0xFF : 0x00);
                animator.start();

                break;
            }
        }

        animateYLimits();
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

        animateYLimits();
    }

    private static final int HOR_LINES = 6;
    private static final int VER_DATES = 4;

    private float[] pendingMinY;
    private float[] pendingTotalMinY;
    private float[] pendingMaxY;
    private float[] pendingTotalMaxY;

    private void animateYLimits() {
        final int[] yLimits = chartData.calcYLimits(startIndex, endIndex);

        final float[] startMinY = minY;
        final float[] endMinY = new float[]{yLimits[0], yLimits[4]};
        final float[] startTotalMinY = totalMinY;
        final float[] endTotalMinY = new float[]{yLimits[2], yLimits[6]};

        final float[] startMaxY = maxY;
        final float[] endMaxY = new float[]{yLimits[1], yLimits[5]};
        final float[] startTotalMaxY = totalMaxY;
        final float[] endTotalMaxY = new float[]{yLimits[3], yLimits[7]};

        if (Arrays.equals(pendingMinY, endMinY) && Arrays.equals(pendingTotalMinY, endTotalMinY) && Arrays.equals(pendingMaxY, endMaxY) && Arrays.equals(pendingTotalMaxY, endTotalMaxY)) {
            callbacks.invalidate();
            return;
        }

        pendingMinY = endMinY;
        pendingTotalMinY = endTotalMinY;
        pendingMaxY = endMaxY;
        pendingTotalMaxY = endTotalMaxY;

        for (int lineIndex = 0; lineIndex < 2; lineIndex++) {
            final int finalLineIndex = lineIndex;
            final float dY = endMaxY[finalLineIndex] - endMinY[finalLineIndex];
            stepY[finalLineIndex] = dY / (HOR_LINES - 0.25f);
            for (int i = 0; i < stepsY[finalLineIndex].length; i++) {
                stepsY[finalLineIndex][i] = (endMinY[finalLineIndex] + stepY[finalLineIndex] * i);
            }

            if (yLimitsAnimator[finalLineIndex] == null) {
                yLimitsAnimator[finalLineIndex] = ValueAnimator.ofFloat(0f, 1f);
                yLimitsAnimator[finalLineIndex].setInterpolator(new DecelerateInterpolator());
                yLimitsAnimator[finalLineIndex].setDuration(ANIMATION_DURATION);
                yLimitsAnimator[finalLineIndex].addListener(new AnimatorListenerAdapter() {
                    private boolean isCanceled = false;

                    @Override
                    public void onAnimationStart(Animator animation) {
                        isCanceled = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        isCanceled = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isCanceled) {
                            prevStepY[finalLineIndex] = stepY[finalLineIndex];
                            System.arraycopy(stepsY[finalLineIndex], 0, prevStepsY[finalLineIndex], 0, HOR_LINES);
                        }

                    }
                });
            } else {
                yLimitsAnimator[finalLineIndex].removeAllUpdateListeners();
            }

            yLimitsAnimator[finalLineIndex].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    minY[finalLineIndex] = (startMinY[finalLineIndex] + (endMinY[finalLineIndex] - startMinY[finalLineIndex]) * animation.getAnimatedFraction());
                    totalMinY[finalLineIndex] = (startTotalMinY[finalLineIndex] + (endTotalMinY[finalLineIndex] - startTotalMinY[finalLineIndex]) * animation.getAnimatedFraction());

                    maxY[finalLineIndex] = (startMaxY[finalLineIndex] + (endMaxY[finalLineIndex] - startMaxY[finalLineIndex]) * animation.getAnimatedFraction());
                    totalMaxY[finalLineIndex] = (startTotalMaxY[finalLineIndex] + (endTotalMaxY[finalLineIndex] - startTotalMaxY[finalLineIndex]) * animation.getAnimatedFraction());

                    callbacks.invalidate();
                }
            });
            yLimitsAnimator[finalLineIndex].cancel();
            yLimitsAnimator[finalLineIndex].start();
        }
    }

    private static final int DRAG_SELECTOR = 1;
    private static final int DRAG_START = 2;
    private static final int DRAG_END = 3;
    private static final int DRAG_SELECTED_INDEX = 4;
    private int dragPointerId = -1;
    private int currentDrag = 0;
    private float selectorDragCenterOffset = 0f;

    private float startSelectedIndexX = -1;
    private float startSelectedIndexY = -1;
    private boolean draggingSelectedIndex = false;

    private float lastDownX = -1;
    private float lastDownY = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        final int pointerId = event.getPointerId(event.getActionIndex());

        final float selectorCenter = (periodStartX + periodEndX) / 2f;
        final float selectorWidth = periodEndX - periodStartX;
        final float selectorWidthPixels = callbacks.getWidth() - 2 * chartPadding;
        float x = (event.getX() - chartPadding) / selectorWidthPixels;

        final float minSelectorWidthRel = minPeriodSelectorWidth * 1f / selectorWidthPixels;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (chartTitleHeight + chartHeight + datesHeight < event.getY() && event.getY() < chartTitleHeight + chartHeight + datesHeight + periodSelectorHeight) {
                    if (Math.abs(periodStartX - x) * selectorWidthPixels < periodSelectorDraggableWidth) {
                        dragPointerId = pointerId;
                        currentDrag = DRAG_START;
                        callbacks.requestDisallowInterceptTouchEvent(true);
                    } else if (Math.abs(periodEndX - x) * selectorWidthPixels < periodSelectorDraggableWidth) {
                        dragPointerId = pointerId;
                        currentDrag = DRAG_END;
                        callbacks.requestDisallowInterceptTouchEvent(true);
                    } else if (periodStartX < x && x < periodEndX) {
                        dragPointerId = pointerId;
                        currentDrag = DRAG_SELECTOR;
                        selectorDragCenterOffset = selectorCenter - x;
                        movePeriodSelectorTo(x + selectorDragCenterOffset, selectorWidth);
                        callbacks.requestDisallowInterceptTouchEvent(true);
                    }
                } else if (chartTitleHeight < event.getY() && event.getY() < chartTitleHeight + chartHeight) {
                    if (selectedIndex < 0) {
                        updateSelectedIndex(x);
                    } else {
                        if (!panelRectOnScreen.contains(event.getX(), event.getY())) {
                            selectedIndex = -1;
                            currentDrag = 0;
                            dragPointerId = -1;
                            startSelectedIndexX = -1;
                            startSelectedIndexY = -1;
                            callbacks.invalidate();
                        }
                    }
                    dragPointerId = pointerId;
                    currentDrag = DRAG_SELECTED_INDEX;
                    startSelectedIndexX = event.getX();
                    startSelectedIndexY = event.getY();
                    callbacks.requestDisallowInterceptTouchEvent(true);
                } else if (chartTitleHeight + chartHeight + datesHeight + periodSelectorHeight < event.getY()) {
                    dragPointerId = pointerId;
                    lastDownX = event.getX();
                    lastDownY = event.getY();
                    callbacks.invalidate();
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
                            if (!draggingSelectedIndex && Math.abs(event.getX() - startSelectedIndexX) < dp6 && Math.abs(event.getY() - startSelectedIndexY) > dp12) {
                                selectedIndex = -1;
                                currentDrag = 0;
                                dragPointerId = -1;
                                startSelectedIndexX = -1;
                                startSelectedIndexY = -1;
                                callbacks.requestDisallowInterceptTouchEvent(false);
                                callbacks.invalidate();
                            } else if (!draggingSelectedIndex && !panelRectOnScreen.contains(event.getX(), event.getY()) && Math.abs(event.getX() - startSelectedIndexX) > dp12 && Math.abs(event.getY() - startSelectedIndexY) < dp6) {
                                draggingSelectedIndex = true;
                            }
                            if (draggingSelectedIndex) {
                                updateSelectedIndex(x);
                            }
                            break;
                    }
                }
                break;
            default:
                if (pointerId == dragPointerId) {
                    currentDrag = 0;
                    dragPointerId = -1;
                    startSelectedIndexX = -1;
                    startSelectedIndexY = -1;
                    draggingSelectedIndex = false;
                    lastDownX = -1;
                    lastDownY = -1;
                    callbacks.invalidate();
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
                    callbacks.invalidate();
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
    public void onDraw(Canvas canvas) {

        canvas.save();

        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setColor(MainActivity.NIGHT_MODE ? Color.WHITE : Color.BLACK);
        titlePaint.setTextSize(dp12 + dp4);
        canvas.drawText(title, chartPadding, dp12 + dp12 + dp8, titlePaint);
        titlePaint.setTextSize(dp12 + dp2);
        final String text = chartData.fullDates[startIndex] + " - " + chartData.fullDates[endIndex - 1];
        final float measureText = titlePaint.measureText(text);
        canvas.drawText(text, callbacks.getWidth() - chartPadding - measureText, dp12 + dp12 + dp8, titlePaint);

        canvas.translate(0, chartTitleHeight);

        final float drawingWidth = getDrawingWidth();

        linesPaint.setStrokeWidth(lineWidth / 2f);
        linesPaint.setColor(MainActivity.NIGHT_MODE ? 0x19FFFFFF : 0x19182D3B);
        final int maxLegendAlpha = MainActivity.NIGHT_MODE ? 0x99 : 0xFF;
        legendPaint.setColor(MainActivity.NIGHT_MODE ? 0x99A3B1C2 : 0xFF8E8E93);

        float showingDatesCount = (endIndex - startIndex) * 1f / stepX;
        int oddDatesAlpha = maxLegendAlpha - (int) ((showingDatesCount - VER_DATES) / VER_DATES * 0xFF);
        for (int i = startIndex / stepX; i < (endIndex - 1) / stepX + 1; i++) {
            float x = (i * stepX - startIndex * 1f) / (endIndex - startIndex) * drawingWidth + chartPadding;
            legendPaint.setAlpha(i * stepX % (stepX * 2) == 0 ? maxLegendAlpha : oddDatesAlpha);
            final float dateTextWidth = legendPaint.measureText(chartData.dates[i * stepX]);
            canvas.drawText(chartData.dates[i * stepX], x - dateTextWidth / 2, chartHeight + dp12 + dp4, legendPaint);
        }

        final float wid = (maxX - minX) / drawingWidth;
        final float widP = (totalMaxX - totalMinX) / drawingWidth;
        final float dYP = chartHeight + datesHeight + periodSelectorHeight;

        canvas.translate(chartPadding, 0);

        for (int c = 0; c < chartData.lines.size(); c++) {
            final float hei = (maxY[c] - minY[c]) / chartHeight;
            final float heiP = (totalMaxY[c] - totalMinY[c]) / periodSelectorHeight;
            final ChartData.Line line = chartData.lines.get(c);
            if (line.isVisible()) {
                int q = 0;
                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _x = ((float) chartData.xValues[i] - minX) / wid;
                    final float _y = chartHeight - ((float) line.values[i] - minY[c]) / hei;

                    points[q++] = _x;
                    points[q++] = _y;
                    if (i != 0 && i != chartData.xValues.length - 1) {
                        points[q++] = _x;
                        points[q++] = _y;
                    }
                }

                line.linePaint.setAlpha(line.alpha);
                canvas.drawLines(points, line.linePaint);

                q = 0;
                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _x = ((float) chartData.xValues[i] - totalMinX) / widP;
                    final float _y = dYP - ((float) line.values[i] - totalMinY[c]) / heiP;

                    points[q++] = _x;
                    points[q++] = _y;
                    if (i != 0 && i != chartData.xValues.length - 1) {
                        points[q++] = _x;
                        points[q++] = _y;
                    }
                }

                line.linePeriodPaint.setAlpha(line.alpha);
                canvas.save();
                canvas.clipPath(periodSelectorClipPath);
                canvas.drawLines(points, 0, q, line.linePeriodPaint);
                canvas.restore();
            }
        }

        // Y lines and axis-values
        for (int c = 0; c < 2; c++) {
            final ValueAnimator opacityAnimator = opacityAnimators.get(chartData.lines.get(c).name);
            final float opacityFraction = opacityAnimator != null ? (int) opacityAnimator.getAnimatedValue() / 255f : 1;
            legendPaint.setColor(chartData.lines.get(c).color);

            if (stepY[c] > 0) {
                linesPaint.setAlpha((int) (yLimitsAnimator[c].getAnimatedFraction() * 0x10 / 2));
                legendPaint.setAlpha((int) (yLimitsAnimator[c].getAnimatedFraction() * maxLegendAlpha * opacityFraction));
                for (int i = 0; i < HOR_LINES; i++) {
                    final float valueY = stepsY[c][i];
                    final float centerY = (minY[c] + maxY[c]) / 2f;
                    final float y = (0.5f - (valueY - centerY) / (maxY[c] - minY[c])) * chartHeight;
                    canvas.drawLine(0, y, drawingWidth, y, linesPaint);
                    final String formatY = formatY((int) valueY);
                    canvas.drawText(formatY, c == 0 ? 0 : (drawingWidth - legendPaint.measureText(formatY)), y - dp6, legendPaint);
                }
            }
            if (prevStepY[c] > 0) {
                linesPaint.setAlpha((int) (0x10 - yLimitsAnimator[c].getAnimatedFraction() * 0x10 / 2));
                legendPaint.setAlpha((int) (maxLegendAlpha - yLimitsAnimator[c].getAnimatedFraction() * maxLegendAlpha * (1 - opacityFraction)));

                for (int i = 0; i < HOR_LINES; i++) {
                    final float valueY = prevStepsY[c][i];
                    final float centerY = (minY[c] + maxY[c]) / 2f;
                    final float y = (0.5f - (valueY - centerY) / (maxY[c] - minY[c])) * chartHeight;
                    canvas.drawLine(0, y, drawingWidth, y, linesPaint);
                    final String formatY = formatY((int) valueY);
                    canvas.drawText(formatY, c == 0 ? 0 : (drawingWidth - legendPaint.measureText(formatY)), y - dp6, legendPaint);
                }
            }
        }

        if (selectedIndex >= 0 && chartData.isAnyLineVisible()) {

            final float _x = ((float) chartData.xValues[selectedIndex] - minX) / wid;

            if (-chartPadding < _x && _x < callbacks.getWidth() - chartPadding) {
                final float changeFraction = selectedIndexAnimator.getAnimatedFraction();

                linesPaint.setStrokeWidth(lineWidth / 2f);
                linesPaint.setColor(MainActivity.NIGHT_MODE ? 0x19FFFFFF : 0x19182D3B);
                canvas.drawLine(_x, 0, _x, chartHeight, linesPaint);

                final boolean panelLefted = _x < callbacks.getWidth() / 2 - chartPadding;
                final float prevX = prevSelectedIndex >= 0 ? ((float) chartData.xValues[prevSelectedIndex] - minX) / wid : _x;
                final float panelAnchor = _x + (prevX - _x) * (1 - changeFraction);

                final float panelLeft = panelAnchor + (panelLefted ? dp12 * 2 : dp12 * -14);
                final float panelRight = panelAnchor + (panelLefted ? dp12 * 14 : dp12 * -2);

                linesPaint.setStrokeWidth(lineWidth);
                periodPaint.setColor(MainActivity.NIGHT_MODE ? 0xFF1c2533 : Color.WHITE);

                final float lineHeight = dp12 * 2;
                float lineY = lineHeight;

                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);
                    if (line.isVisibleOrWillBe) {
                        linesPaint.setColor(line.color);
                        linesPaint.setAlpha(line.alpha);

                        final float hei = (maxY[c] - minY[c]) / chartHeight;
                        final float _y = chartHeight - ((float) line.values[selectedIndex] - minY[c]) / hei;
                        canvas.drawCircle(_x, _y, dp4, periodPaint);
                        canvas.drawCircle(_x, _y, dp4, linesPaint);

                        lineY += lineHeight;
                    }
                }

                final float panelPadding = dp12;

                panelRectOnScreen.set(
                        panelLeft - panelPadding,
                        chartTitleHeight,
                        panelRight + panelPadding,
                        chartTitleHeight + lineY + panelPadding
                );

                canvas.drawRoundRect(
                        panelLeft - panelPadding,
                        0,
                        panelRight + panelPadding,
                        lineY + panelPadding,
                        dp4, dp4, periodPaint);
                linesPaint.setStrokeWidth(lineWidth / 2f);
                linesPaint.setColor(MainActivity.NIGHT_MODE ? 0x77000000 : 0x99CCCCCC);
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

                lineY = lineHeight;

                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);
                    if (line.isVisibleOrWillBe) {
                        line.panelTextPaint.setAlpha(prevSelectedIndex >= 0 ? (int) (0xFF * changeFraction) : 0XFF);
                        lineY += lineHeight;
                        String formatY = toStringWithSpaces(line.values[selectedIndex]);
                        float valueWidth = titlePaint.measureText(formatY);
                        canvas.drawText(formatY, panelRight - valueWidth, lineY + (prevSelectedIndex >= 0 ? translationAppear : 0), line.panelTextPaint);
                    }
                }
                if (prevSelectedIndex >= 0) {
                    lineY = lineHeight;
                    for (int c = 0; c < chartData.lines.size(); c++) {
                        final ChartData.Line line = chartData.lines.get(c);
                        if (line.isVisibleOrWillBe) {
                            line.panelTextPaint.setAlpha(0XFF - (int) (0xFF * changeFraction));
                            lineY += lineHeight;
                            String formatY = toStringWithSpaces(line.values[prevSelectedIndex]);
                            float valueWidth = titlePaint.measureText(formatY);
                            canvas.drawText(formatY, panelRight - valueWidth, lineY + translationDisappear, line.panelTextPaint);
                        }
                    }
                }
                titlePaint.setAlpha(0xFF);
            } else {
                panelRectOnScreen.set(0, 0, 0, 0);
            }
        } else {
            panelRectOnScreen.set(0, 0, 0, 0);
        }

        canvas.translate(0, chartHeight + datesHeight);

        float startX = startIndex * 1f / chartData.xValues.length * drawingWidth;
        float endX = endIndex * 1f / chartData.xValues.length * drawingWidth;

        periodPaint.setColor(MainActivity.NIGHT_MODE ? 0x99304259 : 0x99E2EEF9);

        final float borderHor = dp2 / 2;
        final float borderVer = dp12;

        // period's outside.
        path.addRoundRect(0, 0, startX + borderVer, periodSelectorHeight, radiiLeft, Path.Direction.CW);
        canvas.drawPath(path, periodPaint);
        path.rewind();
        path.addRoundRect(endX - borderVer, 0, drawingWidth, periodSelectorHeight, radiiRight, Path.Direction.CW);
        canvas.drawPath(path, periodPaint);
        path.rewind();

        periodPaint.setColor(MainActivity.NIGHT_MODE ? 0x996F899E : 0x8086A9C4);

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
                startX + borderVer / 2 - dp2 / 2,
                periodSelectorHeight / 2 - dp6,
                startX + borderVer / 2 + dp2 / 2,
                periodSelectorHeight / 2 + dp6,
                dp2,
                dp2,
                periodPaint
        );
        canvas.drawRoundRect(
                endX - borderVer / 2 - dp2 / 2,
                periodSelectorHeight / 2 - dp6,
                endX - borderVer / 2 + dp2 / 2,
                periodSelectorHeight / 2 + dp6,
                dp2,
                dp2,
                periodPaint
        );

        canvas.restore();

        if (chartData.lines.size() > 1) {
            final float chipCornerRadius = dp12 * 4;
            float currentLineX = chartPadding;
            float currentLineY = chartTitleHeight + chartHeight + datesHeight + periodSelectorHeight + chipsMarginTop + chipMargin;
            for (int c = 0; c < chartData.lines.size(); c++) {
                final ChartData.Line line = chartData.lines.get(c);
                float chipWidth = line.chipTextWidth + 2 * chipPadding;

                if (chipWidth > drawingWidth - currentLineX) {
                    currentLineX = chartPadding;
                    currentLineY += chipHeight + chipMargin;
                }

                line.chipRectOnScreen.set(currentLineX, currentLineY, currentLineX + chipWidth, currentLineY + chipHeight);

                if (line.isVisibleOrWillBe) {
                    periodPaint.setColor(line.color);
                    canvas.drawRoundRect(line.chipRectOnScreen, chipCornerRadius, chipCornerRadius, periodPaint);

                    final int checkLeft = (int) (line.chipRectOnScreen.left + chipPadding / 2 - dp2);
                    final int checkTop = (int) (line.chipRectOnScreen.top + chipPadding / 4 + dp2);
                    drawableCheck.setBounds(
                            checkLeft,
                            checkTop,
                            checkLeft + drawableCheck.getIntrinsicWidth(),
                            checkTop + drawableCheck.getIntrinsicHeight()
                    );
                    drawableCheck.draw(canvas);
                }
                canvas.drawRoundRect(line.chipRectOnScreen, chipCornerRadius, chipCornerRadius, line.chipBorderPaint);

                float textTransitionX = line.isVisibleOrWillBe ? chipPadding / 4 + dp2 : 0;
                canvas.drawText(
                        line.name,
                        currentLineX + chipPadding + textTransitionX,
                        currentLineY + chipPadding,
                        line.isVisibleOrWillBe ? chipWhiteTextPaint : line.chipTextPaint
                );

                if (lastDownX >= 0 && line.chipRectOnScreen.contains(lastDownX, lastDownY)) {
                    periodPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    periodPaint.setColor(MainActivity.NIGHT_MODE && !line.isVisibleOrWillBe ? 0x54727272 : 0x54B0B0B0);
                    canvas.drawRoundRect(line.chipRectOnScreen, chipCornerRadius, chipCornerRadius, periodPaint);
                    periodPaint.setStyle(Paint.Style.FILL);
                }

                currentLineX += chipWidth + chipMargin;
            }
        }
    }

    private float getDrawingWidth() {
        return callbacks.getWidth() - 2 * chartPadding;
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

    private static String toStringWithSpaces(int y) {
        if (y == 0) return "0";

        StringBuilder stringBuilder = new StringBuilder();
        int q = 0;
        while (y > 0) {
            stringBuilder.append(y % 10);
            y /= 10;
            ++q;
            if (q == 3) {
                q = 0;
                stringBuilder.append(' ');
            }
        }
        return stringBuilder.reverse().toString();
    }
}
