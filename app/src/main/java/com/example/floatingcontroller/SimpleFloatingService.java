package com.example.floatingcontroller;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class SimpleFloatingService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long intervalMillis = intent.getLongExtra("scrollInterval", 40000);

        showFloatingWindow();

        // 启动辅助服务并传递间隔
        Intent serviceIntent = new Intent(this, AutoScrollAccessibilityService.class);
        serviceIntent.putExtra("scrollInterval", intervalMillis);
        startService(serviceIntent);

        Toast.makeText(this, "辅助服务已启动", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    private void showFloatingWindow() {
        // 可选：添加悬浮窗 UI
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
