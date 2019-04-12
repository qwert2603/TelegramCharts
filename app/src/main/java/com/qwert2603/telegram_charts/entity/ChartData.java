package com.qwert2603.telegram_charts.entity;

import android.graphics.Paint;
import android.graphics.RectF;

import java.util.List;

public class ChartData {

    public enum Type {
        LINES,
        LINES_2_Y,
        BARS,
        AREA,
    }

    public static class Line {
        public int color;
        public String name;
        public int[] values;

        public int alpha = 0xFF;

        public boolean isVisible() {
            return alpha > 0;
        }

        public boolean isVisibleOrWillBe = true;

        public final RectF chipRectOnScreen = new RectF();

        public float chipTextWidth;

        public final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        public final Paint linePeriodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        public final Paint chipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        public final Paint chipBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        public final Paint panelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public Type type;
    public long[] xValues;
    public String[] dates;
    public String[] fullDates;
    public String[] selectedDates;
    public List<Line> lines;

    public float xStep;

    public int[] calcYLimits(int startIndex, int endIndex) {
        if (type == Type.AREA) return new int[]{0, 100, 0, 100};
        if (type == Type.BARS) return calcYLimitsBars(startIndex, endIndex);
        return calcYLimitsLines(startIndex, endIndex);
    }

    private int[] calcYLimitsBars(int startIndex, int endIndex) {
        int maxY = 0;
        int totalMaxY = 0;

        for (int i = 0; i < xValues.length; i++) {
            int y = 0;
            for (ChartData.Line line : lines) {
                if (line.isVisibleOrWillBe) {
                    y += line.values[i];
                }
            }
            if (y > totalMaxY) totalMaxY = y;
            if (startIndex <= i && i < endIndex && y > maxY) maxY = y;
        }

        if (maxY == 0) return new int[]{0, 116, 0, 116};

        return new int[]{0, maxY, 0, totalMaxY};
    }

    private int[] calcYLimitsLines(int startIndex, int endIndex) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int totalMinY = Integer.MAX_VALUE;
        int totalMaxY = Integer.MIN_VALUE;

        for (ChartData.Line line : lines) {
            if (line.isVisibleOrWillBe) {
                int[] values = line.values;
                for (int i = startIndex; i < endIndex; i++) {
                    int y = values[i];
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
                for (int i = 0; i < xValues.length; i++) {
                    int y = values[i];
                    if (y < totalMinY) totalMinY = y;
                    if (y > totalMaxY) totalMaxY = y;
                }
            }
        }

        if (minY == Integer.MAX_VALUE) return new int[]{0, 116, 0, 116};

        return new int[]{minY, maxY, totalMinY, totalMaxY};
    }

    public boolean isAnyLineVisible() {
        for (Line line : lines) {
            if (line.isVisibleOrWillBe) return true;
        }
        return false;
    }
}
