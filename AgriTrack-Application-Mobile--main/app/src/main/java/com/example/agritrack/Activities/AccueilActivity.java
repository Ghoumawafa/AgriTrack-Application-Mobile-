package com.example.agritrack.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agritrack.R;
import com.example.agritrack.Models.DashboardModule;
import com.example.agritrack.Utils.ModuleCardHelper;
import com.example.agritrack.Utils.StorageHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class AccueilActivity extends AppCompatActivity {

    private StorageHelper storageHelper;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        storageHelper = new StorageHelper(this);

        // 1. Security Check
        if (!storageHelper.isUserLoggedIn()) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 2. Initialize UI
        initializeComponents();
    }

    private void initializeComponents() {
        updateWelcomeText();
        setupBottomNavigation();
        setupModuleCards();
    }

    private void updateWelcomeText() {
        TextView welcomeText = findViewById(R.id.welcomeText);
        String userName = storageHelper.getUserName();
        String farmName = storageHelper.getFarmName();
        welcomeText.setText("Bienvenue, " + userName + "!\n" + farmName);
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;

            if (itemId == R.id.nav_notifications) {
                startActivity(new Intent(AccueilActivity.this, NotificationsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(AccueilActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupModuleCards() {
        // --- Setup Standard Modules ---
        DashboardModule[] modules = {
                new DashboardModule("🐄", "Animaux", "Gérer le bétail", "#795548", PlaceholderActivity.class),
                new DashboardModule("🌱", "Cultures", "Suivre les récoltes", "#2E7D32", PlaceholderActivity.class),
                new DashboardModule("💊", "Médicaments", "Soins & vaccins", "#C2185B", PlaceholderActivity.class),
                new DashboardModule("🚜", "Matériel", "Outils & équipements", "#1565C0", PlaceholderActivity.class),
                new DashboardModule("💰", "Finances", "Dépenses & revenus", "#9C27B0", PlaceholderActivity.class),
                new DashboardModule("🍽️", "Alimentation", "Nourriture animaux", "#FF9800", PlaceholderActivity.class)
        };

        int[] cardIds = {
                R.id.card_animals, R.id.card_plants, R.id.card_medicines,
                R.id.card_equipment, R.id.card_finance, R.id.card_food
        };

        for (int i = 0; i < modules.length && i < cardIds.length; i++) {
            CardView card = findViewById(cardIds[i]);
            if (card != null) {
                ModuleCardHelper.setupModuleCard(this, card, modules[i]);
            }
        }

        // --- Setup Irrigation Module (The one we just completed) ---
        CardView irrigationCard = findViewById(R.id.card_irrigation);
        if (irrigationCard != null) {
            irrigationCard.setOnClickListener(v -> {
                startActivity(new Intent(AccueilActivity.this, IrrigationActivity.class));
            });
        }

        // --- Setup Plant Module ---
        CardView plantCard = findViewById(R.id.card_plants);
        if (plantCard != null) {
            plantCard.setOnClickListener(v -> {
                startActivity(new Intent(AccueilActivity.this, PlantActivity.class));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (storageHelper != null && storageHelper.isUserLoggedIn()) {
            updateWelcomeText();
        }
    }
}