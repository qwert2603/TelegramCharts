package com.qwert2603.telegram_charts;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

public class Canvas2Activity extends Activity {
    public static String TAG = "CanvasActivity";
    private TextureView mTextureView;
    private RenderThread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content);

        mTextureView = findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(new CanvasListener());
        mTextureView.setOpaque(false);
    }

    private class RenderThread extends Thread {
        private volatile boolean mRunning = true;

        @Override
        public void run() {
            float x = 0.0f;
            float y = 0.0f;
            float speedX = 5.0f;
            float speedY = 3.0f;
            float[] floats = new float[6000];

            Paint paint = new Paint();
            paint.setColor(0xff00ff00);

            while (mRunning && !Thread.interrupted()) {
                final Canvas canvas = mTextureView.lockCanvas(null);
                try {
                    canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
                    canvas.drawRect(x, y, x + 20.0f, y + 20.0f, paint);

                    LogUtils.d("qqqq 1 " + canvas.isHardwareAccelerated());

                    int q = 0;
                    for (int i = 0; i < floats.length / 4; i++) {
                        final float _x = i % 2 == 0 ? i / 10f : 600;
                        final float _y = i / 20f;

                        floats[q++] = _x;
                        floats[q++] = _y;
                        if (i != 0 && i != floats.length / 4 - 1) {
                            floats[q++] = _x;
                            floats[q++] = _y;
                        }
                    }
                    LogUtils.d("qqqq 2");
                    canvas.drawLines(floats, paint);
                    LogUtils.d("qqqq 3");

                } finally {
                    mTextureView.unlockCanvasAndPost(canvas);
                }

                if (x + 20.0f + speedX >= mTextureView.getWidth()
                        || x + speedX <= 0.0f) {
                    speedX = -speedX;
                }
                if (y + 20.0f + speedY >= mTextureView.getHeight()
                        || y + speedY <= 0.0f) {
                    speedY = -speedY;
                }

                x += speedX;
                y += speedY;

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
//                     Interrupted
                }
            }
        }

        public void stopRendering() {
            interrupt();
            mRunning = false;
        }

    }

    private class CanvasListener implements SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface,
                                              int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable");
            mThread = new RenderThread();
            mThread.start();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureDestroyed");
            if (mThread != null) {
                mThread.stopRendering();
            }
            return true;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureUpdated");
        }

    }
}
