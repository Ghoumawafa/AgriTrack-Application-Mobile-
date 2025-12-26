package com.example.agritrack.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agritrack.Activities.LoginActivity;
import com.example.agritrack.Activities.NotificationsActivity;
import com.example.agritrack.Activities.ProfileActivity;
import com.example.agritrack.R;
import com.example.agritrack.Models.DashboardModule;
import com.example.agritrack.Utils.ModuleCardHelper;
import com.example.agritrack.Utils.StorageHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Activité principale (Accueil) de l'application AgriTrack
 * Affiche les différents modules disponibles sous forme de cartes
 */
public class AccueilActivity extends AppCompatActivity {

    private StorageHelper storageHelper;
    private BottomNavigationView bottomNavigationView;

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

    /**
     * Initialise tous les composants de l'interface
     */
    private void initializeComponents() {
        updateWelcomeText();
        setupBottomNavigation();
        setupModuleCards();

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
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(AccueilActivity.this, NotificationsActivity.class));
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(AccueilActivity.this, ProfileActivity.class));
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
        // Créer les modules avec les activités correspondantes
        // Note: Remplacez PlaceholderActivity par les vraies activités quand elles seront créées
        DashboardModule[] modules = {
                new DashboardModule("🐄", "Animaux", "Gérer le bétail", "#1F5C2E", PlaceholderActivity.class),
                new DashboardModule("🌾", "Cultures", "Suivre les récoltes", "#F57F17", PlaceholderActivity.class),
                new DashboardModule("💊", "Médicaments", "Soins & vaccins", "#C62828", PlaceholderActivity.class),
                new DashboardModule("🚜", "Matériel", "Outils & équipements", "#0277BD", PlaceholderActivity.class),
                new DashboardModule("💰", "Finances", "Dépenses & revenus", "#6A1B9A", PlaceholderActivity.class),
                new DashboardModule("📅", "Calendrier", "Planifier les tâches", "#E65100", PlaceholderActivity.class)
        };

        // IDs des cartes dans le layout
        int[] cardIds = {
                R.id.card_animals,
                R.id.card_plants,
                R.id.card_medicines,
                R.id.card_equipment,
                R.id.card_finance,

        };

        // Configurer chaque carte avec son module
        for (int i = 0; i < modules.length && i < cardIds.length; i++) {
            ModuleCardHelper.setupModuleCard(this, findViewById(cardIds[i]), modules[i]);
        }
    }





    @Override
    protected void onResume() {
        super.onResume();
        // Mettre à jour l'affichage quand on revient sur l'activité
        if (storageHelper != null && storageHelper.isUserLoggedIn()) {
            updateWelcomeText();
        }
    }
}