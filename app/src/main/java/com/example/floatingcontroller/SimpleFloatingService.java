package com.example.floatingcontroller;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SimpleFloatingService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private Handler handler = new Handler();
    private boolean autoMode = false;
    private Runnable autoRunnable;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatingWindow();
    }

    private void createFloatingWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#80000000"));
        layout.setPadding(10, 10, 10, 10);

        Button clickBtn = new Button(this);
        clickBtn.setText("模拟点击");
        clickBtn.setTextSize(10);
        clickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simulateClick();
            }
        });

        Button autoBtn = new Button(this);
        autoBtn.setText("自动模式");
        autoBtn.setTextSize(10);
        autoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAutoMode(autoBtn);
            }
        });

        Button closeBtn = new Button(this);
        closeBtn.setText("关闭");
        closeBtn.setTextSize(10);
        closeBtn.setBackgroundColor(Color.RED);
        closeBtn.setTextColor(Color.WHITE);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });

        layout.addView(clickBtn);
        layout.addView(autoBtn);
        layout.addView(closeBtn);
        floatingView = layout;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSLUCENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 100;
        params.y = 200;

        windowManager.addView(floatingView, params);
        makeDraggable(params);
    }

    private void makeDraggable(final WindowManager.LayoutParams params) {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void simulateClick() {
        Toast.makeText(this, "执行点击操作", Toast.LENGTH_SHORT).show();
    }

    private void toggleAutoMode(Button btn) {
        autoMode = !autoMode;
        if (autoMode) {
            btn.setText("停止自动");
            startAutoMode();
        } else {
            btn.setText("自动模式");
            stopAutoMode();
        }
    }

    private void startAutoMode() {
        autoRunnable = new Runnable() {
            @Override
            public void run() {
                if (autoMode) {
                    simulateClick();
                    handler.postDelayed(this, 3000);
                }
            }
        };
        handler.post(autoRunnable);
    }

    private void stopAutoMode() {
        if (autoRunnable != null) {
            handler.removeCallbacks(autoRunnable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
        stopAutoMode();
    }
}
