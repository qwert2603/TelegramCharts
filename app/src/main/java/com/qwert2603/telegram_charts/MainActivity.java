package com.qwert2603.telegram_charts;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<ChartData> chartDataList = DataParser.parseData(getApplicationContext());

        ListView listView = findViewById(R.id.listView);
        listView.setDivider(null);
        listView.setAdapter(new ChartsAdapter(this, 0, chartDataList));
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

    static class ChartsAdapter extends ArrayAdapter<ChartData> {
        public ChartsAdapter(Context context, int resource, List<ChartData> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ChartView chartView = (ChartView) layoutInflater.inflate(R.layout.item_chart, parent, false);
            chartView.setChartData(getItem(position));
            return chartView;
        }
    }
}
