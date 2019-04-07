package com.qwert2603.telegram_charts;

import java.util.List;

public class ChartData {

    static class Line {
        String color;
        String name;
        int[] values;
    }

    long[] xValues;
    List<Line> lines;
}
