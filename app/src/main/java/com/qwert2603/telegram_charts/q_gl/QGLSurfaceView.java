package com.qwert2603.telegram_charts.q_gl;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class QGLSurfaceView extends GLSurfaceView {

    private final QGLRenderer renderer;

    public QGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);

        renderer = new QGLRenderer();
        setRenderer(renderer);

        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
