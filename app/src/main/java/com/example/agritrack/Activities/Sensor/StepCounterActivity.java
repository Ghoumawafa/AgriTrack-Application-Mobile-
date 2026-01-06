package com.example.agritrack.Activities.Sensor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.example.agritrack.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 100;

    // UI Elements
    private TextView tvStepCount, tvDistance, tvCalories, tvGoalProgress, tvLastUpdate;
    private ProgressBar progressBarSteps;
    private ImageButton btnBack, btnReset;

    // Sensor
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private Sensor stepDetectorSensor;

    // Data
    private int totalSteps = 0;
    private int previousTotalSteps = 0;
    private int dailySteps = 0;
    private int dailyGoal = 10000; // Objectif par défaut: 10,000 pas

    // Constants pour calculs
    private static final float STEP_LENGTH_M = 0.762f; // Longueur moyenne d'un pas en mètres
    private static final float CALORIES_PER_STEP = 0.04f; // Calories par pas (moyenne)

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "StepCounterPrefs";
    private static final String KEY_PREVIOUS_STEPS = "previousSteps";
    private static final String KEY_LAST_DATE = "lastDate";
    private static final String KEY_DAILY_GOAL = "dailyGoal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        // Initialiser les vues
        initViews();

        // Initialiser SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Charger les données sauvegardées
        loadData();

        // Vérifier si c'est un nouveau jour
        checkNewDay();

        // Initialiser le capteur
        initSensor();

        // Demander les permissions si nécessaire (Android 10+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        ACTIVITY_RECOGNITION_REQUEST_CODE);
            }
        }

        // Listeners des boutons
        btnBack.setOnClickListener(v -> finish());
        btnReset.setOnClickListener(v -> resetDailySteps());

        // Mettre à jour l'affichage
        updateUI();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnReset = findViewById(R.id.btnReset);
        tvStepCount = findViewById(R.id.tvStepCount);
        tvDistance = findViewById(R.id.tvDistance);
        tvCalories = findViewById(R.id.tvCalories);
        tvGoalProgress = findViewById(R.id.tvGoalProgress);
        tvLastUpdate = findViewById(R.id.tvLastUpdate);
        progressBarSteps = findViewById(R.id.progressBarSteps);
        progressBarSteps.setMax(dailyGoal);
    }

    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Essayer d'abord le Step Counter (plus précis)
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCounterSensor == null) {
            // Si pas disponible, utiliser Step Detector
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

            if (stepDetectorSensor == null) {
                Toast.makeText(this, "❌ Capteur de pas non disponible sur cet appareil",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "✅ Utilisation du Step Detector",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "✅ Capteur de pas initialisé",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadData() {
        previousTotalSteps = sharedPreferences.getInt(KEY_PREVIOUS_STEPS, 0);
        dailyGoal = sharedPreferences.getInt(KEY_DAILY_GOAL, 10000);
        progressBarSteps.setMax(dailyGoal);
    }

    private void checkNewDay() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastDate = sharedPreferences.getString(KEY_LAST_DATE, "");

        if (!today.equals(lastDate)) {
            // Nouveau jour, réinitialiser les pas quotidiens
            previousTotalSteps = totalSteps;
            saveData();

            // Sauvegarder la nouvelle date
            sharedPreferences.edit().putString(KEY_LAST_DATE, today).apply();
        }
    }

    private void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_PREVIOUS_STEPS, previousTotalSteps);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Enregistrer les listeners des capteurs
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor,
                    SensorManager.SENSOR_DELAY_UI);
        } else if (stepDetectorSensor != null) {
            sensorManager.registerListener(this, stepDetectorSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Désenregistrer les listeners pour économiser la batterie
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        // Sauvegarder les données
        saveData();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // Step Counter donne le nombre total de pas depuis le redémarrage
            totalSteps = (int) event.values[0];
            dailySteps = totalSteps - previousTotalSteps;

        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // Step Detector détecte chaque pas individuellement
            dailySteps++;
        }

        // Mettre à jour l'interface
        updateUI();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Pas besoin d'action ici
    }

    private void updateUI() {
        // Mettre à jour le compteur de pas
        tvStepCount.setText(String.valueOf(dailySteps));

        // Calculer et afficher la distance (en km)
        float distanceKm = (dailySteps * STEP_LENGTH_M) / 1000;
        DecimalFormat df = new DecimalFormat("#.##");
        tvDistance.setText(df.format(distanceKm) + " km");

        // Calculer et afficher les calories
        int calories = (int) (dailySteps * CALORIES_PER_STEP);
        tvCalories.setText(String.valueOf(calories) + " kcal");

        // Mettre à jour la barre de progression
        progressBarSteps.setProgress(dailySteps);

        // Afficher le pourcentage de l'objectif
        int progressPercent = (dailySteps * 100) / dailyGoal;
        tvGoalProgress.setText(progressPercent + "% de l'objectif");

        // Message de motivation
        if (dailySteps >= dailyGoal) {
            tvGoalProgress.setText("🎉 Objectif atteint ! " + progressPercent + "%");
        }

        // Heure de dernière mise à jour
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(new Date());
        tvLastUpdate.setText("Dernière mise à jour : " + currentTime);
    }

    private void resetDailySteps() {
        // Réinitialiser les compteurs
        previousTotalSteps = totalSteps;
        dailySteps = 0;

        // Sauvegarder
        saveData();

        // Mettre à jour l'affichage
        updateUI();

        Toast.makeText(this, "✅ Compteur réinitialisé", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Permission accordée", Toast.LENGTH_SHORT).show();
                initSensor();
            } else {
                Toast.makeText(this, "❌ Permission refusée - Le compteur ne fonctionnera pas",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}