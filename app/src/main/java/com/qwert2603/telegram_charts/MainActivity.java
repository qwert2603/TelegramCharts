package com.qwert2603.telegram_charts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.qwert2603.telegram_charts.entity.ChartData;

import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<ChartData> chartDataList = DataParser.parseData(getApplicationContext());

        final LinearLayout linearLayout = findViewById(R.id.linearLayout);
        for (int i = 0; i < chartDataList.size(); i++) {
            ViewGroup view = (ViewGroup) getLayoutInflater().inflate(R.layout.item_chart, linearLayout, false);
            linearLayout.addView(view);

            final ChartData chartData = chartDataList.get(i);
            final ChartCustomView chartView = new ChartCustomView(this, "Chart #" + i, chartData);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartView.measureHeight());
            view.addView(chartView, layoutParams);

            LinearLayout.LayoutParams qLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.dp12) * 2);
            View child = new View(this);
            child.setBackgroundColor(0xffe0e0e0);
            view.addView(child, qLayoutParams);
        }

//        final ScrollView scrollView = findViewById(R.id.scrollView);
//        scrollView.getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
//            @Override
//            public void onDraw() {
//                for (int i = 0; i < linearLayout.getChildCount(); i++) {
//                    ViewGroup childAt = (ViewGroup) linearLayout.getChildAt(i);
//                    ChartTextureView chartTextureView = (ChartTextureView) childAt.getChildAt(0);
//                    int chT = childAt.getTop();
//                    int chB = childAt.getBottom();
//                    int scT = scrollView.getScrollY();
//                    int scB = scrollView.getScrollY() + scrollView.getHeight();
//                    LogUtils.d(String.format("chartTextureView.isVisible %d %d %d %d", chT, chB, scT, scB));
//                    boolean isVisible = scT <= chT && chT <= scB
//                            || scT <= chB && chB <= scB
//                            /*|| chT <= scT && scT <= chB*/;
//                    LogUtils.d("chartTextureView.isVisible " + i + " " + isVisible);
//                    chartTextureView.isVisible = isVisible;
//                }
//            }
//        });
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