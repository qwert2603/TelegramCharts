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
import java.util.List;
import java.util.Map;

public class ChartViewDelegateLines implements Delegate {

    private static final long ANIMATION_DURATION = 300L;

    private final Resources resources;
    private final Context context;

    private final Callbacks callbacks;

    public ChartViewDelegateLines(Context context, String title, final ChartData chartData, final Callbacks callbacks) {
        this.callbacks = callbacks;
        this.context = context;
        this.resources = context.getResources();

        this.title = title;

        this.chartData = chartData;
        points = new float[(chartData.xValues.length - 1) * 4];

        int[] yLimits = chartData.calcYLimits(startIndex, endIndex);
        minY = yLimits[0];
        maxY = yLimits[1];
        totalMinY = yLimits[2];
        totalMaxY = yLimits[3];

        setPeriodIndices(0, chartData.xValues.length);

        totalMinX = minX;
        totalMaxX = maxX;

        dp12 = getResources().getDimension(R.dimen.dp12);
        dp2 = getResources().getDimension(R.dimen.dp2);
        dp4 = getResources().getDimension(R.dimen.dp4);
        dp6 = getResources().getDimension(R.dimen.dp6);
        dp8 = getResources().getDimension(R.dimen.dp8);
        float lineWidth = getResources().getDimension(R.dimen.line_width);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        titlePaint.setTextSize(dp12 + dp4);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);

        datesRangePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        datesRangePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        datesRangePaint.setTextSize(dp12 + dp2);
        datesRangePaint.setTypeface(Typeface.DEFAULT_BOLD);

        legendDatesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legendDatesPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        legendDatesPaint.setTextSize(dp12 - dp12 / 12f);
        legendDatesPaint.setTextAlign(Paint.Align.CENTER);

        legendYStepsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legendYStepsPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        legendYStepsPaint.setTextSize(dp12 - dp12 / 12f);

        periodSelectorOutsidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        periodSelectorOutsidePaint.setStyle(Paint.Style.FILL);
        periodSelectorDragBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        periodSelectorDragBorderPaint.setStyle(Paint.Style.FILL);
        periodSelectorWhiteDragPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        periodSelectorWhiteDragPaint.setStyle(Paint.Style.FILL);
        periodSelectorWhiteDragPaint.setColor(0xD7FFFFFF);

        yLinesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        yLinesPaint.setStyle(Paint.Style.STROKE);
        yLinesPaint.setStrokeWidth(lineWidth / 2f);

        linesPaints = new Paint[chartData.lines.size()];
        for (int i = 0; i < chartData.lines.size(); i++) {
            final ChartData.Line line = chartData.lines.get(i);
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(line.color);
            paint.setStrokeWidth(lineWidth * 0.9f);
            paint.setStrokeCap(Paint.Cap.SQUARE);
            linesPaints[i] = paint;
        }

        periodSelectorLinesPaints = new Paint[chartData.lines.size()];
        for (int i = 0; i < chartData.lines.size(); i++) {
            final ChartData.Line line = chartData.lines.get(i);
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(line.color);
            paint.setStrokeWidth(lineWidth / 2f * 0.9f);
            paint.setStrokeCap(Paint.Cap.BUTT);
            periodSelectorLinesPaints[i] = paint;
        }

        //chips
        chipsMarginTop = dp6;
        chipMargin = dp12;
        chipPadding = dp12 + dp12;
        float chipTextSize = dp12 + dp2;
        chipHeight = chipPadding + chipTextSize;

        chipWhiteTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        chipWhiteTextPaint.setColor(Color.WHITE);
        chipWhiteTextPaint.setTextSize(chipTextSize);

        highlightChipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightChipPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        chipsRectOnScreen = new RectF[chartData.lines.size()];
        chipsTextWidth = new float[chartData.lines.size()];
        chipsFillPaint = new Paint[chartData.lines.size()];
        chipsTextPaint = new Paint[chartData.lines.size()];
        chipsBorderPaint = new Paint[chartData.lines.size()];

