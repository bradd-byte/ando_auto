package com.example.floatingcontroller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        Button permissionBtn = new Button(this);
        permissionBtn.setText("获取悬浮窗权限");
        layout.addView(permissionBtn);

        EditText intervalInput = new EditText(this);
        intervalInput.setHint("滑动间隔(ms)");
        intervalInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(intervalInput);

        Button startBtn = new Button(this);
        startBtn.setText("启动悬浮窗");
        layout.addView(startBtn);

        setContentView(layout);

        permissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Settings.canDrawOverlays(MainActivity.this)) {
                    String intervalStr = intervalInput.getText().toString();
                    long intervalMillis = 40000;
                    if (!intervalStr.isEmpty()) {
                        intervalMillis = Long.parseLong(intervalStr);
                    }

                    Intent intent = new Intent(MainActivity.this, SimpleFloatingService.class);
                    intent.putExtra("scrollInterval", intervalMillis);
                    startService(intent);

                    Toast.makeText(MainActivity.this, "已启动，间隔：" + intervalMillis + "ms", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "请先获取悬浮窗权限", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
