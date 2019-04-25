package com.qwert2603.telegram_charts.entity;

import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RawData {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMM");
    private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("d MMMM yyyy");
    private static final SimpleDateFormat SELECTED_DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy");

    public List<List<Object>> columns;
    public Map<String, String> types;
    public Map<String, String> names;
    public Map<String, String> colors;
    public boolean y_scaled;
//    public boolean stacked;
//    public boolean percentage;

    public ChartData toChartData() {
        ChartData chartData = new ChartData();

        final String typeString = types.get(columns.get(1).get(0));
        if (typeString.equals("line")) {
            chartData.type = y_scaled ? ChartData.Type.LINES_2_Y : ChartData.Type.LINES;
        } else if (typeString.equals("bar")) {
            chartData.type = ChartData.Type.BARS;
        } else if (typeString.equals("area")) {
            chartData.type = ChartData.Type.AREA;
        }

        int valuesCount = columns.get(0).size() - 1;
        chartData.xValues = new long[valuesCount];
        chartData.dates = new String[valuesCount];
        chartData.fullDates = new String[valuesCount];
        chartData.selectedDates = new String[valuesCount];
        for (int i = 0; i < valuesCount; i++) {
            long millis = (long) columns.get(0).get(i + 1);
            chartData.xValues[i] = millis;
            chartData.dates[i] = DATE_FORMAT.format(new Date(chartData.xValues[i]));
            chartData.fullDates[i] = FULL_DATE_FORMAT.format(new Date(chartData.xValues[i]));
            chartData.selectedDates[i] = SELECTED_DATE_FORMAT.format(new Date(chartData.xValues[i]));
        }
        chartData.xStep = chartData.xValues[1] - chartData.xValues[0];

        chartData.lines = new ArrayList<>();

        for (int c = 1; c < columns.size(); c++) {
            String name = (String) columns.get(c).get(0);
            ChartData.Line line = new ChartData.Line();
            chartData.lines.add(line);
            line.name = names.get(name);
            line.color = Color.parseColor(colors.get(name));
            line.values = new int[valuesCount];

            List<Object> values = columns.get(c);
            for (int i = 0; i < valuesCount; i++) {
                line.values[i] = (int) values.get(i + 1);
            }
        }

        return chartData;
    }
}