        for (int i = 0; i < chartData.lines.size(); i++) {
            final ChartData.Line line = chartData.lines.get(i);

            chipsRectOnScreen[i] = new RectF();

            chipsTextPaint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            chipsTextPaint[i].setColor(line.color);
            chipsTextPaint[i].setTextSize(chipTextSize);

            chipsTextWidth[i] = chipsTextPaint[i].measureText(line.name) + 2 * chipPadding;

            chipsBorderPaint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            chipsBorderPaint[i].setColor(line.color);
            chipsBorderPaint[i].setStyle(Paint.Style.STROKE);
            chipsBorderPaint[i].setStrokeWidth(lineWidth / 2f);

            chipsFillPaint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            chipsFillPaint[i].setColor(line.color);
            chipsFillPaint[i].setStyle(Paint.Style.FILL);
        }

        selectedXLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedXLinePaint.setStyle(Paint.Style.STROKE);
        selectedXLinePaint.setStrokeWidth(lineWidth / 2f);

        selectedCircleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedCircleFillPaint.setStyle(Paint.Style.FILL);

        panelDatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        panelDatePaint.setTextSize(dp12 + dp2);
        panelDatePaint.setTypeface(Typeface.DEFAULT_BOLD);

        panelLinesNamesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        panelLinesNamesPaint.setTextSize(dp12 + dp2);

        selectedCirclesStrokePaint = new Paint[chartData.lines.size()];
        for (int i = 0; i < chartData.lines.size(); i++) {
            final ChartData.Line line = chartData.lines.get(i);
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(line.color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(lineWidth);
            selectedCirclesStrokePaint[i] = paint;
        }

        panelShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        panelShadowPaint.setStyle(Paint.Style.STROKE);
        panelShadowPaint.setStrokeWidth(lineWidth / 2f);

        panelBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        panelBackgroundPaint.setStyle(Paint.Style.FILL);

        panelYValuesPaint = new Paint[chartData.lines.size()];
        for (int i = 0; i < chartData.lines.size(); i++) {
            final ChartData.Line line = chartData.lines.get(i);
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(line.color);
            paint.setTextSize(dp12 + dp2);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.RIGHT);
            panelYValuesPaint[i] = paint;
        }

        chartHeight = getResources().getDimension(R.dimen.chart_height);
        datesHeight = getResources().getDimension(R.dimen.dates_height);
        chartTitleHeight = getResources().getDimension(R.dimen.chart_title_height);
        periodSelectorHeight = getResources().getDimension(R.dimen.period_selector_height);
        minPeriodSelectorWidth = getResources().getDimension(R.dimen.min_period_selector_width);
        periodSelectorDraggableWidth = getResources().getDimension(R.dimen.period_selector_draggable_width);

        chartPadding = getResources().getDimension(R.dimen.chart_padding);

        // period selector
        final float fillRadius = dp6;
        radiiLeft = new float[]{fillRadius, fillRadius, 0, 0, 0, 0, fillRadius, fillRadius};
        radiiRight = new float[]{0, 0, fillRadius, fillRadius, fillRadius, fillRadius, 0, 0};
        periodSelectorClipPath = new Path();
        periodSelectorClipPath.addRoundRect(
                0,
                0,
                getDrawingWidth(),
                periodSelectorHeight,
                new float[]{fillRadius, fillRadius, fillRadius, fillRadius, fillRadius, fillRadius, fillRadius, fillRadius},
                Path.Direction.CW
        );

