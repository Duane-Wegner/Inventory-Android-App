package com.modfive.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class PermissionActivity extends AppCompatActivity {

    private TextView permissionStatusText;
    private Button requestPermissionBtn;

    // Launcher for the permission request
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_activity);

        permissionStatusText = findViewById(R.id.permissionStatusText);
        requestPermissionBtn = findViewById(R.id.requestPermissionBtn);

        // Setup permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        permissionStatusText.setText("SMS Permission granted! You will receive notifications.");
                        requestPermissionBtn.setEnabled(false);
                        // Enable SMS notifications here if needed
                    } else {
                        permissionStatusText.setText("SMS Permission denied. Notifications will be disabled.");
                        requestPermissionBtn.setEnabled(true);
                        // Disable SMS notifications here if needed
                    }
                }
        );

        checkSmsPermission();

        requestPermissionBtn.setOnClickListener(v -> {
            requestSmsPermission();
        });
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            permissionStatusText.setText("SMS Permission already granted! You will receive notifications.");
            requestPermissionBtn.setEnabled(false);
            // Enable SMS notifications here if needed
        } else {
            permissionStatusText.setText("SMS Permission not granted. Please allow to receive SMS notifications.");
            requestPermissionBtn.setEnabled(true);
        }
    }

    private void requestSmsPermission() {
        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
    }
}
