package com.example.agritrack.Activities.Animaux;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalFeedingScheduleDao;
import com.example.agritrack.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedingStatisticsActivity extends AppCompatActivity {

    private CustomPieChartView chart;
    private Spinner spinnerChartType;
    private TextView tvChartTitle;
    private TextView tvEmptyState;

    private AgriTrackRoomDatabase database;
    private AnimalDao animalDao;
    private AnimalFeedingScheduleDao scheduleDao;

    // Types de graphiques disponibles
    private static final String[] CHART_TYPES = {
            "🐾 Animaux par espèce",
            "📋 Statut des repas",
            "🍽️ Repas par espèce"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeding_statistics);

        database = AgriTrackRoomDatabase.getInstance(this);
        animalDao = database.animalDao();
        scheduleDao = database.animalFeedingScheduleDao();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Initialiser les vues
        chart = findViewById(R.id.chart);
        spinnerChartType = findViewById(R.id.spinnerChartType);
        tvChartTitle = findViewById(R.id.tvChartTitle);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        setupSpinner();

        // Charger le premier graphique par défaut
        loadChartData(0);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                CHART_TYPES
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChartType.setAdapter(adapter);

        spinnerChartType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Mettre à jour le titre
                tvChartTitle.setText(CHART_TYPES[position]);
                // Charger les données
                loadChartData(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ne rien faire
            }
        });
    }

    private void loadChartData(int chartTypeIndex) {
        new Thread(() -> {
            try {
                List<CustomPieChartView.PieSlice> slices = new ArrayList<>();

                switch (chartTypeIndex) {
                    case 0: // Animaux par espèce
                        slices = getAnimalStatistics();
                        break;
                    case 1: // Statut des repas
                        slices = getMealStatusStatistics();
                        break;
                    case 2: // Repas par espèce
                        slices = getMealsBySpeciesStatistics();
                        break;
                }

                final List<CustomPieChartView.PieSlice> finalSlices = slices;

                runOnUiThread(() -> {
                    if (finalSlices.isEmpty()) {
                        chart.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Aucune donnée disponible\npour ce type de graphique");
                    } else {
                        chart.setVisibility(View.VISIBLE);
                        tvEmptyState.setVisibility(View.GONE);
                        chart.setData(finalSlices);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(FeedingStatisticsActivity.this,
                            "Erreur lors du chargement: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private List<CustomPieChartView.PieSlice> getAnimalStatistics() {
        List<CustomPieChartView.PieSlice> slices = new ArrayList<>();

        try {
            int vaches = animalDao.getCountBySpecies("Vache");
            int moutons = animalDao.getCountBySpecies("Mouton");
            int chevres = animalDao.getCountBySpecies("Chèvre");
            int poules = animalDao.getCountBySpecies("Poule");

            if (vaches > 0) {
                slices.add(new CustomPieChartView.PieSlice(
                        "Vaches (" + vaches + ")",
                        vaches,
                        Color.parseColor("#FF6B6B")
                ));
            }
            if (moutons > 0) {
                slices.add(new CustomPieChartView.PieSlice(
                        "Moutons (" + moutons + ")",
                        moutons,
                        Color.parseColor("#4ECDC4")
                ));
            }
            if (chevres > 0) {
                slices.add(new CustomPieChartView.PieSlice(
                        "Chèvres (" + chevres + ")",
                        chevres,
                        Color.parseColor("#95E1D3")
                ));
            }
            if (poules > 0) {
                slices.add(new CustomPieChartView.PieSlice(
                        "Poules (" + poules + ")",
                        poules,
                        Color.parseColor("#F38181")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return slices;
    }

    private List<CustomPieChartView.PieSlice> getMealStatusStatistics() {
        List<CustomPieChartView.PieSlice> slices = new ArrayList<>();

        try {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            int pending = scheduleDao.getPendingFeedingsForDate(today).size();
            int completed = scheduleDao.getCompletedFeedingsForDate(today).size();
            int skipped = scheduleDao.getSkippedFeedingsForDate(today).size();

            if (pending > 0) {
                slices.add(new CustomPieChartView.PieSlice(
                        "En attente (" + pending + ")",
                        pending,
                        Color.parseColor("#FF9800")
                ));
            }
            if (completed > 0) {
                slices.add(new CustomPieChartView.PieSlice(
                        "Complétés (" + completed + ")",
                        completed,
                        Color.parseColor("#4CAF50")
                ));
            }
            if (skipped > 0) {
                slices.add(new CustomPieChartView.PieSlice(
                        "Sautés (" + skipped + ")",
                        skipped,
                        Color.parseColor("#F44336")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return slices;
    }

    private List<CustomPieChartView.PieSlice> getMealsBySpeciesStatistics() {
        List<CustomPieChartView.PieSlice> slices = new ArrayList<>();

        try {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            int vacheMeals = scheduleDao.getMealCountForSpecies("Vache", today);
            int moutonMeals = scheduleDao.getMealCountForSpecies("Mouton", today);
            int chevreMeals = scheduleDao.getMealCountForSpecies("Chèvre", today);
            int pouleMeals = scheduleDao.getMealCountForSpecies("Poule", today);

            if (vacheMeals > 0) {
                slices.add(new CustomPieChartView.PieSlice(
                        "Vaches (" + vacheMeals + ")",
                        vacheMeals,
                        Color.parseColor("#2196F3")
                ));
            }
            if (moutonMeals > 0) {
                slices.add(new CustomPieChartView.PieSlice(
                        "Moutons (" + moutonMeals + ")",
                        moutonMeals,
                        Color.parseColor("#9C27B0")
                ));
            }
            if (chevreMeals > 0) {
                slices.add(new CustomPieChartView.PieSlice(
                        "Chèvres (" + chevreMeals + ")",
                        chevreMeals,
                        Color.parseColor("#FF5722")
                ));
            }
            if (pouleMeals > 0) {
                slices.add(new CustomPieChartView.PieSlice(
                        "Poules (" + pouleMeals + ")",
                        pouleMeals,
                        Color.parseColor("#FFC107")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return slices;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les données si nécessaire
        int selectedPosition = spinnerChartType.getSelectedItemPosition();
        if (selectedPosition >= 0) {
            loadChartData(selectedPosition);
        }
    }
}