        gestureDetector = new GestureDetector(this.context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (chartTitleHeight + chartHeight + datesHeight + periodSelectorHeight < e.getY()) {
                    List<ChartData.Line> lines = chartData.lines;
                    for (int i = 0; i < lines.size(); i++) {
                        if (chipsRectOnScreen[i].contains(e.getX(), e.getY())) {
                            ChartData.Line line = lines.get(i);
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
                    List<ChartData.Line> lines = chartData.lines;
                    for (int i = 0; i < lines.size(); i++) {
                        if (chipsRectOnScreen[i].contains(e.getX(), e.getY())) {
                            setOnlyOneLineVisible(lines.get(i).name);
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

        setNightMode(MainActivity.NIGHT_MODE);
    }

    private Resources getResources() {
        return resources;
    }

    private final GestureDetector gestureDetector;
    private final RectF panelRectOnScreen = new RectF();

    private int maxLegendAlpha;

    private final float chartHeight;
    private final float datesHeight;
    private final float chartTitleHeight;
    private final float periodSelectorHeight;
    private final float minPeriodSelectorWidth;
    private final float periodSelectorDraggableWidth;
    private final float chartPadding;
    private final float dp2;
    private final float dp4;
    private final float dp6;
    private final float dp8;
    private final float dp12;
    private final float chipsMarginTop;
    private final float chipMargin;
    private final float chipPadding;
    private final float chipHeight;

    private final Path path = new Path();
    private final float[] radiiLeft;
    private final float[] radiiRight;

    private final String title;
    private final ChartData chartData;
    private final Paint panelBackgroundPaint;
    private final Paint panelShadowPaint;
    private final float[] points;

    private final Paint periodSelectorOutsidePaint;
    private final Paint periodSelectorDragBorderPaint;
    private final Paint periodSelectorWhiteDragPaint;
    private final Paint legendDatesPaint;
    private final Paint legendYStepsPaint;
    private final Paint yLinesPaint;
    private final Paint titlePaint;
    private final Paint datesRangePaint;
    private final Paint chipWhiteTextPaint;
    private final Paint highlightChipPaint;

    private final Paint[] linesPaints;
    private final Paint[] periodSelectorLinesPaints;

    private final RectF[] chipsRectOnScreen;
    private final float[] chipsTextWidth;
    private final Paint[] chipsBorderPaint;
    private final Paint[] chipsFillPaint;
    private final Paint[] chipsTextPaint;

    private final Paint selectedXLinePaint;
    private final Paint selectedCircleFillPaint;
    private final Paint[] selectedCirclesStrokePaint;
    private final Paint panelDatePaint;
    private final Paint panelLinesNamesPaint;
    private final Paint[] panelYValuesPaint;

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

    // step on X-axis (in indices).
    private int stepX;
    private float stepY;
    private float prevStepY;

    private final float[] stepsY = new float[HOR_LINES];
    private final float[] prevStepsY = new float[HOR_LINES];

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
    public int measureHeight(int width) {
        float currentLineX = chartPadding;
        float currentLineY = chartTitleHeight + chartHeight + datesHeight + periodSelectorHeight + chipsMarginTop + chipMargin;
        if (chartData.lines.size() > 1) {
            for (int c = 0; c < chartData.lines.size(); c++) {
                final float chipWidth = chipsTextWidth[c] + 2 * chipPadding;

                if (chipWidth > width - chartPadding - currentLineX) {
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

    @Override
    public void setNightMode(boolean night) {
        titlePaint.setColor(night ? Color.WHITE : Color.BLACK);
        datesRangePaint.setColor(night ? Color.WHITE : Color.BLACK);
        maxLegendAlpha = night ? 0x99 : 0xFF;
        legendDatesPaint.setColor(night ? 0x99A3B1C2 : 0xFF8E8E93);
        legendYStepsPaint.setColor(night ? 0x99A3B1C2 : 0xFF8E8E93);
        yLinesPaint.setColor(night ? 0x19FFFFFF : 0x19182D3B);
        selectedXLinePaint.setColor(night ? 0x19FFFFFF : 0x19182D3B);
        selectedCircleFillPaint.setColor(night ? 0xFF1c2533 : Color.WHITE);
        panelBackgroundPaint.setColor(night ? 0xFF1c2533 : Color.WHITE);
        panelShadowPaint.setColor(night ? 0x77000000 : 0x99CCCCCC);
        panelDatePaint.setColor(night ? Color.WHITE : Color.BLACK);
        panelLinesNamesPaint.setColor(night ? Color.WHITE : Color.BLACK);
        periodSelectorOutsidePaint.setColor(night ? 0x99304259 : 0x99E2EEF9);
        periodSelectorDragBorderPaint.setColor(night ? 0x806F899E : 0x8086A9C4);
        highlightChipPaint.setColor(night/* && !line.isVisibleOrWillBe*/ ? 0x54727272 : 0x54B0B0B0);

        callbacks.invalidate();
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

        stepX = Utils.floorToPowerOf2((endIndex - startIndex) / VER_DATES);
        if (stepX < 1) stepX = 1;

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
        stepY = dY / (HOR_LINES - 0.25f);
        for (int i = 0; i < stepsY.length; i++) {
            stepsY[i] = (endMinY + stepY * i);
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
                        System.arraycopy(stepsY, 0, prevStepsY, 0, HOR_LINES);
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

    // canvas translation must be (0, 0).
    private void drawTitle(Canvas canvas) {
        final float textY = dp12 + dp12 + dp8;

        canvas.drawText(title, chartPadding, textY, titlePaint);

        final String text = chartData.fullDates[startIndex] + " - " + chartData.fullDates[endIndex - 1];
        final float measureText = datesRangePaint.measureText(text);
        canvas.drawText(text, callbacks.getWidth() - chartPadding - measureText, textY, datesRangePaint);
    }

    // canvas translation must be (0, 0).
    private void drawDates(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        final float showingDatesCount = (endIndex - startIndex) * 1f / stepX;
        final int oddDatesAlpha = maxLegendAlpha - (int) ((showingDatesCount - VER_DATES) / VER_DATES * maxLegendAlpha);
        for (int index = startIndex / stepX * stepX; index < Math.min(endIndex + stepX, chartData.xValues.length); index += stepX) {
            float x = (index - startIndex * 1f) / (endIndex - startIndex - 1) * drawingWidth + chartPadding;
            legendDatesPaint.setAlpha(index % (stepX * 2) == 0 ? maxLegendAlpha : oddDatesAlpha);
            final String dateText = chartData.dates[index];
            canvas.drawText(dateText, x, chartTitleHeight + chartHeight + dp12 + dp4, legendDatesPaint);
        }
    }

    private float yValueToYOnChart(float y) {
        final float centerY = (minY + maxY) / 2f;
        return (0.5f - (y - centerY) / (maxY - minY)) * chartHeight;
    }

    // canvas translation must be (0, 0).
    private void drawYSteps(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        yLinesPaint.setAlpha((int) (yLimitsAnimator.getAnimatedFraction() * 0x19));
        legendYStepsPaint.setAlpha((int) (yLimitsAnimator.getAnimatedFraction() * maxLegendAlpha));
        for (int i = 0; i < HOR_LINES; i++) {
            final float valueY = stepsY[i];
            final float y = yValueToYOnChart(valueY);
            canvas.drawLine(chartPadding, chartTitleHeight + y, chartPadding + drawingWidth, chartTitleHeight + y, yLinesPaint);
            canvas.drawText(formatY((int) valueY), chartPadding, chartTitleHeight + y - dp6, legendYStepsPaint);
        }
        if (prevStepY > 0) {
            yLinesPaint.setAlpha((int) (0x19 - yLimitsAnimator.getAnimatedFraction() * 0x19));
            legendYStepsPaint.setAlpha((int) (maxLegendAlpha - yLimitsAnimator.getAnimatedFraction() * maxLegendAlpha));
            for (int i = 0; i < HOR_LINES; i++) {
                final float valueY = prevStepsY[i];
                final float y = yValueToYOnChart(valueY);
                canvas.drawLine(chartPadding, chartTitleHeight + y, chartPadding + drawingWidth, chartTitleHeight + y, yLinesPaint);
                canvas.drawText(formatY((int) valueY), chartPadding, chartTitleHeight + y - dp6, legendYStepsPaint);
            }
        }
    }

    // canvas translation must be (0, 0).
    private void drawChart(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        final float wid = (maxX - minX) / drawingWidth;
        final float hei = (maxY - minY) / chartHeight;
        final float dX = chartPadding;
        final float dY = chartTitleHeight + chartHeight;

        for (int c = 0; c < chartData.lines.size(); c++) {
            final ChartData.Line line = chartData.lines.get(c);
            if (line.isVisible()) {
                int q = 0;
                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _x = dX + ((float) chartData.xValues[i] - minX) / wid;
                    final float _y = dY - ((float) line.values[i] - minY) / hei;

                    points[q++] = _x;
                    points[q++] = _y;
                    if (i != 0 && i != chartData.xValues.length - 1) {
                        points[q++] = _x;
                        points[q++] = _y;
                    }
                }

                final Paint paint = linesPaints[c];
                paint.setAlpha(line.alpha);
                canvas.drawLines(points, paint);
            }
        }
    }

    // canvas translation must be (0, chartTitleHeight).
    private void drawChartSelection(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        final float wid = (maxX - minX) / drawingWidth;
        final float hei = (maxY - minY) / chartHeight;

        if (selectedIndex >= 0 && chartData.isAnyLineVisible()) {

            final float _x = chartPadding + (chartData.xValues[selectedIndex] - minX) / wid;

            if (0 < _x && _x < callbacks.getWidth()) {
                final float changeFraction = selectedIndexAnimator.getAnimatedFraction();

                canvas.drawLine(_x, 0, _x, chartHeight, selectedXLinePaint);

                final boolean panelLefted = _x < callbacks.getWidth() / 2;
                final float prevX = prevSelectedIndex < 0 ? _x : chartPadding + (chartData.xValues[prevSelectedIndex] - minX) / wid;
                final float panelAnchor = _x + (prevX - _x) * (1 - changeFraction);

                final float panelContentLeft = panelAnchor + (panelLefted ? dp12 * 2 : dp12 * -14);
                final float panelContentRight = panelAnchor + (panelLefted ? dp12 * 14 : dp12 * -2);

                final float lineHeight = dp12 * 2;

                float lineY = lineHeight;

                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);
                    if (line.isVisible()) {
                        selectedCircleFillPaint.setAlpha(line.alpha);
                        selectedCirclesStrokePaint[c].setAlpha(line.alpha);

                        final float _y = chartHeight - ((float) line.values[selectedIndex] - minY) / hei;
                        canvas.drawCircle(_x, _y, dp4, selectedCircleFillPaint);
                        canvas.drawCircle(_x, _y, dp4, selectedCirclesStrokePaint[c]);

                        if (line.alpha <= 0x80) {
                            lineY += lineHeight * line.alpha / 128f;
                        } else {
                            lineY += lineHeight;
                        }
                    }
                }

                final float panelPadding = dp12;

                panelRectOnScreen.set(
                        panelContentLeft - panelPadding,
                        chartTitleHeight,
                        panelContentRight + panelPadding,
                        chartTitleHeight + lineY + panelPadding
                );

                canvas.drawRoundRect(
                        panelContentLeft - panelPadding,
                        0,
                        panelContentRight + panelPadding,
                        lineY + panelPadding,
                        dp4, dp4, panelBackgroundPaint);
                canvas.drawRoundRect(
                        panelContentLeft - panelPadding,
                        0,
                        panelContentRight + panelPadding,
                        lineY + panelPadding,
                        dp4, dp4, panelShadowPaint);

                lineY = lineHeight;

                final boolean isBack = selectedIndex < prevSelectedIndex;
                final String selectedDate = chartData.selectedDates[selectedIndex];
                final String prevDate = prevSelectedIndex >= 0 ? chartData.selectedDates[prevSelectedIndex] : selectedDate;

                final int commonChars = Utils.commonEnd(prevDate, selectedDate);
                final String changeOfSelected = selectedDate.substring(0, selectedDate.length() - commonChars);

                final float translationAppear = (1 - changeFraction) * dp12 * (isBack ? -1 : 1);
                final float translationDisappear = -changeFraction * dp12 * (isBack ? -1 : 1);

                panelDatePaint.setAlpha((int) (0xFF * changeFraction));
                canvas.drawText(changeOfSelected, panelContentLeft, lineY + translationAppear, panelDatePaint);
                if (prevSelectedIndex >= 0) {
                    panelDatePaint.setAlpha(0XFF - (int) (0xFF * changeFraction));
                    String changeOfPrev = prevDate.substring(0, prevDate.length() - commonChars);
                    canvas.drawText(changeOfPrev, panelContentLeft, lineY + translationDisappear, panelDatePaint);
                }
                panelDatePaint.setAlpha(0xFF);
                String datesCommonEnd = selectedDate.substring(selectedDate.length() - commonChars);
                canvas.drawText(datesCommonEnd, panelContentLeft + panelDatePaint.measureText(changeOfSelected), lineY, panelDatePaint);

                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);
                    if (line.isVisible()) {

                        if (line.alpha <= 0x80) {
                            lineY += lineHeight * line.alpha / 128f;
                        } else {
                            lineY += lineHeight;
                        }

                        final int maxAlpha;
                        if (line.alpha <= 0x80) {
                            maxAlpha = 0;
                        } else {
                            maxAlpha = (line.alpha - 0x80) * 2;
                        }

                        panelLinesNamesPaint.setAlpha(maxAlpha);
                        canvas.drawText(line.name, panelContentLeft, lineY, panelLinesNamesPaint);

                        panelYValuesPaint[c].setAlpha(prevSelectedIndex >= 0 ? (int) (maxAlpha * changeFraction) : maxAlpha);
                        String formatY = toStringWithSpaces(line.values[selectedIndex]);
                        canvas.drawText(formatY, panelContentRight, lineY + (prevSelectedIndex >= 0 ? translationAppear : 0), panelYValuesPaint[c]);

                        if (prevSelectedIndex >= 0) {
                            panelYValuesPaint[c].setAlpha(maxAlpha - (int) (maxAlpha * changeFraction));
                            String formatPrevY = toStringWithSpaces(line.values[prevSelectedIndex]);
                            canvas.drawText(formatPrevY, panelContentRight, lineY + translationDisappear, panelYValuesPaint[c]);
                        }
                    }
                }
            } else {
                panelRectOnScreen.set(0, 0, 0, 0);
            }
        } else {
            panelRectOnScreen.set(0, 0, 0, 0);
        }
    }

    // canvas translation must be (chartPadding, chartTitleHeight + chartHeight + datesHeight).
    private void drawPeriodSelector(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        final float wid = (totalMaxX - totalMinX) / drawingWidth;
        final float hei = (totalMaxY - totalMinY) / periodSelectorHeight;

        canvas.save();
        canvas.clipPath(periodSelectorClipPath);

        for (int c = 0; c < chartData.lines.size(); c++) {
            final ChartData.Line line = chartData.lines.get(c);
            if (line.isVisible()) {
                int q = 0;

                for (int i = 0; i < chartData.xValues.length; i++) {
                    final float _x = ((float) chartData.xValues[i] - totalMinX) / wid;
                    final float _y = periodSelectorHeight - ((float) line.values[i] - totalMinY) / hei;

                    points[q++] = _x;
                    points[q++] = _y;
                    if (i != 0 && i != chartData.xValues.length - 1) {
                        points[q++] = _x;
                        points[q++] = _y;
                    }
                }

                final Paint paint = periodSelectorLinesPaints[c];
                paint.setAlpha(line.alpha);
                canvas.drawLines(points, paint);
            }
        }

        canvas.restore();

        final float startX = startIndex * 1f / chartData.xValues.length * drawingWidth;
        final float endX = endIndex * 1f / chartData.xValues.length * drawingWidth;

        final float borderHor = dp2 / 2;
        final float borderVer = dp12;

        // period's outside.
        path.addRoundRect(0, 0, startX + borderVer, periodSelectorHeight, radiiLeft, Path.Direction.CW);
        canvas.drawPath(path, periodSelectorOutsidePaint);
        path.rewind();
        path.addRoundRect(endX - borderVer, 0, drawingWidth, periodSelectorHeight, radiiRight, Path.Direction.CW);
        canvas.drawPath(path, periodSelectorOutsidePaint);
        path.rewind();

        // horizontal borders
        canvas.drawRect(startX + borderVer, -borderHor, endX - borderVer, 0, periodSelectorDragBorderPaint);
        canvas.drawRect(startX + borderVer, periodSelectorHeight, endX - borderVer, periodSelectorHeight + borderHor, periodSelectorDragBorderPaint);

        // vertical borders
        path.addRoundRect(startX, -borderHor, startX + borderVer, periodSelectorHeight + borderHor, radiiLeft, Path.Direction.CW);
        canvas.drawPath(path, periodSelectorDragBorderPaint);
        path.rewind();
        path.addRoundRect(endX - borderVer, -borderHor, endX, periodSelectorHeight + borderHor, radiiRight, Path.Direction.CW);
        canvas.drawPath(path, periodSelectorDragBorderPaint);
        path.rewind();

        // white drag rects
        canvas.drawRoundRect(
                startX + borderVer / 2 - dp2 / 2,
                periodSelectorHeight / 2 - dp6,
                startX + borderVer / 2 + dp2 / 2,
                periodSelectorHeight / 2 + dp6,
                dp2,
                dp2,
                periodSelectorWhiteDragPaint
        );
        canvas.drawRoundRect(
                endX - borderVer / 2 - dp2 / 2,
                periodSelectorHeight / 2 - dp6,
                endX - borderVer / 2 + dp2 / 2,
                periodSelectorHeight / 2 + dp6,
                dp2,
                dp2,
                periodSelectorWhiteDragPaint
        );
    }

    // canvas translation must be (0, 0).
    private void drawChips(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        if (chartData.lines.size() > 1) {
            final float chipCornerRadius = dp12 * 4;
            float currentLineX = chartPadding;
            float currentLineY = chartTitleHeight + chartHeight + datesHeight + periodSelectorHeight + chipsMarginTop + chipMargin;
            for (int c = 0; c < chartData.lines.size(); c++) {
                final ChartData.Line line = chartData.lines.get(c);
                float chipWidth = chipsTextWidth[c];

                if (chipWidth > drawingWidth + chartPadding - currentLineX) {
                    currentLineX = chartPadding;
                    currentLineY += chipHeight + chipMargin;
                }

                chipsRectOnScreen[c].set(currentLineX, currentLineY, currentLineX + chipWidth, currentLineY + chipHeight);

                if (line.isVisibleOrWillBe) {
                    canvas.drawRoundRect(chipsRectOnScreen[c], chipCornerRadius, chipCornerRadius, chipsFillPaint[c]);

                    final int checkLeft = (int) (chipsRectOnScreen[c].left + chipPadding / 2 - dp2);
                    final int checkTop = (int) (chipsRectOnScreen[c].top + chipPadding / 4 + dp2);
                    drawableCheck.setBounds(
                            checkLeft,
                            checkTop,
                            checkLeft + drawableCheck.getIntrinsicWidth(),
                            checkTop + drawableCheck.getIntrinsicHeight()
                    );
                    drawableCheck.draw(canvas);
                }
                canvas.drawRoundRect(chipsRectOnScreen[c], chipCornerRadius, chipCornerRadius, chipsBorderPaint[c]);

                float textTransitionX = line.isVisibleOrWillBe ? chipPadding / 4 + dp2 : 0;
                canvas.drawText(
                        line.name,
                        currentLineX + chipPadding + textTransitionX,
                        currentLineY + chipPadding,
                        line.isVisibleOrWillBe ? chipWhiteTextPaint : chipsTextPaint[c]
                );

                if (lastDownX >= 0 && chipsRectOnScreen[c].contains(lastDownX, lastDownY)) {
                    canvas.drawRoundRect(chipsRectOnScreen[c], chipCornerRadius, chipCornerRadius, highlightChipPaint);
                }

                currentLineX += chipWidth + chipMargin;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {

        drawTitle(canvas);
        drawDates(canvas);
        drawYSteps(canvas);
        drawChart(canvas);
        drawChips(canvas);

        canvas.translate(0, chartTitleHeight);

        drawChartSelection(canvas);

        canvas.translate(chartPadding, chartHeight + datesHeight);

        drawPeriodSelector(canvas);
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
