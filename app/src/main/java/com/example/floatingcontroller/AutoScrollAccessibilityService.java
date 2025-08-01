package com.example.ando_auto;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

public class AutoScrollAccessibilityService extends AccessibilityService {

    private Handler handler;
    private long intervalMillis = 40000; // 默认 40 秒间隔

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        handler = new Handler(Looper.getMainLooper());
        startAutoScroll();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 暂不需要处理事件
    }

    @Override
    public void onInterrupt() {
        stopAutoScroll();
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
        path.moveTo(500, 1500); // 起点坐标 (可调)
        path.lineTo(500, 500);  // 终点坐标，向上滑动

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 500);
        GestureDescription gesture = new GestureDescription.Builder().addStroke(stroke).build();
        dispatchGesture(gesture, null, null);
    }

    // 你可以添加一个 setter 方法用于修改 intervalMillis
    public void updateInterval(long millis) {
        this.intervalMillis = millis;
    }
}
