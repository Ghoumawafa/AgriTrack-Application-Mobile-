package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import com.example.agritrack.R;
import com.example.agritrack.Utils.StorageHelper;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        StorageHelper storageHelper = new StorageHelper(this);

        new Handler().postDelayed(() -> {
            if (storageHelper.isUserLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, com.example.agritrack.Activities.AccueilActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }
}