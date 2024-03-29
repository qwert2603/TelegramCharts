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

import com.qwert2603.telegram_charts.R;
import com.qwert2603.telegram_charts.Utils;
import com.qwert2603.telegram_charts.entity.ChartData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartViewDelegateLines implements Delegate {

    private static final long ANIMATION_DURATION = 300L;

    private final Resources resources;
    protected final Context context;

    protected final Callbacks callbacks;
    protected boolean night;

    private static final DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();

    public ChartViewDelegateLines(Context context, String title, final ChartData chartData, final Callbacks callbacks) {
        this.callbacks = callbacks;
        this.context = context;
        this.resources = context.getResources();

        this.title = title;

        this.chartData = chartData;
        points = new float[chartData.xValues.length * 4];

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
        lineWidth = getResources().getDimension(R.dimen.line_width);

        periodSelectorBorderHor = dp2 / 2;
        periodSelectorBorderVer = dp12;

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        titlePaint.setTextSize(dp12 + dp4);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);

        datesRangePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        datesRangePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        datesRangePaint.setTextSize(dp12 + dp2);
        datesRangePaint.setTypeface(Typeface.DEFAULT_BOLD);
        datesRangePaint.setTextAlign(Paint.Align.RIGHT);

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
        periodSelectorDragBorderPaintDragging = new Paint(Paint.ANTI_ALIAS_FLAG);
        periodSelectorDragBorderPaintDragging.setStyle(Paint.Style.FILL);
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
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(lineWidth * 0.9f);
            linesPaints[i] = paint;
        }

        periodSelectorLinesPaints = new Paint[chartData.lines.size()];
        for (int i = 0; i < chartData.lines.size(); i++) {
            final ChartData.Line line = chartData.lines.get(i);
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(line.color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(lineWidth / 2f * 0.9f);
            periodSelectorLinesPaints[i] = paint;
        }

        //chips
        chipsMarginHorizontal = dp6;
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
        chipsWidth = new float[chartData.lines.size()];
        chipsFillPaint = new Paint[chartData.lines.size()];
        chipsTextPaint = new Paint[chartData.lines.size()];
        chipsBorderPaint = new Paint[chartData.lines.size()];

        for (int i = 0; i < chartData.lines.size(); i++) {
            final ChartData.Line line = chartData.lines.get(i);

            chipsRectOnScreen[i] = new RectF();

            chipsTextPaint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            chipsTextPaint[i].setColor(line.color);
            chipsTextPaint[i].setTextSize(chipTextSize);

            chipsWidth[i] = chipsTextPaint[i].measureText(line.name) + 2 * chipPadding;

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

        final float panelTextSize = dp12 + dp2;

        panelDatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        panelDatePaint.setTextSize(panelTextSize);
        panelDatePaint.setTypeface(Typeface.DEFAULT_BOLD);

        panelPercentsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        panelPercentsPaint.setTextSize(panelTextSize);
        panelPercentsPaint.setTypeface(Typeface.DEFAULT_BOLD);
        panelPercentsPaint.setTextAlign(Paint.Align.RIGHT);

        panelLinesNamesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        panelLinesNamesPaint.setTextSize(panelTextSize);

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
            paint.setTextSize(panelTextSize);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.RIGHT);
            panelYValuesPaint[i] = paint;
        }

        panelYValueAllPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        panelYValueAllPaint.setTextSize(panelTextSize);
        panelYValueAllPaint.setTypeface(Typeface.DEFAULT_BOLD);
        panelYValueAllPaint.setTextAlign(Paint.Align.RIGHT);

        chartHeight = getResources().getDimension(R.dimen.chart_height);
        datesHeight = getResources().getDimension(R.dimen.dates_height);
        periodSelectorHeight = getResources().getDimension(R.dimen.period_selector_height);
        minPeriodSelectorWidth = getResources().getDimension(R.dimen.min_period_selector_width);
        periodSelectorDraggableWidth = getResources().getDimension(R.dimen.period_selector_draggable_width);

        chartPadding = getResources().getDimension(R.dimen.chart_padding);

        // period selector
        psFillRadius = dp6;
        radiiLeft = new float[]{psFillRadius, psFillRadius, 0, 0, 0, 0, psFillRadius, psFillRadius};
        radiiRight = new float[]{0, 0, psFillRadius, psFillRadius, psFillRadius, psFillRadius, 0, 0};

        gestureDetector = new GestureDetector(this.context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (getChartTitleHeight() + chartHeight + datesHeight + periodSelectorHeight < e.getY()) {
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
                if (getChartTitleHeight() + chartHeight + datesHeight + periodSelectorHeight < e.getY()) {
                    List<ChartData.Line> lines = chartData.lines;
                    for (int i = 0; i < lines.size(); i++) {
                        if (chipsRectOnScreen[i].contains(e.getX(), e.getY())) {
                            setOnlyOneLineVisible(lines.get(i).name);
                            break;
                        }
                    }
                    lastChipsDownX = -1;
                    lastChipsDownY = -1;
                    callbacks.invalidate();
                }
            }
        });

        final Drawable drawableCheck = resources.getDrawable(R.drawable.ic_done_black_24dp);
        drawablesCheck = new Drawable[chartData.xValues.length];
        for (int i = 0; i < chartData.lines.size(); i++) {
            drawablesCheck[i] = drawableCheck.getConstantState().newDrawable().mutate();
        }
    }

    private Resources getResources() {
        return resources;
    }

    boolean isSelectionPanelWithAllString() {
        return false;
    }

    boolean isSelectionPanelWithPercents() {
        return false;
    }

    float getChartTitleHeight() {
        return dp12 * 4 + dp4;
    }

    float getPanelMarginTop() {
        return 0f;
    }

    float getYLinesCompressFactor() {
        return 0.25f;
    }

    private final GestureDetector gestureDetector;
    private final RectF panelRectOnScreen = new RectF();

    private int maxLegendAlpha;

    protected final float chartHeight;
    protected final float datesHeight;
    protected final float periodSelectorHeight;
    private final float minPeriodSelectorWidth;
    private final float periodSelectorDraggableWidth;
    protected final float chartPadding;
    private final float dp2;
    private final float dp4;
    private final float dp6;
    private final float dp8;
    protected final float lineWidth;
    protected final float dp12;
    private final float chipsMarginHorizontal;
    private final float chipMargin;
    private final float chipPadding;
    private final float chipHeight;
    private final float periodSelectorBorderHor;
    private final float periodSelectorBorderVer;
    protected final float psFillRadius;

    private final Path path = new Path();
    private final float[] radiiLeft;
    private final float[] radiiRight;

    private final String title;
    protected final ChartData chartData;
    private final Paint panelBackgroundPaint;
    private final Paint panelShadowPaint;
    protected final float[] points;

    private final Paint periodSelectorOutsidePaint;
    private final Paint periodSelectorDragBorderPaint;
    private final Paint periodSelectorDragBorderPaintDragging;
    private final Paint periodSelectorWhiteDragPaint;
    private final Paint legendDatesPaint;
    private final Paint legendYStepsPaint;
    private final Paint yLinesPaint;
    private final Paint titlePaint;
    private final Paint datesRangePaint;
    private final Paint chipWhiteTextPaint;
    private final Paint highlightChipPaint;

    protected final Paint[] linesPaints;
    protected final Paint[] periodSelectorLinesPaints;

    private final RectF[] chipsRectOnScreen;
    private final float[] chipsWidth;
    private final Paint[] chipsBorderPaint;
    private final Paint[] chipsFillPaint;
    private final Paint[] chipsTextPaint;

    protected final Paint selectedXLinePaint;
    private final Paint selectedCircleFillPaint;
    private final Paint[] selectedCirclesStrokePaint;
    private final Paint panelDatePaint;
    private final Paint panelLinesNamesPaint;
    private final Paint panelPercentsPaint;
    private final Paint[] panelYValuesPaint;
    private final Paint panelYValueAllPaint;

    private float periodStartXRel = 0;
    private float periodEndXRel = 1;
    protected int startIndex;
    protected int endIndex;

    protected int selectedIndex = -1;
    private int prevSelectedIndex = -1;

    protected long minX;
    protected long maxX;

    protected final long totalMinX;
    protected final long totalMaxX;

    // step on X-axis (in indices).
    private int stepX;
    private float stepY;
    private float prevStepY;

    private final float[] stepsY = new float[HOR_LINES];
    private final float[] prevStepsY = new float[HOR_LINES];

    protected int minY;
    protected int maxY;

    protected int totalMinY;
    protected int totalMaxY;

    private ValueAnimator yLimitsAnimator;
    private ValueAnimator selectedIndexAnimator;

    private Map<String, ValueAnimator> opacityAnimators = new HashMap<>();

    private final Drawable[] drawablesCheck;

    protected final Path periodSelectorClipPath = new Path();

    @Override
    public int measureHeight(int width) {
        float drawingWidth = width - 2 * chartPadding;
        periodSelectorClipPath.rewind();
        periodSelectorClipPath.addRoundRect(
                0,
                0,
                drawingWidth,
                periodSelectorHeight,
                new float[]{psFillRadius, psFillRadius, psFillRadius, psFillRadius, psFillRadius, psFillRadius, psFillRadius, psFillRadius},
                Path.Direction.CW
        );

        float currentLineX = chartPadding;
        float currentLineY = getChartTitleHeight() + chartHeight + datesHeight + periodSelectorHeight + chipsMarginHorizontal + chipMargin;
        if (chartData.lines.size() > 1) {
            for (int c = 0; c < chartData.lines.size(); c++) {
                final float chipWidth = chipsWidth[c];

                if (chipWidth > width - chartPadding - currentLineX) {
                    currentLineX = chartPadding;
                    currentLineY += chipHeight + chipMargin;
                }

                currentLineX += chipWidth + chipMargin;
            }

            // plus last chips line.
            currentLineY += chipHeight + chipMargin;
        }

        return (int) (currentLineY + chipsMarginHorizontal);
    }

    @Override
    public void setNightMode(boolean night) {
        this.night = night;
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
        panelPercentsPaint.setColor(night ? Color.WHITE : Color.BLACK);
        panelLinesNamesPaint.setColor(night ? Color.WHITE : Color.BLACK);
        panelYValueAllPaint.setColor(night ? Color.WHITE : Color.BLACK);
        periodSelectorOutsidePaint.setColor(night ? 0x99304259 : 0x99E2EEF9);
        periodSelectorDragBorderPaint.setColor(night ? 0x806F899E : 0x8086A9C4);
        periodSelectorDragBorderPaintDragging.setColor(night ? 0xE06F899E : 0xE086A9C4);
        highlightChipPaint.setColor(night ? 0x54727272 : 0x54B0B0B0);

        callbacks.invalidate();
    }

    private void setOnlyOneLineVisible(String name) {
        for (final ChartData.Line line : chartData.lines) {
            final boolean visible = line.name.equals(name);

            ValueAnimator animator = opacityAnimators.get(line.name);
            if (animator == null) {
                animator = createAnimatorForLineOpacity(line);
                opacityAnimators.put(line.name, animator);
            }

            line.isVisibleOrWillBe = visible;

            animator.setFloatValues(line.alpha, visible ? 1f : 0f);
            animator.start();
        }

        animateYLimits();
    }

    private void setLineVisible(String name, boolean visible) {
        for (final ChartData.Line line : chartData.lines) {
            if (line.name.equals(name)) {

                ValueAnimator animator = opacityAnimators.get(line.name);
                if (animator == null) {
                    animator = createAnimatorForLineOpacity(line);
                    opacityAnimators.put(line.name, animator);
                }

                line.isVisibleOrWillBe = visible;

                animator.setFloatValues(line.alpha, visible ? 1f : 0f);
                animator.start();

                break;
            }
        }

        animateYLimits();
    }

    private ValueAnimator createAnimatorForLineOpacity(final ChartData.Line line) {
        ValueAnimator animator = ValueAnimator
                .ofFloat(0f)
                .setDuration(ANIMATION_DURATION);
        animator.setInterpolator(decelerateInterpolator);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                line.alpha = (float) animation.getAnimatedValue();
                callbacks.invalidate();
            }
        });
        return animator;
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
        stepY = dY / (HOR_LINES - getYLinesCompressFactor());
        for (int i = 0; i < stepsY.length; i++) {
            stepsY[i] = (endMinY + stepY * i);
        }

        if (yLimitsAnimator == null) {
            yLimitsAnimator = ValueAnimator.ofFloat(0f, 1f);
            yLimitsAnimator.setInterpolator(decelerateInterpolator);
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
                float animatedFraction = (float) animation.getAnimatedValue();
                minY = (int) (startMinY + (endMinY - startMinY) * animatedFraction);
                totalMinY = (int) (startTotalMinY + (endTotalMinY - startTotalMinY) * animatedFraction);

                maxY = (int) (startMaxY + (endMaxY - startMaxY) * animatedFraction);
                totalMaxY = (int) (startTotalMaxY + (endTotalMaxY - startTotalMaxY) * animatedFraction);

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
    private float selectorDragCenterOffsetRel = 0f;

    private float startSelectedIndexX = -1;
    private float startSelectedIndexY = -1;
    private boolean draggingSelectedIndex = false;

    private float lastChipsDownX = -1;
    private float lastChipsDownY = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        final int pointerId = event.getPointerId(event.getActionIndex());

        final float selectorCenterRel = (periodStartXRel + periodEndXRel) / 2f;
        final float selectorWidthRel = periodEndXRel - periodStartXRel;
        final float drawingWidth = getDrawingWidth();
        float xRel = (event.getX() - chartPadding) / drawingWidth;

        final float minSelectorWidthRel = minPeriodSelectorWidth / drawingWidth;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (getChartTitleHeight() + chartHeight + datesHeight < event.getY() && event.getY() < getChartTitleHeight() + chartHeight + datesHeight + periodSelectorHeight) {
                    if (Math.abs(periodStartXRel - xRel) * drawingWidth < periodSelectorDraggableWidth) {
                        dragPointerId = pointerId;
                        currentDrag = DRAG_START;
                        callbacks.requestDisallowInterceptTouchEvent(true);
                    } else if (Math.abs(periodEndXRel - xRel) * drawingWidth < periodSelectorDraggableWidth) {
                        dragPointerId = pointerId;
                        currentDrag = DRAG_END;
                        callbacks.requestDisallowInterceptTouchEvent(true);
                    } else if (periodStartXRel < xRel && xRel < periodEndXRel) {
                        dragPointerId = pointerId;
                        currentDrag = DRAG_SELECTOR;
                        selectorDragCenterOffsetRel = selectorCenterRel - xRel;
                        movePeriodSelectorTo(xRel + selectorDragCenterOffsetRel, selectorWidthRel);
                        callbacks.requestDisallowInterceptTouchEvent(true);
                    }
                } else if (getChartTitleHeight() < event.getY() && event.getY() < getChartTitleHeight() + chartHeight) {
                    if (selectedIndex < 0) {
                        updateSelectedIndex(xRel);
                    } else {
                        if (!panelRectOnScreen.contains(event.getX(), event.getY())) {
                            selectedIndex = -1;
                            currentDrag = 0;
                            dragPointerId = -1;
                            startSelectedIndexX = -1;
                            startSelectedIndexY = -1;
                        }
                    }
                    dragPointerId = pointerId;
                    currentDrag = DRAG_SELECTED_INDEX;
                    startSelectedIndexX = event.getX();
                    startSelectedIndexY = event.getY();
                    callbacks.requestDisallowInterceptTouchEvent(true);
                } else if (getChartTitleHeight() + chartHeight + datesHeight + periodSelectorHeight < event.getY()) {
                    dragPointerId = pointerId;
                    lastChipsDownX = event.getX();
                    lastChipsDownY = event.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointerId == dragPointerId) {
                    switch (currentDrag) {
                        case DRAG_START:
                            if (xRel > periodEndXRel - minSelectorWidthRel) {
                                xRel = periodEndXRel - minSelectorWidthRel;
                            }
                            if (xRel < 0) {
                                xRel = 0;
                            }
                            periodStartXRel = xRel;
                            int newStartIndex = (int) (xRel * chartData.xValues.length);
                            setPeriodIndices(newStartIndex, endIndex);
                            break;
                        case DRAG_END:
                            if (xRel < periodStartXRel + minSelectorWidthRel) {
                                xRel = periodStartXRel + minSelectorWidthRel;
                            }
                            if (xRel > 1) {
                                xRel = 1;
                            }
                            periodEndXRel = xRel;
                            int newEndIndex = (int) (xRel * chartData.xValues.length);
                            setPeriodIndices(startIndex, newEndIndex);
                            break;
                        case DRAG_SELECTOR:
                            movePeriodSelectorTo(xRel + selectorDragCenterOffsetRel, selectorWidthRel);
                            break;
                        case DRAG_SELECTED_INDEX:
                            if (!draggingSelectedIndex && Math.abs(event.getX() - startSelectedIndexX) < dp6 && Math.abs(event.getY() - startSelectedIndexY) > dp12) {
                                selectedIndex = -1;
                                currentDrag = 0;
                                dragPointerId = -1;
                                startSelectedIndexX = -1;
                                startSelectedIndexY = -1;
                                callbacks.requestDisallowInterceptTouchEvent(false);
                            } else if (!draggingSelectedIndex && !panelRectOnScreen.contains(event.getX(), event.getY())
                                    && Math.abs(event.getX() - startSelectedIndexX) > dp12 && Math.abs(event.getY() - startSelectedIndexY) < dp6) {
                                draggingSelectedIndex = true;
                            }
                            if (draggingSelectedIndex) {
                                updateSelectedIndex(xRel);
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
                    lastChipsDownX = -1;
                    lastChipsDownY = -1;
                }
                break;
        }

        callbacks.invalidate();

        return true;
    }

    private void updateSelectedIndex(float xRel) {
        int newSelectedIndex = (int) (startIndex + (endIndex - 1 - startIndex) * xRel);
        if (newSelectedIndex < 0) {
            newSelectedIndex = 0;
        }
        if (newSelectedIndex > chartData.xValues.length - 1) {
            newSelectedIndex = chartData.xValues.length - 1;
        }

        if (newSelectedIndex == selectedIndex) return;
        prevSelectedIndex = selectedIndex;
        selectedIndex = newSelectedIndex;

        if (selectedIndexAnimator == null) {
            selectedIndexAnimator = ValueAnimator.ofFloat(0f, 1f);
            selectedIndexAnimator.setInterpolator(decelerateInterpolator);
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

    private void movePeriodSelectorTo(float xRel, float selectorWidthRel) {
        float newMin = xRel - selectorWidthRel / 2;
        float newMax = xRel + selectorWidthRel / 2;
        if (newMin < 0) {
            newMin = 0;
            newMax = selectorWidthRel;
        }
        if (newMax > 1) {
            newMin = 1 - selectorWidthRel;
            newMax = 1;
        }

        periodStartXRel = newMin;
        periodEndXRel = newMax;

        int newStartIndex = (int) (newMin * chartData.xValues.length);
        int newEndIndex = (int) (newMax * chartData.xValues.length);
        setPeriodIndices(newStartIndex, newEndIndex);
    }

    // canvas translation must be (0, 0).
    private void drawTitle(Canvas canvas) {
        final float textY = dp12 + dp12 + dp8;

        canvas.drawText(title, chartPadding, textY, titlePaint);

        final String text = chartData.fullDates[startIndex] + " - " + chartData.fullDates[endIndex - 1];
        canvas.drawText(text, callbacks.getWidth() - chartPadding, textY, datesRangePaint);
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
            canvas.drawText(dateText, x, getChartTitleHeight() + chartHeight + dp12 + dp4, legendDatesPaint);
        }
    }

    private float yValueToYOnChart(float y) {
        final float centerY = (minY + maxY) / 2f;
        return (0.5f - (y - centerY) / (maxY - minY)) * chartHeight;
    }

    // canvas translation must be (0, 0).
    private void drawYSteps(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        float animatedFraction = (float) yLimitsAnimator.getAnimatedValue();
        yLinesPaint.setAlpha((int) (animatedFraction * 0x19));
        legendYStepsPaint.setAlpha((int) (animatedFraction * maxLegendAlpha));
        for (int i = 0; i < HOR_LINES; i++) {
            final float valueY = stepsY[i];
            final float y = yValueToYOnChart(valueY);
            canvas.drawLine(chartPadding, getChartTitleHeight() + y, chartPadding + drawingWidth, getChartTitleHeight() + y, yLinesPaint);
            canvas.drawText(Utils.formatY((int) valueY), chartPadding, getChartTitleHeight() + y - dp6, legendYStepsPaint);
        }
        if (prevStepY > 0) {
            yLinesPaint.setAlpha((int) (0x19 - animatedFraction * 0x19));
            legendYStepsPaint.setAlpha((int) (maxLegendAlpha - animatedFraction * maxLegendAlpha));
            for (int i = 0; i < HOR_LINES; i++) {
                final float valueY = prevStepsY[i];
                final float y = yValueToYOnChart(valueY);
                canvas.drawLine(chartPadding, getChartTitleHeight() + y, chartPadding + drawingWidth, getChartTitleHeight() + y, yLinesPaint);
                canvas.drawText(Utils.formatY((int) valueY), chartPadding, getChartTitleHeight() + y - dp6, legendYStepsPaint);
            }
        }
    }

    // canvas translation must be (0, 0).
    protected void drawChart(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        final float wid = (maxX - minX) / drawingWidth;
        final float hei = (maxY - minY) / chartHeight;
        final float dX = chartPadding;
        final float dY = getChartTitleHeight() + chartHeight;

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
                paint.setAlpha(line.alphaInt());
                canvas.drawLines(points, 0, q, paint);
            }
        }
    }

    // canvas translation must be (0, 0).
    protected void drawSelectionOnChart(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();
        final float titleHeight = getChartTitleHeight();

        final float wid = (maxX - minX) / drawingWidth;
        final float hei = (maxY - minY) / chartHeight;

        if (selectedIndex >= 0 && chartData.isAnyLineVisible()) {

            final float _x = chartPadding + (chartData.xValues[selectedIndex] - minX) / wid;

            if (startIndex <= selectedIndex && selectedIndex < endIndex) {
                canvas.drawLine(_x, titleHeight, _x, titleHeight + chartHeight, selectedXLinePaint);

                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);
                    if (line.isVisible()) {
                        selectedCircleFillPaint.setAlpha(line.alphaInt());
                        selectedCirclesStrokePaint[c].setAlpha(line.alphaInt());

                        final float _y = titleHeight + chartHeight - ((float) line.values[selectedIndex] - minY) / hei;
                        canvas.drawCircle(_x, _y, dp4, selectedCircleFillPaint);
                        canvas.drawCircle(_x, _y, dp4, selectedCirclesStrokePaint[c]);
                    }
                }
            }
        }
    }

    // canvas translation must be (0, 0).
    private void drawSelectionPanel(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();
        final float titleHeight = getChartTitleHeight();

        final float wid = (maxX - minX) / drawingWidth;

        if (selectedIndex >= 0 && chartData.isAnyLineVisible()) {

            final float _x = chartPadding + (chartData.xValues[selectedIndex] - minX) / wid;

            if (startIndex <= selectedIndex && selectedIndex < endIndex) {
                final float changeFraction = (float) selectedIndexAnimator.getAnimatedValue();

                final boolean isSelectionPanelWithAllString = isSelectionPanelWithAllString();
                final boolean isSelectionPanelWithPercents = isSelectionPanelWithPercents();
                final float panelMarginTop = getPanelMarginTop();

                final boolean panelLefted = _x < callbacks.getWidth() / 2;
                final float prevX = prevSelectedIndex < 0 ? _x : chartPadding + (chartData.xValues[prevSelectedIndex] - minX) / wid;
                final float panelAnchor = _x + (prevX - _x) * (1 - changeFraction);

                final float panelContentLeft = panelAnchor + (panelLefted ? dp12 * 2 : dp12 * -14);
                final float panelContentRight = panelAnchor + (panelLefted ? dp12 * 14 : dp12 * -2);

                final float lineHeight = dp12 * 2;
                final float percentsWidth = dp12 + dp12 + dp8;

                float lineY = panelMarginTop + lineHeight;

                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);
                    if (line.isVisible()) {
                        if (line.alpha <= 0.5f) {
                            lineY += lineHeight * line.alpha * 2;
                        } else {
                            lineY += lineHeight;
                        }
                    }
                }
                if (isSelectionPanelWithAllString) {
                    lineY += lineHeight;
                }

                final float panelPadding = dp12;

                panelRectOnScreen.set(
                        panelContentLeft - panelPadding,
                        titleHeight + panelMarginTop,
                        panelContentRight + panelPadding,
                        titleHeight + lineY + panelPadding
                );

                canvas.drawRoundRect(
                        panelContentLeft - panelPadding,
                        titleHeight + panelMarginTop,
                        panelContentRight + panelPadding,
                        titleHeight + lineY + panelPadding,
                        dp4, dp4, panelBackgroundPaint);
                canvas.drawRoundRect(
                        panelContentLeft - panelPadding,
                        titleHeight + panelMarginTop,
                        panelContentRight + panelPadding,
                        titleHeight + lineY + panelPadding,
                        dp4, dp4, panelShadowPaint);

                lineY = panelMarginTop + lineHeight;

                final boolean isBack = selectedIndex < prevSelectedIndex;
                final String selectedDate = chartData.selectedDates[selectedIndex];
                final String prevDate = prevSelectedIndex >= 0 ? chartData.selectedDates[prevSelectedIndex] : selectedDate;

                final int commonChars = Utils.commonEnd(prevDate, selectedDate);
                final String changeOfSelected = selectedDate.substring(0, selectedDate.length() - commonChars);

                final float translationAppear = (1 - changeFraction) * dp12 * (isBack ? -1 : 1);
                final float translationDisappear = -changeFraction * dp12 * (isBack ? -1 : 1);

                panelDatePaint.setAlpha((int) (0xFF * changeFraction));
                canvas.drawText(changeOfSelected, panelContentLeft, titleHeight + lineY + translationAppear, panelDatePaint);
                if (prevSelectedIndex >= 0) {
                    panelDatePaint.setAlpha(0XFF - (int) (0xFF * changeFraction));
                    String changeOfPrev = prevDate.substring(0, prevDate.length() - commonChars);
                    canvas.drawText(changeOfPrev, panelContentLeft, titleHeight + lineY + translationDisappear, panelDatePaint);
                }
                panelDatePaint.setAlpha(0xFF);
                String datesCommonEnd = selectedDate.substring(selectedDate.length() - commonChars);
                canvas.drawText(datesCommonEnd, panelContentLeft + panelDatePaint.measureText(changeOfSelected), titleHeight + lineY, panelDatePaint);

                int sum = 0;
                int prevSum = 0;
                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);

                    if (line.isVisible()) {
                        sum += line.values[selectedIndex];
                        if (prevSelectedIndex >= 0) {
                            prevSum += line.values[prevSelectedIndex];
                        }
                    }
                }

                for (int c = 0; c < chartData.lines.size(); c++) {
                    final ChartData.Line line = chartData.lines.get(c);

                    if (line.isVisible()) {

                        if (line.alpha <= 0.5f) {
                            lineY += lineHeight * line.alpha * 2;
                        } else {
                            lineY += lineHeight;
                        }

                        final int maxAlpha;
                        if (line.alpha <= 0.5f) {
                            maxAlpha = 0;
                        } else {
                            maxAlpha = (int) ((line.alpha - 0.5f) * 2 * 255f);
                        }

                        panelLinesNamesPaint.setAlpha(maxAlpha);
                        canvas.drawText(line.name, panelContentLeft + (isSelectionPanelWithPercents ? percentsWidth : 0), titleHeight + lineY, panelLinesNamesPaint);

                        final int alphaAppear = prevSelectedIndex >= 0 ? (int) (maxAlpha * changeFraction) : maxAlpha;
                        final int alphaDisappear = maxAlpha - (int) (maxAlpha * changeFraction);

                        panelYValuesPaint[c].setAlpha(alphaAppear);
                        String formatY = Utils.toStringWithSpaces(line.values[selectedIndex]);
                        canvas.drawText(formatY, panelContentRight, titleHeight + lineY + (prevSelectedIndex >= 0 ? translationAppear : 0), panelYValuesPaint[c]);

                        if (isSelectionPanelWithPercents) {
                            panelPercentsPaint.setAlpha(alphaAppear);
                            String percentsFormatY = Utils.toStringWithSpaces(Math.round(line.values[selectedIndex] * 100f / sum)) + '%';
                            canvas.drawText(percentsFormatY, panelContentLeft + percentsWidth - dp8, titleHeight + lineY + (prevSelectedIndex >= 0 ? translationAppear : 0), panelPercentsPaint);
                        }

                        if (prevSelectedIndex >= 0) {
                            panelYValuesPaint[c].setAlpha(alphaDisappear);
                            String formatPrevY = Utils.toStringWithSpaces(line.values[prevSelectedIndex]);
                            canvas.drawText(formatPrevY, panelContentRight, titleHeight + lineY + translationDisappear, panelYValuesPaint[c]);

                            if (isSelectionPanelWithPercents) {
                                panelPercentsPaint.setAlpha(alphaDisappear);
                                String prevPercentsFormatY = Utils.toStringWithSpaces(Math.round(line.values[prevSelectedIndex] * 100f / prevSum)) + '%';
                                canvas.drawText(prevPercentsFormatY, panelContentLeft + percentsWidth - dp8, titleHeight + lineY + translationDisappear, panelPercentsPaint);
                            }
                        }
                    }
                }
                if (isSelectionPanelWithAllString) {
                    lineY += lineHeight;

                    panelLinesNamesPaint.setAlpha(0xFF);
                    canvas.drawText("All", panelContentLeft, titleHeight + lineY, panelLinesNamesPaint);

                    panelYValueAllPaint.setAlpha(prevSelectedIndex >= 0 ? (int) (0xFF * changeFraction) : 0xFF);
                    String formatY = Utils.toStringWithSpaces(sum);
                    canvas.drawText(formatY, panelContentRight, titleHeight + lineY + (prevSelectedIndex >= 0 ? translationAppear : 0), panelYValueAllPaint);

                    if (prevSelectedIndex >= 0) {
                        panelYValueAllPaint.setAlpha(0xFF - (int) (0xFF * changeFraction));
                        String formatPrevY = Utils.toStringWithSpaces(prevSum);
                        canvas.drawText(formatPrevY, panelContentRight, titleHeight + lineY + translationDisappear, panelYValueAllPaint);
                    }
                }
            } else {
                panelRectOnScreen.set(0, 0, 0, 0);
            }
        } else {
            panelRectOnScreen.set(0, 0, 0, 0);
        }
    }

    // canvas translation must be (chartPadding, getChartTitleHeight() + chartHeight + datesHeight).
    protected void drawPeriodSelectorChart(Canvas canvas) {
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
                paint.setAlpha(line.alphaInt());
                canvas.drawLines(points, 0, q, paint);
            }
        }

        canvas.restore();
    }

    // canvas translation must be (chartPadding, getChartTitleHeight() + chartHeight + datesHeight).
    private void drawPeriodSelectorUi(Canvas canvas) {
        final float drawingWidth = getDrawingWidth();

        final float startX = startIndex * 1f / chartData.xValues.length * drawingWidth;
        final float endX = endIndex * 1f / chartData.xValues.length * drawingWidth;

        // period's outside.
        path.addRoundRect(0, 0, startX + periodSelectorBorderVer, periodSelectorHeight, radiiLeft, Path.Direction.CW);
        canvas.drawPath(path, periodSelectorOutsidePaint);
        path.rewind();
        path.addRoundRect(endX - periodSelectorBorderVer, 0, drawingWidth, periodSelectorHeight, radiiRight, Path.Direction.CW);
        canvas.drawPath(path, periodSelectorOutsidePaint);
        path.rewind();

        // horizontal borders
        canvas.drawRect(
                startX + periodSelectorBorderVer, -periodSelectorBorderHor, endX - periodSelectorBorderVer, 0,
                currentDrag == DRAG_SELECTOR ? periodSelectorDragBorderPaintDragging : periodSelectorDragBorderPaint
        );
        canvas.drawRect(
                startX + periodSelectorBorderVer, periodSelectorHeight, endX - periodSelectorBorderVer, periodSelectorHeight + periodSelectorBorderHor,
                currentDrag == DRAG_SELECTOR ? periodSelectorDragBorderPaintDragging : periodSelectorDragBorderPaint
        );

        // vertical borders
        path.addRoundRect(startX, -periodSelectorBorderHor, startX + periodSelectorBorderVer, periodSelectorHeight + periodSelectorBorderHor, radiiLeft, Path.Direction.CW);
        canvas.drawPath(path, currentDrag == DRAG_START || currentDrag == DRAG_SELECTOR ? periodSelectorDragBorderPaintDragging : periodSelectorDragBorderPaint);
        path.rewind();
        path.addRoundRect(endX - periodSelectorBorderVer, -periodSelectorBorderHor, endX, periodSelectorHeight + periodSelectorBorderHor, radiiRight, Path.Direction.CW);
        canvas.drawPath(path, currentDrag == DRAG_END || currentDrag == DRAG_SELECTOR ? periodSelectorDragBorderPaintDragging : periodSelectorDragBorderPaint);
        path.rewind();

        // white drag rects
        canvas.drawRoundRect(
                startX + periodSelectorBorderVer / 2 - dp2 / 2,
                periodSelectorHeight / 2 - dp6,
                startX + periodSelectorBorderVer / 2 + dp2 / 2,
                periodSelectorHeight / 2 + dp6,
                dp2,
                dp2,
                periodSelectorWhiteDragPaint
        );
        canvas.drawRoundRect(
                endX - periodSelectorBorderVer / 2 - dp2 / 2,
                periodSelectorHeight / 2 - dp6,
                endX - periodSelectorBorderVer / 2 + dp2 / 2,
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
            float currentLineY = getChartTitleHeight() + chartHeight + datesHeight + periodSelectorHeight + chipsMarginHorizontal + chipMargin;
            for (int c = 0; c < chartData.lines.size(); c++) {
                final ChartData.Line line = chartData.lines.get(c);
                float chipWidth = chipsWidth[c];

                if (chipWidth > drawingWidth + chartPadding - currentLineX) {
                    currentLineX = chartPadding;
                    currentLineY += chipHeight + chipMargin;
                }

                chipsRectOnScreen[c].set(currentLineX, currentLineY, currentLineX + chipWidth, currentLineY + chipHeight);

                if (line.isVisibleOrWillBe) {
                    canvas.drawRoundRect(chipsRectOnScreen[c], chipCornerRadius, chipCornerRadius, chipsFillPaint[c]);
                }

                Drawable drawableCheck = drawablesCheck[c];
                final int checkLeft = (int) (chipsRectOnScreen[c].left + chipPadding / 2 - dp2);
                final int checkTop = (int) (chipsRectOnScreen[c].top + chipPadding / 4 + dp2);
                drawableCheck.setBounds(
                        checkLeft,
                        checkTop,
                        checkLeft + drawableCheck.getIntrinsicWidth(),
                        checkTop + drawableCheck.getIntrinsicHeight()
                );
                drawableCheck.setAlpha(line.alphaInt());
                drawableCheck.draw(canvas);

                canvas.drawRoundRect(chipsRectOnScreen[c], chipCornerRadius, chipCornerRadius, chipsBorderPaint[c]);

                float textTransitionX = line.alpha * (chipPadding / 4 + dp2);
                canvas.drawText(
                        line.name,
                        currentLineX + chipPadding + textTransitionX,
                        currentLineY + chipPadding,
                        line.isVisibleOrWillBe ? chipWhiteTextPaint : chipsTextPaint[c]
                );

                if (lastChipsDownX >= 0 && chipsRectOnScreen[c].contains(lastChipsDownX, lastChipsDownY)) {
                    canvas.drawRoundRect(chipsRectOnScreen[c], chipCornerRadius, chipCornerRadius, highlightChipPaint);
                }

                currentLineX += chipWidth + chipMargin;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawChart(canvas);
        drawSelectionOnChart(canvas);
        drawTitle(canvas);
        drawDates(canvas);
        drawYSteps(canvas);
        drawSelectionPanel(canvas);
        drawChips(canvas);

        canvas.translate(chartPadding, getChartTitleHeight() + chartHeight + datesHeight);

        drawPeriodSelectorChart(canvas);
        drawPeriodSelectorUi(canvas);
    }

    protected float getDrawingWidth() {
        return callbacks.getWidth() - 2 * chartPadding;
    }
}
