package com.example.agritrack.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agritrack.Activities.Animaux.AnimalCategoryActivity;
import com.example.agritrack.Activities.Animaux.FeedingDashboardActivity;
import com.example.agritrack.Activities.Sensor.StepCounterActivity;
import com.example.agritrack.R;
import com.example.agritrack.Models.DashboardModule;
import com.example.agritrack.Utils.ModuleCardHelper;
import com.example.agritrack.Utils.StorageHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Affiche les différents modules disponibles sous forme de cartes
 */
public class AccueilActivity extends AppCompatActivity {

    private StorageHelper storageHelper;
    private BottomNavigationView bottomNavigationView;
    private TextView stepBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        // Initialiser StorageHelper
        storageHelper = new StorageHelper(this);

        // Vérifier si l'utilisateur est connecté
        if (!storageHelper.isUserLoggedIn()) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialiser les composants
        initializeComponents();
    }

    private void initializeComponents() {
        updateWelcomeText();
        setupBottomNavigation();
        setupModuleCards();
        setupStepCounterButton();
    }

    /**
     * Configure le bouton du compteur de pas
     */
    private void setupStepCounterButton() {
        LinearLayout btnStepCounter = findViewById(R.id.btnStepCounter);        stepBadge = findViewById(R.id.stepBadge);

        // Ajouter le click listener
        btnStepCounter.setOnClickListener(v -> {
            Intent intent = new Intent(AccueilActivity.this, StepCounterActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Optionnel: Mettre à jour le badge avec le nombre de pas
        updateStepBadge();
    }

    /**
     * Met à jour le badge avec le nombre de pas
     */
    private void updateStepBadge() {
        // Ici, vous pouvez récupérer le nombre de pas depuis votre base de données
        // ou depuis SharedPreferences
        // Pour l'instant, on va simplement cacher le badge
        stepBadge.setVisibility(View.GONE);

        // Si vous avez une méthode dans StorageHelper pour récupérer les pas:
        // int stepCount = storageHelper.getStepCount();
        // if (stepCount > 0) {
        //     stepBadge.setVisibility(View.VISIBLE);
        //     stepBadge.setText(String.valueOf(stepCount));
        // } else {
        //     stepBadge.setVisibility(View.GONE);
        // }
    }

    /**
     * Met à jour le texte de bienvenue avec les infos de l'utilisateur
     */
    private void updateWelcomeText() {
        TextView welcomeText = findViewById(R.id.welcomeText);
        String userName = storageHelper.getUserName();
        String farmName = storageHelper.getFarmName();
        welcomeText.setText("Bienvenue, " + userName + "!\n" + farmName);
    }

    /**
     * Configure la barre de navigation inférieure
     */
    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // Déjà sur la page d'accueil
                    return true;

                } else if (itemId == R.id.nav_weather) { // AJOUT MÉTÉO
                    // Ouvrir l'activité météo
                    startActivity(new Intent(AccueilActivity.this, WeatherActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;

                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(AccueilActivity.this, NotificationsActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;

                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(AccueilActivity.this, ProfileActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Configure toutes les cartes de modules
     */
    private void setupModuleCards() {
        // Mapper explicitement les cartes visibles dans activity_accueil.xml
        ModuleCardHelper.setupModuleCard(
                this,
                findViewById(R.id.card_land),
                new DashboardModule("🌾", "Terrain", "Gestion des terrains", "#F57F17", TerrainListActivity.class)
        );

        ModuleCardHelper.setupModuleCard(
                this,
                findViewById(R.id.card_animals),
                new DashboardModule("🐄", "Animaux", "Gestion du bétail", "#1F5C2E", AnimalCategoryActivity.class)
        );

        ModuleCardHelper.setupModuleCard(
                this,
                findViewById(R.id.card_plants),
                new DashboardModule("🌱", "Cultures", "Suivi des récoltes", "#F57F17", PlaceholderActivity.class)
        );

        ModuleCardHelper.setupModuleCard(
                this,
                findViewById(R.id.card_food),
                new DashboardModule("🍽️", "Alimentation", "Nourriture animaux", "#F57F17", FeedingDashboardActivity.class)
        );

        ModuleCardHelper.setupModuleCard(
                this,
                findViewById(R.id.card_irrigation),
                new DashboardModule("💧", "Irrigation", "Gestion de l'irrigation", "#0277BD", PlaceholderActivity.class)
        );

        ModuleCardHelper.setupModuleCard(
                this,
                findViewById(R.id.card_medicines),
                new DashboardModule("💊", "Médicaments", "Soins & vaccins", "#C62828", PlaceholderActivity.class)
        );

        ModuleCardHelper.setupModuleCard(
                this,
                findViewById(R.id.card_equipment),
                new DashboardModule("🚜", "Matériel", "Outils & équipements", "#0277BD", EquipmentListActivity.class)
        );

        ModuleCardHelper.setupModuleCard(
                this,
                findViewById(R.id.card_finance),
                new DashboardModule("💰", "Finances", "Dépenses & revenus", "#6A1B9A", PlaceholderActivity.class)
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mettre à jour l'affichage quand on revient sur l'activité
        if (storageHelper != null && storageHelper.isUserLoggedIn()) {
            updateWelcomeText();
            updateStepBadge(); // Mettre à jour le badge
        }
        // Remettre la sélection sur "Accueil" quand on revient
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
}