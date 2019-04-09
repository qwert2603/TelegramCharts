package com.qwert2603.telegram_charts;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qwert2603.telegram_charts.entity.ChartData;

import java.util.List;

public class MainActivity extends Activity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<ChartData> chartDataList = DataParser.parseData(getApplicationContext());

        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        for (int i = 0; i < chartDataList.size(); i++) {
            ViewGroup view = (ViewGroup) getLayoutInflater().inflate(R.layout.item_chart, linearLayout, false);
            linearLayout.addView(view);

            final ChartData chartData = chartDataList.get(i);
            final ChartView chartView = new ChartView(this, "Chart #" + i, chartData);
            int chartHeight = (int) getResources().getDimension(R.dimen.chart_view_height);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight);
            view.addView(chartView, layoutParams);

            for (final ChartData.Line line : chartData.lines) {
                CheckBox checkBox = (CheckBox) getLayoutInflater().inflate(R.layout.item_checkbox, view, false);
                view.addView(checkBox);
                checkBox.setText(line.name);
                checkBox.setTextColor(line.color);
//   todo             checkBox.setButtonTintList(ColorStateList.valueOf(line.color));
                checkBox.setChecked(true);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        chartView.setLineVisible(line.name, isChecked);
                    }
                });
            }

            LinearLayout.LayoutParams qLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.dp12) * 2);
            View child = new View(this);
            child.setBackgroundColor(0xffe0e0e0);
            view.addView(child, qLayoutParams);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.night_mode) {
            //todo
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


/*
 * чекбоксы chips
 * ночной режим
 * обработка ситуации, когда все линии отключены
 */