package com.example.agritrack.Activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.example.agritrack.R;

/**
 * Activité temporaire pour tester la navigation
 * À remplacer par les vraies activités des modules
 */
public class PlaceholderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);

        // Configurer le titre
        TextView titleView = findViewById(R.id.placeholder_title);
        if (titleView != null) {
            titleView.setText("🚧 Module en développement 🚧");
        }

        // Configurer le bouton retour
        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // Retourner à l'écran d'accueil
                finish();
            });
        }

        // Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Retourner à l'accueil au lieu de fermer l'app
                finish();
            }
        });
    }
}