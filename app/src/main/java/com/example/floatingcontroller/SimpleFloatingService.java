package com.example.floatingcontroller;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SimpleFloatingService extends Service {
    private static final String TAG = "FloatingService";
    private WindowManager windowManager;
    private View floatingView;
    private long scrollInterval = 40000; // 默认40秒

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            scrollInterval = intent.getLongExtra("scrollInterval", 40000);
        }
        
        Log.d(TAG, "启动悬浮窗服务，滑动间隔: " + scrollInterval + "ms");
        
        // 检查悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "没有悬浮窗权限", Toast.LENGTH_SHORT).show();
                stopSelf();
                return START_NOT_STICKY;
            }
        }
        
        showFloatingWindow();
        
        // ❌ 删除这行 - 不能用startService启动无障碍服务
        // Intent serviceIntent = new Intent(this, AutoScrollAccessibilityService.class);
        // startService(serviceIntent);
        
        // ✅ 正确的方式：检查无障碍服务是否已启用
        checkAccessibilityService();
        
        return START_STICKY;
    }

    private void checkAccessibilityService() {
        if (AutoScrollAccessibilityService.getInstance() != null) {
            // 无障碍服务已启用，设置参数并可以开始使用
            AutoScrollAccessibilityService.getInstance().setScrollInterval(scrollInterval);
            Toast.makeText(this, "无障碍服务已就绪，间隔: " + scrollInterval + "ms", Toast.LENGTH_SHORT).show();
        } else {
            // 无障碍服务未启用，提示用户手动启用
            Toast.makeText(this, "请先在系统设置中启用无障碍服务", Toast.LENGTH_LONG).show();
            // 打开无障碍设置页面
            openAccessibilitySettings();
        }
    }

    private void showFloatingWindow() {
        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            
            // 创建悬浮窗布局
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundColor(0xAA000000); // 半透明黑色背景
            layout.setPadding(30, 30, 30, 30);
            
            // 开始按钮
            Button startButton = new Button(this);
            startButton.setText("开始滑动");
            startButton.setTextColor(0xFFFFFFFF);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AutoScrollAccessibilityService service = AutoScrollAccessibilityService.getInstance();
                    if (service != null) {
                        service.setScrollInterval(scrollInterval);
                        service.startAutoScroll();
                        Toast.makeText(SimpleFloatingService.this, "开始自动滑动", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SimpleFloatingService.this, "请先启用无障碍服务", Toast.LENGTH_SHORT).show();
                        openAccessibilitySettings();
                    }
                }
            });
            
            // 停止按钮
            Button stopButton = new Button(this);
            stopButton.setText("停止滑动");
            stopButton.setTextColor(0xFFFFFFFF);
            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AutoScrollAccessibilityService service = AutoScrollAccessibilityService.getInstance();
                    if (service != null) {
                        service.stopAutoScroll();
                        Toast.makeText(SimpleFloatingService.this, "停止自动滑动", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            
            // 设置按钮
            Button settingsButton = new Button(this);
            settingsButton.setText("无障碍设置");
            settingsButton.setTextColor(0xFFFFFFFF);
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAccessibilitySettings();
                }
            });
            
            // 关闭按钮
            Button closeButton = new Button(this);
            closeButton.setText("关闭悬浮窗");
            closeButton.setTextColor(0xFFFFFFFF);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopSelf();
                }
            });
            
            layout.addView(startButton);
            layout.addView(stopButton);
            layout.addView(settingsButton);
            layout.addView(closeButton);
            
            floatingView = layout;
            
            // 设置悬浮窗参数
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            );
            
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = 200;
            
            windowManager.addView(floatingView, params);
            Log.d(TAG, "悬浮窗创建成功");
            
        } catch (Exception e) {
            Log.e(TAG, "创建悬浮窗失败", e);
            Toast.makeText(this, "创建悬浮窗失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openAccessibilitySettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "打开无障碍设置失败", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "悬浮窗服务销毁");
        
        // 停止自动滑动
        AutoScrollAccessibilityService service = AutoScrollAccessibilityService.getInstance();
        if (service != null) {
            service.stopAutoScroll();
        }
        
        // 移除悬浮窗
        if (floatingView != null && windowManager != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception e) {
                Log.e(TAG, "移除悬浮窗失败", e);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
