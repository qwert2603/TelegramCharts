package com.qwert2603.telegram_charts.q_gl.h;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.qwert2603.telegram_charts.DataParser;
import com.qwert2603.telegram_charts.entity.ChartData;

public class AreaGLSurfaceView extends GLSurfaceView {

    private LessonOneRenderer renderer;

    public AreaGLSurfaceView(Context context) {
        super(context);
        init(null);
    }

    public AreaGLSurfaceView(Context context, ChartData chartData) {
        super(context);
        init(chartData);
    }

    public AreaGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(null);
    }

    private void init(ChartData chartData) {
        if (chartData == null) {
            chartData = DataParser.parseDataStage2(getContext()).get(4);
        }

        setEGLContextClientVersion(2);
        renderer = new LessonOneRenderer(getContext(), chartData);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void setChartsSizes(float main, float divider, float periodSelector) {
        renderer.setChartsSizes(main, divider, periodSelector);
    }

    public void setAlpha(int line, float alpha) {
        renderer.alpha[line] = alpha;
    }

    public void setPeriodIndices(int startIndex, int endIndex) {
        renderer.setPeriodIndices(startIndex, endIndex);
    }
}
