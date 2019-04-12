//package com.qwert2603.telegram_charts;
//
//import android.annotation.TargetApi;
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.PorterDuff;
//import android.graphics.SurfaceTexture;
//import android.os.Build;
//import android.os.SystemClock;
//import android.view.MotionEvent;
//import android.view.Surface;
//import android.view.TextureView;
//
//import com.qwert2603.telegram_charts.chart_delegates.Callbacks;
//import com.qwert2603.telegram_charts.chart_delegates.ChartViewDelegateBars;
//import com.qwert2603.telegram_charts.entity.ChartData;
//
//@TargetApi(Build.VERSION_CODES.M)
//public class ChartTextureView extends TextureView {
//
//    private final ChartViewDelegateBars chartViewDelegateBars;
//
//    private RenderThread mThread;
//
//    public volatile boolean isVisible;
//    public volatile boolean needRedraw;
//
//    public ChartTextureView(Context context, String title, ChartData chartData) {
//        super(context);
//        setSurfaceTextureListener(new CanvasListener());
//        setOpaque(false);
//
//        chartViewDelegateBars = new ChartViewDelegateBars(context, title, chartData, new Callbacks() {
//            @Override
//            public void invalidate() {
//                needRedraw = true;
//            }
//
//            @Override
//            public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//                ChartTextureView.this.getParent().requestDisallowInterceptTouchEvent(disallowIntercept);
//            }
//
//            @Override
//            public float getWidth() {
//                return ChartTextureView.this.getWidth();
//            }
//        });
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        chartViewDelegateBars.onTouchEvent(event);
//        return true;
//    }
//
//    private class RenderThread extends Thread {
//        private volatile boolean mRunning = true;
//
//        @Override
//        public void run() {
//            final Surface surface = new Surface(ChartTextureView.this.getSurfaceTexture());
//
//            while (mRunning) {
//                final long drawStart = SystemClock.elapsedRealtime();
//
//                if (isVisible && needRedraw) {
//                    needRedraw = false;
//                    final Canvas canvas = surface.lockHardwareCanvas();
//                    try {
//                        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
//                        chartViewDelegateBars.onDraw(canvas);
//                    } finally {
//                        surface.unlockCanvasAndPost(canvas);
//                    }
//                }
//
//                final long drawMillis = SystemClock.elapsedRealtime() - drawStart;
//                long millisToSleep = 17 - drawMillis;
//                if (millisToSleep < 0) millisToSleep = 0;
//
//                try {
//                    Thread.sleep(millisToSleep);
//                } catch (InterruptedException ignored) {
//                }
//            }
//
//            surface.release();
//        }
//
//        public void stopRendering() {
//            mRunning = false;
//        }
//
//    }
//
//    private class CanvasListener implements SurfaceTextureListener {
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//            mThread = new RenderThread();
//            mThread.start();
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//            if (mThread != null) {
//                mThread.stopRendering();
//            }
//            return true;
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        }
//    }
//}
