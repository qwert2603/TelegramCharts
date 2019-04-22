package com.qwert2603.telegram_charts;

import android.content.Context;

import com.qwert2603.telegram_charts.entity.ChartData;
import com.qwert2603.telegram_charts.entity.RawData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DataParser {

    private static final boolean WITH_TEST_CHART = true;

    public static List<ChartData> parseDataStage1(Context appContext) {
//        try {
//            InputStream inputStream = appContext.getAssets().open("chart_data.json");
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//
//            StringBuilder stringBuilder = new StringBuilder();
//
//            String s = bufferedReader.readLine();
//            stringBuilder.append(s);
//            while (true) {
//                s = bufferedReader.readLine();
//                if (s == null) break;
//                stringBuilder.append(s);
//            }
//
//            Type rawDataType = new TypeToken<List<RawData>>() {
//            }.getType();
//
//            List<RawData> parsedCharts = GSON.fromJson(stringBuilder.toString(), rawDataType);
//
//            List<ChartData> result = new ArrayList<>(parsedCharts.size());
//            for (RawData parsedChart : parsedCharts) {
//                result.add(parsedChart.toChartData());
//            }
//            return result;
//        } catch (Exception e) {
//            e.printStackTrace();
        return null;
//        }
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

                JSONObject jsonObject = new JSONObject(stringBuilder.toString());

                RawData parsedChart = new RawData();
                parsedChart.y_scaled = jsonObject.optBoolean("y_scaled");

                parsedChart.types = new HashMap<>();
                JSONObject typesJson = jsonObject.getJSONObject("types");
                Iterator<String> types = typesJson.keys();
                while (types.hasNext()) {
                    String next = types.next();
                    parsedChart.types.put(next, typesJson.getString(next));
                }

                parsedChart.names = new HashMap<>();
                JSONObject namesJson = jsonObject.getJSONObject("names");
                Iterator<String> names = namesJson.keys();
                while (names.hasNext()) {
                    String next = names.next();
                    parsedChart.names.put(next, namesJson.getString(next));
                }

                parsedChart.colors = new HashMap<>();
                JSONObject colorsJson = jsonObject.getJSONObject("colors");
                Iterator<String> colors = colorsJson.keys();
                while (colors.hasNext()) {
                    String next = colors.next();
                    parsedChart.colors.put(next, colorsJson.getString(next));
                }

                parsedChart.columns = new ArrayList<>();
                JSONArray columnsJson = jsonObject.getJSONArray("columns");
                for (int j = 0; j < columnsJson.length(); j++) {
                    List<Object> objects = new ArrayList<>();
                    parsedChart.columns.add(objects);

                    JSONArray values = columnsJson.getJSONArray(j);
                    for (int k = 0; k < values.length(); k++) {
                        objects.add(values.get(k));
                    }
                }

                result.add(parsedChart.toChartData());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String readAsset(Context context, String name) {
        try {
            InputStream inputStream = context.getAssets().open(name);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder stringBuilder = new StringBuilder();

            String s = bufferedReader.readLine();
            stringBuilder.append(s);
            while (true) {
                s = bufferedReader.readLine();
                if (s == null) break;
                stringBuilder.append(s);
            }

            inputStream.close();

            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
