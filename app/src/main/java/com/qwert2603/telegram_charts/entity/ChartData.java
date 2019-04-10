package com.qwert2603.telegram_charts.entity;

import java.util.List;

public class ChartData {

    public static class Line {
        public int color;
        public String name;
        public int[] values;

        public int alpha = 0xFF;

        public boolean isVisible() {
            return alpha > 0;
        }

        public boolean isVisibleOrWillBe = true;
    }

    public long[] xValues;
    public String[] dates;
    public String[] fullDates;
    public String[] selectedDates;
    public List<Line> lines;

    public int[] calcYLimits(int startIndex, int endIndex) {
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

        return new int[]{minY, maxY, totalMinY, totalMaxY};
    }
}
