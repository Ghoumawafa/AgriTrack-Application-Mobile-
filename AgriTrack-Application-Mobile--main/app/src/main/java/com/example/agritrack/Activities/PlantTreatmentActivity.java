package com.example.agritrack.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Adapters.PlantTreatmentAdapter;
import com.example.agritrack.Database.AppDatabase;
import com.example.agritrack.Models.Plant;
import com.example.agritrack.Models.PlantTreatment;
import com.example.agritrack.R;
import com.example.agritrack.Utils.DiseaseDetector;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlantTreatmentActivity extends AppCompatActivity {
    private static final String TAG = "PlantTreatmentActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Spinner spinnerPlant;
    private ImageView imagePreview;
    private Button btnCapture, btnAnalyze;
    private CardView cardResults;
    private TextView txtDisease, txtConfidence, txtSeverity, txtRecommendation;
    private RecyclerView recyclerViewTreatments;
    private PlantTreatmentAdapter adapter;

    private List<Plant> plantList = new ArrayList<>();
    private List<PlantTreatment> treatmentList = new ArrayList<>();
    private Plant selectedPlant;
    private String currentPhotoPath;
    private DiseaseDetector diseaseDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_treatment);

        try {
            db = AppDatabase.getInstance(this);
            diseaseDetector = new DiseaseDetector(this);

            // Toolbar setup
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Détection Maladies");
                }
                toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
            }

            // Initialize views
            spinnerPlant = findViewById(R.id.spinnerPlant);
            imagePreview = findViewById(R.id.imagePreview);
            btnCapture = findViewById(R.id.btnCapture);
            btnAnalyze = findViewById(R.id.btnAnalyze);
            cardResults = findViewById(R.id.cardResults);
            txtDisease = findViewById(R.id.txtDisease);
            txtConfidence = findViewById(R.id.txtConfidence);
            txtSeverity = findViewById(R.id.txtSeverity);
            txtRecommendation = findViewById(R.id.txtRecommendation);
            recyclerViewTreatments = findViewById(R.id.recyclerViewTreatments);

            // RecyclerView setup
            recyclerViewTreatments.setLayoutManager(new LinearLayoutManager(this));
            adapter = new PlantTreatmentAdapter(this, treatmentList, new PlantTreatmentAdapter.OnItemActionListener() {
                @Override
                public void onEdit(PlantTreatment treatment) {
                    showEditTreatmentDialog(treatment);
                }

                @Override
                public void onDelete(PlantTreatment treatment) {
                    showDeleteConfirmation(treatment);
                }
            });
            recyclerViewTreatments.setAdapter(adapter);

            // Button listeners
            btnCapture.setOnClickListener(v -> checkCameraPermissionAndCapture());
            btnAnalyze.setOnClickListener(v -> analyzeImage());

            // Bottom navigation
            setupBottomNavigation();

            // Load plants and setup spinner
            loadPlants();

            // Check if plantId was passed from PlantActivity
            int plantId = getIntent().getIntExtra("plantId", -1);
            if (plantId != -1) {
                // Will select this plant after loading
                loadPlants(plantId);
            }

        } catch (Exception ex) {
            Log.e(TAG, "onCreate failed", ex);
            Toast.makeText(this, "Erreur interne", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                try {
                    if (id == R.id.nav_home) {
                        startActivity(new Intent(PlantTreatmentActivity.this, AccueilActivity.class));
                        return true;
                    } else if (id == R.id.nav_notifications) {
                        startActivity(new Intent(PlantTreatmentActivity.this, NotificationsActivity.class));
                        return true;
                    } else if (id == R.id.nav_profile) {
                        startActivity(new Intent(PlantTreatmentActivity.this, ProfileActivity.class));
                        return true;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Navigation failed", e);
                }
                return false;
            });
        }
    }

    private void loadPlants() {
        loadPlants(-1);
    }

    private void loadPlants(int selectPlantId) {
        executor.execute(() -> {
            try {
                List<Plant> plants = db.plantDao().getAllPlants();
                runOnUiThread(() -> {
                    plantList.clear();
                    if (plants != null) {
                        plantList.addAll(plants);
                    }
                    setupPlantSpinner(selectPlantId);
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to load plants", e);
            }
        });
    }

    private void setupPlantSpinner(int selectPlantId) {
        List<String> plantNames = new ArrayList<>();
        plantNames.add("Sélectionner une plante");
        for (Plant plant : plantList) {
            plantNames.add(plant.getName());
        }

        ArrayAdapter<String> plantAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, plantNames);
        plantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlant.setAdapter(plantAdapter);

        spinnerPlant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedPlant = plantList.get(position - 1);
                    loadTreatments();
                } else {
                    selectedPlant = null;
                    treatmentList.clear();
                    if (adapter != null) adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Select specific plant if requested
        if (selectPlantId != -1) {
            for (int i = 0; i < plantList.size(); i++) {
                if (plantList.get(i).getId() == selectPlantId) {
                    spinnerPlant.setSelection(i + 1);
                    break;
                }
            }
        }
    }

    private void loadTreatments() {
        if (selectedPlant == null) return;

        executor.execute(() -> {
            try {
                List<PlantTreatment> treatments = db.plantTreatmentDao().getByPlantId(selectedPlant.getId());
                runOnUiThread(() -> {
                    treatmentList.clear();
                    if (treatments != null) {
                        treatmentList.addAll(treatments);
                    }
                    if (adapter != null) adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to load treatments", e);
            }
        });
    }

    private void checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permission caméra requise", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
                Toast.makeText(this, "Erreur lors de la création du fichier", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.agritrack.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "Aucune application caméra disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "PLANT_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (currentPhotoPath != null) {
                File imgFile = new File(currentPhotoPath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imagePreview.setImageBitmap(bitmap);
                    btnAnalyze.setEnabled(true);
                    cardResults.setVisibility(View.GONE);
                }
            }
        }
    }

    private void analyzeImage() {
        if (selectedPlant == null) {
            Toast.makeText(this, "Veuillez sélectionner une plante", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPhotoPath == null) {
            Toast.makeText(this, "Veuillez capturer une image", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAnalyze.setEnabled(false);
        btnAnalyze.setText("Analyse en cours...");

        executor.execute(() -> {
            try {
                // AI Disease Detection
                DiseaseDetector.DetectionResult result = diseaseDetector.detectDisease(currentPhotoPath);

                runOnUiThread(() -> {
                    displayResults(result);
                    saveTreatment(result);
                    btnAnalyze.setEnabled(true);
                    btnAnalyze.setText("🔍 Analyser avec IA");
                });
            } catch (Exception e) {
                Log.e(TAG, "AI analysis failed", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur lors de l'analyse", Toast.LENGTH_SHORT).show();
                    btnAnalyze.setEnabled(true);
                    btnAnalyze.setText("🔍 Analyser avec IA");
                });
            }
        });
    }

    private void displayResults(DiseaseDetector.DetectionResult result) {
        cardResults.setVisibility(View.VISIBLE);
        txtDisease.setText("Maladie: " + result.diseaseName);
        txtConfidence.setText(String.format(Locale.getDefault(), "Confiance: %.0f%%", result.confidence * 100));
        txtSeverity.setText("Sévérité: " + result.severity);
        txtRecommendation.setText("Recommandation: " + result.recommendation);
    }

    private void saveTreatment(DiseaseDetector.DetectionResult result) {
        if (selectedPlant == null) return;

        PlantTreatment treatment = new PlantTreatment(
                selectedPlant.getId(),
                result.diseaseName,
                result.confidence,
                currentPhotoPath
        );
        treatment.setSeverity(result.severity);
        treatment.setRecommendedAction(result.recommendation);

        executor.execute(() -> {
            try {
                db.plantTreatmentDao().insert(treatment);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Traitement enregistré", Toast.LENGTH_SHORT).show();
                    loadTreatments();
                    currentPhotoPath = null;
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to save treatment", e);
            }
        });
    }

    private void showEditTreatmentDialog(PlantTreatment treatment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifier Statut");

        String[] statuses = {"Détecté", "En traitement", "Traité"};
        int currentIndex = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(treatment.getStatus())) {
                currentIndex = i;
                break;
            }
        }

        builder.setSingleChoiceItems(statuses, currentIndex, (dialog, which) -> {
            treatment.setStatus(statuses[which]);
            executor.execute(() -> {
                try {
                    db.plantTreatmentDao().update(treatment);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Statut mis à jour", Toast.LENGTH_SHORT).show();
                        loadTreatments();
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Failed to update treatment", e);
                }
            });
            dialog.dismiss();
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showDeleteConfirmation(PlantTreatment treatment) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer Traitement")
                .setMessage("Êtes-vous sûr de vouloir supprimer ce traitement ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteTreatment(treatment))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteTreatment(PlantTreatment treatment) {
        executor.execute(() -> {
            try {
                db.plantTreatmentDao().delete(treatment);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Traitement supprimé", Toast.LENGTH_SHORT).show();
                    loadTreatments();
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete treatment", e);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        if (diseaseDetector != null) {
            diseaseDetector.close();
        }
    }
}

