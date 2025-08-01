package com.example.floatingcontroller;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        
        Button permissionBtn = new Button(this);
        permissionBtn.setText("获取悬浮窗权限");
        permissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestOverlayPermission();
            }
        });
        
        Button startBtn = new Button(this);
        startBtn.setText("启动悬浮窗");
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canDrawOverlays()) {
                    startService(new Intent(MainActivity.this, SimpleFloatingService.class));
                    Toast.makeText(MainActivity.this, "悬浮窗已启动", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "请先获取悬浮窗权限", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        layout.addView(permissionBtn);
        layout.addView(startBtn);
        setContentView(layout);
    }
    
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }
    
    private boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }
}
