package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import com.example.agritrack.R;
import com.example.agritrack.Utils.StorageHelper;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        StorageHelper storageHelper = new StorageHelper(this);

        // Use Handler with explicit Looper to avoid memory leak
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (!isFinishing()) {
                if (storageHelper.isUserLoggedIn()) {
                    startActivity(new Intent(SplashActivity.this, AccueilActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }
        }, SPLASH_DELAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to prevent memory leak
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}