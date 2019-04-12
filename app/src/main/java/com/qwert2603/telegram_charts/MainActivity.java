package com.qwert2603.telegram_charts;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toolbar;

import com.qwert2603.telegram_charts.entity.ChartData;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String[] CHART_NAMES = {
            "Followers",
            "Interactions",
            "Fruits",
            "Views",
            "Fruits",
            "Test",
    };

    public static boolean NIGHT_MODE = false;

    private LinearLayout linearLayout;
    private Toolbar toolbar;
    private ScrollView scrollView;

    private List<View> views = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        List<ChartData> chartDataList = DataParser.parseDataStage2(getApplicationContext());

        final int dividerHeight = (int) getResources().getDimension(R.dimen.dates_height);
        linearLayout = findViewById(R.id.linearLayout);
        for (int i = 0; i < chartDataList.size(); i++) {
            ViewGroup view = (ViewGroup) getLayoutInflater().inflate(R.layout.item_chart, linearLayout, false);
            linearLayout.addView(view);

            final ChartData chartData = chartDataList.get(i);
            final ChartCustomView chartView = new ChartCustomView(this, CHART_NAMES[i], chartData);
            views.add(chartView);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartView.measureHeight());
            view.addView(chartView, layoutParams);

            LinearLayout.LayoutParams qLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight);
            View child = new DividerView(this);
            views.add(child);
            view.addView(child, qLayoutParams);
        }

        scrollView = findViewById(R.id.scrollView);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
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

        setNightMode(NIGHT_MODE, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.night_mode) {
            setNightMode(!NIGHT_MODE, item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setNightMode(boolean night, MenuItem menuItem) {
        NIGHT_MODE = night;

        Drawable drawable = getDrawable(R.drawable.ic_moon);
        drawable.setTint(NIGHT_MODE ? Color.WHITE : 0xFF8e8e93);
        if (menuItem != null) menuItem.setIcon(drawable);

        toolbar.setTitleTextColor(NIGHT_MODE ? Color.WHITE : Color.BLACK);
        toolbar.setBackgroundColor(NIGHT_MODE ? 0xFF242f3e : Color.WHITE);
        linearLayout.setBackgroundColor(NIGHT_MODE ? 0xFF242f3e : Color.WHITE);

        for (View view : views) {
            view.invalidate();
        }

    }
}