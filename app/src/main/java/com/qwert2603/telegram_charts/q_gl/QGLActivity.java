package com.qwert2603.telegram_charts.q_gl;

import android.app.Activity;
import android.os.Bundle;

public class QGLActivity extends Activity {

    private QGLSurfaceView qglSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        qglSurfaceView = new QGLSurfaceView(this);
        setContentView(qglSurfaceView);
    }
}
