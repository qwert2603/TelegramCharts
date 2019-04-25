package com.qwert2603.telegram_charts.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.qwert2603.telegram_charts.DataParser;
import com.qwert2603.telegram_charts.entity.ChartData;

public class GLSurfaceViewArea extends GLSurfaceView {

    private RendererArea renderer;

    public GLSurfaceViewArea(Context context) {
        super(context);
        init(null);
    }

    public GLSurfaceViewArea(Context context, ChartData chartData) {
        super(context);
        init(chartData);
    }

    public GLSurfaceViewArea(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(null);
    }

    private void init(ChartData chartData) {
        if (chartData == null) {
            chartData = DataParser.parseDataStage2(getContext()).get(4);
        }

        setEGLContextClientVersion(2);
        setEGLConfigChooser(new AntiAliasConfigChooser());
        renderer = new RendererArea(getContext(), chartData);
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

    public void setNight(boolean night) {
        renderer.setNight(night);
    }
}
