package com.example.agritrack.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

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

    // Camera launcher
    private ActivityResultLauncher<Void> takePicturePreviewLauncher;

    // Permission launcher
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;

    private DiseaseDetector detector; // keep one instance per Activity

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

        detector = new DiseaseDetector(this);

        setupBottomNavigation();
        setupPlantSpinner();
        setupCameraAndPermissions();

        btnAnalyze.setEnabled(false);
        btnAnalyze.setOnClickListener(v -> runAnalysis());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (detector != null) detector.close();
    }

    private void setupPlantSpinner() {
        List<PlantEntity> plants = plantDao.getAllPlants();
        List<String> names = new ArrayList<>();
        for (PlantEntity p : plants) {
            names.add(p.getId() + " - " + (p.getName() == null ? "Plante" : p.getName()));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                names
        );
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

    private void setupCameraAndPermissions() {
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        takePicturePreviewLauncher.launch(null);
                    } else {
                        Toast.makeText(this,
                                "Permission caméra requise pour prendre une photo",
                                Toast.LENGTH_LONG).show();
                    }
                });

        takePicturePreviewLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        lastBitmap = bitmap;
                        imagePreview.setImageBitmap(bitmap);
                        btnAnalyze.setEnabled(true);
                        cardResults.setVisibility(View.GONE);
                    }
                });

        btnCapture.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION)
                    == PackageManager.PERMISSION_GRANTED) {
                takePicturePreviewLauncher.launch(null);
            } else {
                requestCameraPermissionLauncher.launch(CAMERA_PERMISSION);
            }
        });
    }

    private void runAnalysis() {
        if (lastBitmap == null) {
            Toast.makeText(this, "Veuillez capturer une image d'abord", Toast.LENGTH_SHORT).show();
            return;
        }

        // UI state
        btnAnalyze.setEnabled(false);
        btnAnalyze.setText("Analyse...");
        cardResults.setVisibility(View.VISIBLE);

        txtDisease.setText("Maladie: Analyse en cours...");
        txtConfidence.setText("Confiance: ...");
        txtSeverity.setText("Sévérité: ...");
        txtRecommendation.setText("Recommandation: ...");

        detector.detectDiseaseAsync(lastBitmap, result -> {
            // Switch back to UI thread
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;

                txtDisease.setText("Maladie: " + result.diseaseName);
                txtConfidence.setText("Confiance: " + Math.round(result.confidence * 100) + "%");
                txtSeverity.setText("Sévérité: " + result.severity);
                txtRecommendation.setText("Recommandation: " + result.recommendation);

                btnAnalyze.setEnabled(true);
                btnAnalyze.setText("Analyser");
            });
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(PlantTreatmentActivity.this, AccueilActivity.class));
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(PlantTreatmentActivity.this, NotificationsActivity.class));
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(PlantTreatmentActivity.this, ProfileActivity.class));
                } else {
                    return false;
                }

                overridePendingTransition(0, 0);
                return true;
            }
        });
    }
}