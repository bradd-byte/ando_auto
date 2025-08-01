package com.example.floatingcontroller;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AutoScrollAccessibilityService extends AccessibilityService {
    private static final String TAG = "AutoScrollService";
    private static AutoScrollAccessibilityService instance;
    private Handler handler;
    private Runnable scrollRunnable;
    private long intervalMillis = 40000; // 默认40秒
    private boolean isAutoScrolling = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d(TAG, "无障碍服务已连接");
        
        // 不要在这里自动启动滑动，而是等待外部调用
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
    }

    // ❌ 删除这个方法 - 无障碍服务不应该有 onStartCommand
    // @Override
    // public int onStartCommand(Intent intent, int flags, int startId) {
    //     // 这个方法会导致闪退
    // }

    public static AutoScrollAccessibilityService getInstance() {
        return instance;
    }

    public void setScrollInterval(long millis) {
        this.intervalMillis = millis;
        Log.d(TAG, "设置滑动间隔: " + millis + "ms");
    }

    public void startAutoScroll() {
        // 检查Android版本
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Android版本过低，不支持手势分发功能");
            return;
        }

        if (isAutoScrolling) {
            Log.d(TAG, "自动滑动已在运行中");
            return;
        }

        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }

        isAutoScrolling = true;

        scrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAutoScrolling) {
                    performScrollGesture();
                    // 安排下一次执行
                    if (handler != null) {
                        handler.postDelayed(this, intervalMillis);
                    }
                }
            }
        };

        handler.post(scrollRunnable);
        Log.d(TAG, "自动滑动已启动，间隔: " + intervalMillis + "ms");
    }

    public void stopAutoScroll() {
        isAutoScrolling = false;
        if (handler != null && scrollRunnable != null) {
            handler.removeCallbacks(scrollRunnable);
        }
        Log.d(TAG, "自动滑动已停止");
    }

    private void performScrollGesture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                // 获取屏幕尺寸 - 动态计算，不使用硬编码
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int screenHeight = displayMetrics.heightPixels;
                int screenWidth = displayMetrics.widthPixels;

                // 创建滑动路径
                Path path = new Path();
                float startX = screenWidth / 2f;        // 屏幕中央
                float startY = screenHeight * 0.8f;     // 从80%位置开始
                float endY = screenHeight * 0.2f;       // 滑动到20%位置

                path.moveTo(startX, startY);
                path.lineTo(startX, endY);

                GestureDescription.StrokeDescription stroke = 
                    new GestureDescription.StrokeDescription(path, 0, 500);
                    
                GestureDescription gesture = 
                    new GestureDescription.Builder().addStroke(stroke).build();

                boolean dispatched = dispatchGesture(gesture, 
                    new GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            Log.d(TAG, "滑动手势执行完成");
                        }

                        @Override
                        public void onCancelled(GestureDescription gestureDescription) {
                            Log.w(TAG, "滑动手势被取消");
                        }
                    }, null);

                if (dispatched) {
                    Log.d(TAG, "滑动手势已分发 (" + startX + ", " + startY + ") -> (" + startX + ", " + endY + ")");
                } else {
                    Log.e(TAG, "滑动手势分发失败");
                }

            } catch (Exception e) {
                Log.e(TAG, "执行滑动手势时发生错误", e);
            }
        } else {
            Log.e(TAG, "当前Android版本不支持手势分发");
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 处理无障碍事件（如果需要）
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "无障碍服务被中断");
        stopAutoScroll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAutoScroll();
        instance = null;
        Log.d(TAG, "无障碍服务已销毁");
    }
}
