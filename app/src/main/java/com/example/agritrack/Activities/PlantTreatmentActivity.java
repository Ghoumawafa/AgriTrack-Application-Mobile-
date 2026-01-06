package com.example.agritrack.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.PlantDao;
import com.example.agritrack.Database.PlantEntity;
import com.example.agritrack.R;
import com.example.agritrack.Utils.DiseaseDetector;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class PlantTreatmentActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private PlantDao plantDao;

    private Spinner spinnerPlant;
    private ImageView imagePreview;
    private Button btnCapture;
    private Button btnAnalyze;

    private View cardResults;
    private TextView txtDisease;
    private TextView txtConfidence;
    private TextView txtSeverity;
    private TextView txtRecommendation;

    private Bitmap lastBitmap;

    private ActivityResultLauncher<Void> takePicturePreviewLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_treatment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        plantDao = AgriTrackRoomDatabase.getInstance(this).plantDao();

        spinnerPlant = findViewById(R.id.spinnerPlant);
        imagePreview = findViewById(R.id.imagePreview);
        btnCapture = findViewById(R.id.btnCapture);
        btnAnalyze = findViewById(R.id.btnAnalyze);

        cardResults = findViewById(R.id.cardResults);
        txtDisease = findViewById(R.id.txtDisease);
        txtConfidence = findViewById(R.id.txtConfidence);
        txtSeverity = findViewById(R.id.txtSeverity);
        txtRecommendation = findViewById(R.id.txtRecommendation);

        setupBottomNavigation();
        setupPlantSpinner();
        setupCamera();

        btnAnalyze.setEnabled(false);
        btnAnalyze.setOnClickListener(v -> runAnalysis());
    }

    private void setupPlantSpinner() {
        List<PlantEntity> plants = plantDao.getAllPlants();
        List<String> names = new ArrayList<>();
        for (PlantEntity p : plants) {
            names.add(p.getId() + " - " + (p.getName() == null ? "Plante" : p.getName()));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinnerPlant.setAdapter(adapter);

        int requestedId = getIntent().getIntExtra("plant_id", -1);
        if (requestedId != -1) {
            for (int i = 0; i < plants.size(); i++) {
                if (plants.get(i).getId() == requestedId) {
                    spinnerPlant.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupCamera() {
        takePicturePreviewLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        lastBitmap = bitmap;
                        imagePreview.setImageBitmap(bitmap);
                        btnAnalyze.setEnabled(true);
                        cardResults.setVisibility(View.GONE);
                    }
                }
        );

        btnCapture.setOnClickListener(v -> takePicturePreviewLauncher.launch(null));
    }

    private void runAnalysis() {
        if (lastBitmap == null) return;

        DiseaseDetector detector = new DiseaseDetector(this);
        DiseaseDetector.DetectionResult result = detector.detectDisease(lastBitmap);

        txtDisease.setText("Maladie: " + result.diseaseName);
        txtConfidence.setText("Confiance: " + Math.round(result.confidence * 100) + "%");
        txtSeverity.setText("Sévérité: " + result.severity);
        txtRecommendation.setText("Recommandation: " + result.recommendation);

        cardResults.setVisibility(View.VISIBLE);
        detector.close();
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(PlantTreatmentActivity.this, AccueilActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    Intent intent = new Intent(PlantTreatmentActivity.this, NotificationsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(PlantTreatmentActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }
}
