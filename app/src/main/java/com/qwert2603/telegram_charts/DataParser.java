package com.qwert2603.telegram_charts;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qwert2603.telegram_charts.entity.ChartData;
import com.qwert2603.telegram_charts.entity.RawData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataParser {

    private static final Gson GSON = new Gson();

    private static final boolean WITH_TEST_CHART = false;

    public static List<ChartData> parseDataStage1(Context appContext) {
        try {
            InputStream inputStream = appContext.getAssets().open("chart_data.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder stringBuilder = new StringBuilder();

            String s = bufferedReader.readLine();
            stringBuilder.append(s);
            while (true) {
                s = bufferedReader.readLine();
                if (s == null) break;
                stringBuilder.append(s);
            }

            Type rawDataType = new TypeToken<List<RawData>>() {
            }.getType();

            List<RawData> parsedCharts = GSON.fromJson(stringBuilder.toString(), rawDataType);

            List<ChartData> result = new ArrayList<>(parsedCharts.size());
            for (RawData parsedChart : parsedCharts) {
                result.add(parsedChart.toChartData());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ChartData> parseDataStage2(Context appContext) {
        int chartsCount = WITH_TEST_CHART ? 8 : 5;

        final List<ChartData> result = new ArrayList<>(chartsCount);

        try {
            for (int i = 1; i <= chartsCount; i++) {
                InputStream inputStream = appContext.getAssets().open(i + "/overview.json");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder stringBuilder = new StringBuilder();

                String s = bufferedReader.readLine();
                stringBuilder.append(s);
                while (true) {
                    s = bufferedReader.readLine();
                    if (s == null) break;
                    stringBuilder.append(s);
                }

                RawData parsedChart = GSON.fromJson(stringBuilder.toString(), RawData.class);
                result.add(parsedChart.toChartData());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
