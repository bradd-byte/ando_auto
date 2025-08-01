package com.example.floatingcontroller;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

public class AutoScrollAccessibilityService extends AccessibilityService {

    private Handler handler;
    private long intervalMillis = 40000;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        handler = new Handler(Looper.getMainLooper());
        startAutoScroll();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long interval = intent.getLongExtra("scrollInterval", 40000);
        updateInterval(interval);
        return super.onStartCommand(intent, flags, startId);
    }

    public void updateInterval(long millis) {
        this.intervalMillis = millis;
        stopAutoScroll();
        startAutoScroll();
    }

    private void startAutoScroll() {
        handler.postDelayed(scrollRunnable, intervalMillis);
    }

    private void stopAutoScroll() {
        if (handler != null) {
            handler.removeCallbacks(scrollRunnable);
        }
    }

    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            performScrollGesture();
            handler.postDelayed(this, intervalMillis);
        }
    };

    private void performScrollGesture() {
        Path path = new Path();
        path.moveTo(500, 1500);
        path.lineTo(500, 500);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 500);
        GestureDescription gesture = new GestureDescription.Builder().addStroke(stroke).build();
        dispatchGesture(gesture, null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {
        stopAutoScroll();
    }
}
