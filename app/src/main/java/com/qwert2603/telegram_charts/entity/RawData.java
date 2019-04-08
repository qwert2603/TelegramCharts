package com.qwert2603.telegram_charts.entity;

import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RawData {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM");

    public List<List<Object>> columns;
    public Map<String, String> types;
    public Map<String, String> names;
    public Map<String, String> colors;

    public ChartData toChartData() {
        ChartData chartData = new ChartData();
        int valuesCount = columns.get(0).size() - 1;
        chartData.xValues = new long[valuesCount];
        chartData.dates = new String[valuesCount];
        for (int i = 0; i < valuesCount; i++) {
            long millis = ((Double) columns.get(0).get(i + 1)).longValue();
            chartData.xValues[i] = millis;
            chartData.dates[i] = DATE_FORMAT.format(new Date(chartData.xValues[i]));
        }

        chartData.lines = new ArrayList<>();

        for (String name : names.keySet()) {
            ChartData.Line line = new ChartData.Line();
            chartData.lines.add(line);
            line.name = names.get(name);
            line.color = Color.parseColor(colors.get(name));
            line.values = new int[valuesCount];

            List<Object> values = null;
            for (List<Object> column : columns) {
                if (column.get(0).equals(name)) {
                    values = column;
                    break;
                }
            }
            for (int i = 0; i < valuesCount; i++) {
                line.values[i] = ((Double) values.get(i + 1)).intValue();
            }
        }

        return chartData;
    }

    public RawData() {
    }

    public RawData(List<List<Object>> columns, Map<String, String> types, Map<String, String> names, Map<String, String> colors) {
        this.columns = columns;
        this.types = types;
        this.names = names;
        this.colors = colors;
    }

    public List<List<Object>> getColumns() {
        return columns;
    }

    public void setColumns(List<List<Object>> columns) {
        this.columns = columns;
    }

    public Map<String, String> getTypes() {
        return types;
    }

    public void setTypes(Map<String, String> types) {
        this.types = types;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

    public Map<String, String> getColors() {
        return colors;
    }

    public void setColors(Map<String, String> colors) {
        this.colors = colors;
    }
}
