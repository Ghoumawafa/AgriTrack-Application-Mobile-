package com.example.agritrack.Activities.Animaux;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.Database.AnimalFeedingScheduleDao;
import com.example.agritrack.Database.AnimalFeedingScheduleEntity;
import com.example.agritrack.Database.AnimalFoodPlanDao;
import com.example.agritrack.Database.AnimalFoodPlanEntity;
import com.example.agritrack.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GenerateScheduleActivity extends AppCompatActivity {

    private TextView tvStartDate, tvEndDate, tvSummary;
    private CheckBox cbVache, cbMouton, cbChevre, cbPoule;
    private Button btnGenerate;
    private ProgressBar progressBar;

    private AgriTrackRoomDatabase database;
    private AnimalDao animalDao;
    private AnimalFoodPlanDao planDao;
    private AnimalFeedingScheduleDao scheduleDao;

    private Calendar startCalendar, endCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_schedule);

        database = AgriTrackRoomDatabase.getInstance(this);
        animalDao = database.animalDao();
        planDao = database.animalFoodPlanDao();
        scheduleDao = database.animalFeedingScheduleDao();

        initializeViews();
        initializeDates();
        updateSummary();
    }

    private void initializeViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvSummary = findViewById(R.id.tvSummary);

        cbVache = findViewById(R.id.cbVache);
        cbMouton = findViewById(R.id.cbMouton);
        cbChevre = findViewById(R.id.cbChevre);
        cbPoule = findViewById(R.id.cbPoule);

        btnGenerate = findViewById(R.id.btnGenerate);
        progressBar = findViewById(R.id.progressBar);

        // Cocher toutes les espèces par défaut
        cbVache.setChecked(true);
        cbMouton.setChecked(true);
        cbChevre.setChecked(true);
        cbPoule.setChecked(true);

        // Listeners pour mise à jour du résumé
        cbVache.setOnCheckedChangeListener((buttonView, isChecked) -> updateSummary());
        cbMouton.setOnCheckedChangeListener((buttonView, isChecked) -> updateSummary());
        cbChevre.setOnCheckedChangeListener((buttonView, isChecked) -> updateSummary());
        cbPoule.setOnCheckedChangeListener((buttonView, isChecked) -> updateSummary());

        tvStartDate.setOnClickListener(v -> showStartDatePicker());
        tvEndDate.setOnClickListener(v -> showEndDatePicker());

        btnGenerate.setOnClickListener(v -> generateSchedules());
    }

    private void initializeDates() {
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        endCalendar.add(Calendar.DAY_OF_MONTH, 7); // 7 jours par défaut

        tvStartDate.setText(displayFormat.format(startCalendar.getTime()));
        tvEndDate.setText(displayFormat.format(endCalendar.getTime()));
    }

    private void showStartDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    startCalendar.set(year, month, dayOfMonth);
                    tvStartDate.setText(displayFormat.format(startCalendar.getTime()));

                    // Vérifier que la date de fin est après la date de début
                    if (endCalendar.before(startCalendar)) {
                        endCalendar = (Calendar) startCalendar.clone();
                        endCalendar.add(Calendar.DAY_OF_MONTH, 7);
                        tvEndDate.setText(displayFormat.format(endCalendar.getTime()));
                    }
                    updateSummary();
                },
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    endCalendar.set(year, month, dayOfMonth);

                    // Vérifier que la date de fin est après la date de début
                    if (endCalendar.before(startCalendar)) {
                        Toast.makeText(this, "La date de fin doit être après la date de début", Toast.LENGTH_SHORT).show();
                        endCalendar = (Calendar) startCalendar.clone();
                        endCalendar.add(Calendar.DAY_OF_MONTH, 7);
                    }

                    tvEndDate.setText(displayFormat.format(endCalendar.getTime()));
                    updateSummary();
                },
                endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH)
        );
        picker.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
        picker.show();
    }

    private void updateSummary() {
        new Thread(() -> {
            List<String> selectedSpecies = getSelectedSpecies();
            int totalAnimals = 0;

            for (String species : selectedSpecies) {
                totalAnimals += animalDao.getCountBySpecies(species);
            }

            long daysBetween = (endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis()) / (1000 * 60 * 60 * 24) + 1;
            int estimatedMeals = (int) (totalAnimals * daysBetween * 2.5); // Moyenne 2.5 repas/jour

            int finalTotalAnimals = totalAnimals;
            runOnUiThread(() -> {
                String summary = String.format(Locale.getDefault(),
                        "📊 Résumé:\n\n" +
                                "• %d animaux sélectionnés\n" +
                                "• %d jours de planning\n" +
                                "• ~%d repas à générer",
                        finalTotalAnimals, daysBetween, estimatedMeals);
                tvSummary.setText(summary);
            });
        }).start();
    }

    private List<String> getSelectedSpecies() {
        List<String> species = new ArrayList<>();
        if (cbVache.isChecked()) species.add("Vache");
        if (cbMouton.isChecked()) species.add("Mouton");
        if (cbChevre.isChecked()) species.add("Chèvre");
        if (cbPoule.isChecked()) species.add("Poule");
        return species;
    }

    private void generateSchedules() {
        List<String> selectedSpecies = getSelectedSpecies();

        if (selectedSpecies.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner au moins une espèce", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnGenerate.setEnabled(false);

        new Thread(() -> {
            try {
                int totalGenerated = 0;

                // Pour chaque espèce sélectionnée
                for (String species : selectedSpecies) {
                    List<AnimalEntity> animals = animalDao.getBySpecies(species);

                    for (AnimalEntity animal : animals) {
                        // Trouver le plan approprié
                        String category = determineCategory(animal);
                        String ageCategory = determineAgeCategory(animal);

                        AnimalFoodPlanEntity plan = planDao.findPlanForAnimal(
                                animal.getSpecies(),
                                category,
                                ageCategory,
                                animal.getWeight()
                        );

                        if (plan != null) {
                            // Générer les horaires pour chaque jour
                            Calendar currentDay = (Calendar) startCalendar.clone();

                            while (currentDay.before(endCalendar) || currentDay.equals(endCalendar)) {
                                String date = dateFormat.format(currentDay.getTime());

                                // Vérifier si des horaires existent déjà pour ce jour
                                List<AnimalFeedingScheduleEntity> existing =
                                        scheduleDao.getSchedulesForAnimalAndDate(animal.getId(), date);

                                if (existing.isEmpty()) {
                                    totalGenerated += createDailySchedules(animal, plan, date);
                                }

                                currentDay.add(Calendar.DAY_OF_MONTH, 1);
                            }
                        }
                    }
                }

                int finalTotal = totalGenerated;
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnGenerate.setEnabled(true);
                    Toast.makeText(this,
                            finalTotal + " repas générés avec succès!",
                            Toast.LENGTH_LONG).show();
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnGenerate.setEnabled(true);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private int createDailySchedules(AnimalEntity animal, AnimalFoodPlanEntity plan, String date) {
        try {
            JSONArray feedingTimes = new JSONArray(plan.getFeedingTimes());
            int mealsCreated = 0;

            for (int i = 0; i < feedingTimes.length(); i++) {
                String time = feedingTimes.getString(i);

                // Calculer les quantités pour ce repas
                double totalFood = plan.getTotalDailyFood();
                int mealsPerDay = plan.getMealsPerDay();

                double hayQty = (totalFood * plan.getHayPercentage() / 100) / mealsPerDay;
                double grainsQty = (totalFood * plan.getGrainsPercentage() / 100) / mealsPerDay;
                double supplementsQty = (totalFood * plan.getSupplementsPercentage() / 100) / mealsPerDay;
                double waterQty = plan.getWaterLiters() / mealsPerDay;

                AnimalFeedingScheduleEntity schedule = new AnimalFeedingScheduleEntity(
                        animal.getId(),
                        plan.getId(),
                        i + 1,
                        mealsPerDay,
                        date,
                        time,
                        hayQty,
                        grainsQty,
                        supplementsQty,
                        waterQty
                );

                scheduleDao.insert(schedule);
                mealsCreated++;
            }

            return mealsCreated;

        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String determineCategory(AnimalEntity animal) {
        // Logique simplifiée - à adapter selon vos besoins
        switch (animal.getSpecies()) {
            case "Vache":
                return animal.getWeight() > 450 ? "Laitière" : "Viande";
            case "Chèvre":
                return "Laitière";
            case "Poule":
                return "Pondeuse";
            default:
                return "Viande";
        }
    }

    private String determineAgeCategory(AnimalEntity animal) {
        // Logique simplifiée basée sur le poids
        switch (animal.getSpecies()) {
            case "Vache":
                return animal.getWeight() < 300 ? "Jeune" : "Adulte";
            case "Mouton":
            case "Chèvre":
                return animal.getWeight() < 30 ? "Jeune" : "Adulte";
            case "Poule":
                return "Adulte";
            default:
                return "Adulte";
        }
    }
}