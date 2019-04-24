package com.qwert2603.telegram_charts.q_gl.h;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.qwert2603.telegram_charts.DataParser;
import com.qwert2603.telegram_charts.entity.ChartData;

public class AreaGLSurfaceView extends GLSurfaceView {

    public LessonOneRenderer renderer;

    public AreaGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public AreaGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ChartData chartData = DataParser.parseDataStage2(getContext()).get(4);

        setEGLContextClientVersion(2);
        renderer = new LessonOneRenderer(getContext(), chartData);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public float getAlpha(int line) {
        return renderer.getAlpha(line);
    }

    public void setAlpha(int line, float alpha) {
        renderer.setAlpha(line, alpha);
        requestRender();
    }
}
