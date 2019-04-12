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

import java.util.HashMap;
import java.util.Map;

public class ChartViewDelegateArea implements Delegate {

    private static final long ANIMATION_DURATION = 200L;

    private final Resources resources;
    private final Context context;

    private final Callbacks callbacks;

    public ChartViewDelegateArea(Context context, String title, final ChartData chartData, final Callbacks callbacks) {
        this.callbacks = callbacks;
        this.context = context;
        this.resources = context.getResources();

        this.title = title;

        linesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linesPaint.setStyle(Paint.Style.STROKE);

        this.chartData = chartData;
        points = new float[chartData.xValues.length * 4];
        sums = new float[chartData.xValues.length];
        totalSums = new float[chartData.xValues.length];

        periodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        periodPaint.setStyle(Paint.Style.FILL);

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
        dp12 = getResources().getDimension(R.dimen.dp12);
        textPaint.setTextSize(dp12 - dp12 / 12f);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.BLACK);
        titlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        chartHeight = getResources().getDimension(R.dimen.chart_height);
        datesHeight = getResources().getDimension(R.dimen.dates_height);
        chartTitleHeight = getResources().getDimension(R.dimen.chart_title_height) + dp12 * 2;
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
            line.linePaint.setStyle(Paint.Style.FILL_AND_STROKE);

            line.linePeriodPaint.setColor(line.color);
            line.linePeriodPaint.setStyle(Paint.Style.FILL_AND_STROKE);

            line.chipTextPaint.setColor(line.color);
            line.chipTextPaint.setTextSize(chipTextSize);
            line.chipTextWidth = line.chipTextPaint.measureText(line.name);

            line.chipBorderPaint.setColor(line.color);
            line.chipBorderPaint.setStyle(Paint.Style.STROKE);
            line.chipBorderPaint.setStrokeWidth(lineWidth / 1.5f);

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
    private final Path barsPath = new Path();
    private final float[] radiiLeft;
    private final float[] radiiRight;

    private final String title;
    private final ChartData chartData;
    private final Paint linesPaint;
    private final float[] points;
    private final float[] sums;
    private final float[] totalSums;

    private final Paint periodPaint;
    private final Paint textPaint;
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
    private float stepY;
    private float prevStepY;

    private final String[] formattedYSteps = new String[HOR_LINES];
    private final String[] formattedPrevYSteps = new String[HOR_LINES];

    private int minY;
    private int maxY;

    private int totalMinY;
    private int totalMaxY;

    private ValueAnimator yLimitsAnimator;
    private ValueAnimator selectedIndexAnimator;

    private Map<String, ValueAnimator> opacityAnimators = new HashMap<>();

    private final Drawable drawableCheck;

    private final Path periodSelectorClipPath;

