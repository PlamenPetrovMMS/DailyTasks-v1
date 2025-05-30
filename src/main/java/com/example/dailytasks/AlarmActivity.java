package com.example.dailytasks;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AlarmActivity extends AppCompatActivity {



    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("AlarmActivity", "AlarmActivity started");
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);

            KeyguardManager km = getSystemService(KeyguardManager.class);
            if (km != null) {
                km.requestDismissKeyguard(this, null); // optionally dismiss keyguard (lock screen)
            }
        } else {
            // For older versions, fall back to deprecated flags:
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            );
        }

        Button dismissButton = findViewById(R.id.dismissButton);
        dismissButton.setOnClickListener(view -> {
            stopService(new Intent(this, AlarmForegroundService.class));
            finish(); // close alarm screen
        });

    }// onCreate ends here ============================








}// AlarmAcrivity ends here ===========================