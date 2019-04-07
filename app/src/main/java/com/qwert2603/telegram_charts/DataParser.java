package com.qwert2603.telegram_charts;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataParser {

    public static List<ChartData> parseData(Context appContext) {
        try {
            InputStream inputStream = appContext.getAssets().open("chart_data.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String s = bufferedReader.readLine();

            Type rawDataType = new TypeToken<List<RawData>>() {
            }.getType();

            List<RawData> parsedCharts = new Gson().fromJson(s, rawDataType);

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

}