    @Override
    public int measureHeight() {
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

    void setLineVisible(String name, boolean visible) {
        for (final ChartData.Line line : chartData.lines) {
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

    private float pendingMinY;
    private float pendingTotalMinY;
    private float pendingMaxY;
    private float pendingTotalMaxY;

    private void animateYLimits() {
        final int[] yLimits = chartData.calcYLimits(startIndex, endIndex);

        final float startMinY = minY;
        final float endMinY = yLimits[0];
        final float startTotalMinY = totalMinY;
        final float endTotalMinY = yLimits[2];

        final float startMaxY = maxY;
        final float endMaxY = yLimits[1];
        final float startTotalMaxY = totalMaxY;
        final float endTotalMaxY = yLimits[3];

        if (pendingMinY == endMinY && pendingTotalMinY == endTotalMinY && pendingMaxY == endMaxY && pendingTotalMaxY == endTotalMaxY) {
            callbacks.invalidate();
            return;
        }

        pendingMinY = endMinY;
        pendingTotalMinY = endTotalMinY;
        pendingMaxY = endMaxY;
        pendingTotalMaxY = endTotalMaxY;

        final float dY = endMaxY - endMinY;
        stepY = dY / (HOR_LINES - 1f);
        for (int i = 0; i < formattedYSteps.length; i++) {
            formattedYSteps[i] = formatY((int) (endMinY + stepY * i));
        }

        if (yLimitsAnimator == null) {
            yLimitsAnimator = ValueAnimator.ofFloat(0f, 1f);
            yLimitsAnimator.setInterpolator(new DecelerateInterpolator());
            yLimitsAnimator.setDuration(ANIMATION_DURATION);
            yLimitsAnimator.addListener(new AnimatorListenerAdapter() {
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
            yLimitsAnimator.removeAllUpdateListeners();
        }

        yLimitsAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                minY = (int) (startMinY + (endMinY - startMinY) * animation.getAnimatedFraction());
                totalMinY = (int) (startTotalMinY + (endTotalMinY - startTotalMinY) * animation.getAnimatedFraction());

                maxY = (int) (startMaxY + (endMaxY - startMaxY) * animation.getAnimatedFraction());
                totalMaxY = (int) (startTotalMaxY + (endTotalMaxY - startTotalMaxY) * animation.getAnimatedFraction());

                callbacks.invalidate();
            }
        });
        yLimitsAnimator.start();
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

        canvas.translate(0, chartTitleHeight);

        final float drawingWidth = getDrawingWidth();

        linesPaint.setStrokeWidth(lineWidth / 2f);
        linesPaint.setColor(MainActivity.NIGHT_MODE ? 0x19FFFFFF : 0x19182D3B);
        textPaint.setColor(MainActivity.NIGHT_MODE ? 0x80FFFFFF : 0x80182D3B);

        float showingDatesCount = (endIndex - startIndex) * 1f / stepX;
        int oddDatesAlpha = 0x80 - (int) ((showingDatesCount - VER_DATES) / VER_DATES * 0x80);
        for (int i = startIndex / stepX; i < (endIndex - 1) / stepX + 1; i++) {
            float x = (i * stepX - startIndex * 1f) / (endIndex - startIndex) * drawingWidth + chartPadding;
            textPaint.setAlpha(i * stepX % (stepX * 2) == 0 ? 0x80 : oddDatesAlpha);
            final float dateTextWidth = textPaint.measureText(chartData.dates[i * stepX]);
            canvas.drawText(chartData.dates[i * stepX], x - dateTextWidth / 2, chartHeight + dp12 + dp4, textPaint);
        }

        final float wid = (maxX - minX) / drawingWidth;
        final float hei = (maxY - minY) / chartHeight;
        final float widP = (totalMaxX - totalMinX) / drawingWidth;
        final float heiP = (totalMaxY - totalMinY) / periodSelectorHeight;
        final float dYP = chartHeight + datesHeight + periodSelectorHeight;
        final int div = 1;

        canvas.translate(chartPadding, 0);

        for (int i = 0; i < chartData.xValues.length; i++) {
            sums[i] = 0;
            totalSums[i] = 0;
        }

        for (int c = 0; c < chartData.lines.size(); c++) {
            final ChartData.Line line = chartData.lines.get(c);
            if (line.isVisible()) {
                int q = 0;
                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _xLeft = ((float) chartData.xValues[i] - minX - chartData.xStep / 2) / wid;
                    final float _xRight = ((float) chartData.xValues[i] - minX + chartData.xStep / 2) / wid;
                    final float _yBottom = chartHeight - (sums[i] - minY) / hei;
                    sums[i] += line.values[i] * (line.alpha / 255f);
                    final float _yTop = chartHeight - (sums[i] - minY) / hei;

                    if (_xLeft < -chartPadding - dp12 || _xRight > drawingWidth + chartPadding + dp12) {
                        continue;
                    }

                    points[q++] = _xLeft;
                    points[q++] = _yTop;
                    points[q++] = _xRight;
                    points[q++] = _yBottom;
                }

                barsPath.moveTo(points[0], chartHeight);
                for (int i = 0; i < q / 4; i++) {
                    barsPath.lineTo(points[i * 4], points[i * 4 + 1]);
                    barsPath.lineTo(points[i * 4 + 2], points[i * 4 + 1]);
                }
                for (int i = q / 4 - 1; i >= 0; i--) {
                    barsPath.lineTo(points[i * 4 + 2], points[i * 4 + 3]);
                    barsPath.lineTo(points[i * 4], points[i * 4 + 3]);
                }
                barsPath.close();
                canvas.drawPath(barsPath, line.linePaint);
                barsPath.reset();

                q = 0;
                for (int i = 0; i < chartData.xValues.length; i++) {
                    if (i % div != 0) {
                        totalSums[i] += line.values[i] * (line.alpha / 255f);
                        continue;
                    }
                    final float _xLeft = ((float) chartData.xValues[i] - totalMinX - chartData.xStep / 2) / widP;
                    final float _xRight = ((float) chartData.xValues[i] - totalMinX + chartData.xStep / 2) / widP;
                    final float _yBottom = dYP - (totalSums[i] - totalMinY) / heiP;
                    totalSums[i] += line.values[i] * (line.alpha / 255f);
                    final float _yTop = dYP - (totalSums[i] - totalMinY) / heiP;

                    points[q++] = _xLeft;
                    points[q++] = _yTop;
                    points[q++] = _xRight;
                    points[q++] = _yBottom;
                }

                barsPath.moveTo(points[0], chartHeight);
                for (int i = 0; i < q / 4; i++) {
                    barsPath.lineTo(points[i * 4], points[i * 4 + 1]);
                    barsPath.lineTo(points[i * 4 + 2], points[i * 4 + 1]);
                }
                for (int i = q / 4 - 1; i >= 0; i--) {
                    barsPath.lineTo(points[i * 4 + 2], points[i * 4 + 3]);
                    barsPath.lineTo(points[i * 4], points[i * 4 + 3]);
                }
                barsPath.close();
                canvas.save();
                canvas.clipPath(periodSelectorClipPath);
                canvas.drawPath(barsPath, line.linePeriodPaint);
                canvas.restore();
                barsPath.reset();
            }
        }

        if (selectedIndex >= 0 && chartData.isAnyLineVisible()) {
            periodPaint.setColor(MainActivity.NIGHT_MODE ? 0x80242F3E : 0x80FFFFFF);
            canvas.drawRect(
                    ((float) chartData.xValues[0] - minX - chartData.xStep / 2) / wid,
                    -chartTitleHeight,
                    ((float) chartData.xValues[selectedIndex] - minX - chartData.xStep / 2) / wid,
                    chartHeight,
                    periodPaint
            );
            canvas.drawRect(
                    ((float) chartData.xValues[selectedIndex] - minX + chartData.xStep / 2) / wid,
                    -chartTitleHeight,
                    ((float) chartData.xValues[chartData.xValues.length - 1] - minX + chartData.xStep / 2) / wid,
                    chartHeight,
                    periodPaint
            );
        }

        // title
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setColor(MainActivity.NIGHT_MODE ? Color.WHITE : Color.BLACK);
        titlePaint.setTextSize(dp12 + dp4);
        canvas.drawText(title, 0, -chartTitleHeight + dp12 + dp12 + dp8, titlePaint);
        titlePaint.setTextSize(dp12 + dp2);
        final String text = chartData.fullDates[startIndex] + " - " + chartData.fullDates[endIndex - 1];
        final float measureText = titlePaint.measureText(text);
        canvas.drawText(text, callbacks.getWidth() - chartPadding * 2 - measureText, -chartTitleHeight + dp12 + dp12 + dp8, titlePaint);

        // Y lines and axis-values
        linesPaint.setAlpha((int) (yLimitsAnimator.getAnimatedFraction() * 0x19));
        textPaint.setAlpha((int) (yLimitsAnimator.getAnimatedFraction() * 0x80));
        for (int i = 0; i < HOR_LINES; i++) {
            final float valueY = minY + stepY * i;
            final float y = (1 - (valueY - minY) / (maxY - minY)) * chartHeight;
            canvas.drawLine(0, y, drawingWidth, y, linesPaint);
            canvas.drawText(formattedYSteps[i], 0, y - dp6, textPaint);
        }
        if (prevStepY > 0) {
            linesPaint.setAlpha((int) (0x19 - yLimitsAnimator.getAnimatedFraction() * 0x19));
            textPaint.setAlpha((int) (0x80 - yLimitsAnimator.getAnimatedFraction() * 0x80));

            for (int i = 0; i < HOR_LINES; i++) {
                final float valueY = minY + prevStepY * i;
                final float y = (1 - (valueY - minY) / (maxY - minY)) * chartHeight;
                canvas.drawLine(0, y, drawingWidth, y, linesPaint);
                canvas.drawText(formattedPrevYSteps[i], 0, y - dp6, textPaint);
            }
        }

        if (selectedIndex >= 0 && chartData.isAnyLineVisible()) {

            final float _x = ((float) chartData.xValues[selectedIndex] - minX) / wid;

            if (-chartPadding < _x && _x < callbacks.getWidth() - chartPadding) {
                final float changeFraction = selectedIndexAnimator.getAnimatedFraction();

                final boolean panelLefted = _x < callbacks.getWidth() / 2 - chartPadding;
                final float prevX = prevSelectedIndex >= 0 ? ((float) chartData.xValues[prevSelectedIndex] - minX) / wid : _x;
                final float panelAnchor = _x + (prevX - _x) * (1 - changeFraction);

                final float panelLeft = panelAnchor + (panelLefted ? dp12 * -1 : dp12 * -11);
                final float panelRight = panelAnchor + (panelLefted ? dp12 * 11 : dp12 * 1);

                periodPaint.setColor(MainActivity.NIGHT_MODE ? 0xFF1c2533 : Color.WHITE);

                final float lineHeight = dp12 * 2;
                float lineY = lineHeight;

                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);
                    if (line.isVisibleOrWillBe) {
                        lineY += lineHeight;
                    }
                }
                lineY += lineHeight;// "All" text

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
                lineY += lineHeight;
                canvas.drawText("All", panelLeft, lineY, titlePaint);

                lineY = lineHeight;

                titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
                int sum = 0;
                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);
                    if (line.isVisibleOrWillBe) {
                        sum += line.values[selectedIndex];
                        line.panelTextPaint.setAlpha(prevSelectedIndex >= 0 ? (int) (0xFF * changeFraction) : 0XFF);
                        lineY += lineHeight;
                        String formatY = toStringWithSpaces(line.values[selectedIndex]);
                        float valueWidth = titlePaint.measureText(formatY);
                        canvas.drawText(formatY, panelRight - valueWidth, lineY + (prevSelectedIndex >= 0 ? translationAppear : 0), line.panelTextPaint);
                    }
                }
                titlePaint.setAlpha(prevSelectedIndex >= 0 ? (int) (0xFF * changeFraction) : 0XFF);
                lineY += lineHeight;
                String formatSum = toStringWithSpaces(sum);
                float sumWidth = titlePaint.measureText(formatSum);
                canvas.drawText(formatSum, panelRight - sumWidth, lineY + (prevSelectedIndex >= 0 ? translationAppear : 0), titlePaint);

                if (prevSelectedIndex >= 0) {
                    int prevSum = 0;
                    lineY = lineHeight;
                    for (int c = 0; c < chartData.lines.size(); c++) {
                        final ChartData.Line line = chartData.lines.get(c);
                        if (line.isVisibleOrWillBe) {
                            prevSum += line.values[prevSelectedIndex];
                            line.panelTextPaint.setAlpha(0XFF - (int) (0xFF * changeFraction));
                            lineY += lineHeight;
                            String formatY = toStringWithSpaces(line.values[prevSelectedIndex]);
                            float valueWidth = titlePaint.measureText(formatY);
                            canvas.drawText(formatY, panelRight - valueWidth, lineY + translationDisappear, line.panelTextPaint);
                        }
                    }
                    titlePaint.setAlpha(0XFF - (int) (0xFF * changeFraction));
                    lineY += lineHeight;
                    String formatPrevSum = toStringWithSpaces(prevSum);
                    float prevSumWidth = titlePaint.measureText(formatPrevSum);
                    canvas.drawText(formatPrevSum, panelRight - prevSumWidth, lineY + translationDisappear, titlePaint);
                }
                titlePaint.setTypeface(Typeface.DEFAULT);

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
