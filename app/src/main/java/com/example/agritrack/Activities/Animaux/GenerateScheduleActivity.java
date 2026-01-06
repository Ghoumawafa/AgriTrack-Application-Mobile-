package com.example.agritrack.Activities.Animaux;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.example.agritrack.Utils.NotificationScheduler;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GenerateScheduleActivity extends AppCompatActivity {

    private TextView tvStartDate, tvEndDate, tvSummary, tvDebug;
    private CheckBox cbVache, cbMouton, cbChevre, cbPoule;
    private Button btnGenerate, btnDebug;
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

        // Debug
        checkInitialData();
    }

    private void checkInitialData() {
        new Thread(() -> {
            StringBuilder debug = new StringBuilder();
            debug.append("🔍 DEBUG INITIAL:\n");

            // Vérifier les animaux
            List<String> allSpecies = animalDao.getAllSpecies();
            debug.append("Espèces trouvées: ").append(allSpecies.size()).append("\n");
            for (String species : allSpecies) {
                int count = animalDao.getCountBySpecies(species);
                debug.append("• ").append(species).append(": ").append(count).append(" animaux\n");
            }

            // Vérifier les plans
            List<AnimalFoodPlanEntity> allPlans = planDao.getAllPlans();
            debug.append("Plans alimentaires: ").append(allPlans.size()).append("\n");
            for (AnimalFoodPlanEntity plan : allPlans) {
                debug.append("• ").append(plan.getSpecies())
                        .append(" (").append(plan.getAgeCategory())
                        .append(") - ").append(plan.getMealsPerDay()).append(" repas/jour\n");
            }

            runOnUiThread(() -> {
                tvDebug.setText(debug.toString());
                Log.d("GENERATE_SCHEDULE", debug.toString());
            });
        }).start();
    }

    private void initializeViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvSummary = findViewById(R.id.tvSummary);
        tvDebug = findViewById(R.id.tvDebug);

        cbVache = findViewById(R.id.cbVache);
        cbMouton = findViewById(R.id.cbMouton);
        cbChevre = findViewById(R.id.cbChevre);
        cbPoule = findViewById(R.id.cbPoule);

        btnGenerate = findViewById(R.id.btnGenerate);
        btnDebug = findViewById(R.id.btnDebug);
        progressBar = findViewById(R.id.progressBar);

        btnDebug.setOnClickListener(v -> debugData());

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

    private void debugData() {
        new Thread(() -> {
            StringBuilder debug = new StringBuilder();
            debug.append("🔍 DONNÉES ACTUELLES:\n");

            // Vérifier les animaux
            List<String> selectedSpecies = getSelectedSpecies();
            debug.append("Espèces sélectionnées: ").append(selectedSpecies).append("\n\n");

            for (String species : selectedSpecies) {
                List<AnimalEntity> animals = animalDao.getBySpecies(species);
                debug.append(species).append(": ").append(animals.size()).append(" animaux\n");

                // Vérifier le plan pour cette espèce
                AnimalFoodPlanEntity plan = planDao.getPlanBySpecies(species);
                if (plan != null) {
                    debug.append("  ✅ Plan trouvé: ").append(plan.getMealsPerDay()).append(" repas/jour\n");
                    debug.append("  Heures: ").append(plan.getFeedingTimes()).append("\n");
                } else {
                    debug.append("  ❌ Aucun plan trouvé pour ").append(species).append("\n");
                    // Créer un plan par défaut
                    createDefaultPlan(species);
                }
            }

            runOnUiThread(() -> {
                tvDebug.setText(debug.toString());
                Toast.makeText(this, "Debug terminé", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void createDefaultPlan(String species) {
        new Thread(() -> {
            AnimalFoodPlanEntity plan = new AnimalFoodPlanEntity();
            plan.setSpecies(species);
            plan.setCategory("Standard");
            plan.setAgeCategory("Adulte");
            plan.setMinWeight(0);
            plan.setMaxWeight(1000);

            // Valeurs par défaut selon l'espèce
            switch (species) {
                case "Vache":
                    plan.setTotalDailyFood(12.0);
                    plan.setHayPercentage(60);
                    plan.setGrainsPercentage(30);
                    plan.setSupplementsPercentage(10);
                    plan.setWaterLiters(50);
                    plan.setFeedingTimes("[\"06:00\", \"12:00\", \"18:00\"]");
                    plan.setMealsPerDay(3);
                    break;
                case "Mouton":
                    plan.setTotalDailyFood(2.5);
                    plan.setHayPercentage(70);
                    plan.setGrainsPercentage(25);
                    plan.setSupplementsPercentage(5);
                    plan.setWaterLiters(8);
                    plan.setFeedingTimes("[\"08:00\", \"16:00\"]");
                    plan.setMealsPerDay(2);
                    break;
                case "Chèvre":
                    plan.setTotalDailyFood(3.0);
                    plan.setHayPercentage(50);
                    plan.setGrainsPercentage(40);
                    plan.setSupplementsPercentage(10);
                    plan.setWaterLiters(12);
                    plan.setFeedingTimes("[\"07:00\", \"13:00\", \"19:00\"]");
                    plan.setMealsPerDay(3);
                    break;
                case "Poule":
                    plan.setTotalDailyFood(0.12);
                    plan.setHayPercentage(0);
                    plan.setGrainsPercentage(80);
                    plan.setSupplementsPercentage(20);
                    plan.setWaterLiters(0.5);
                    plan.setFeedingTimes("[\"07:00\", \"16:00\"]");
                    plan.setMealsPerDay(2);
                    break;
            }

            planDao.insert(plan);
            Log.d("GENERATE_SCHEDULE", "Plan par défaut créé pour " + species);
        }).start();
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
            int totalPlans = 0;

            for (String species : selectedSpecies) {
                totalAnimals += animalDao.getCountBySpecies(species);

                AnimalFoodPlanEntity plan = planDao.getPlanBySpecies(species);
                if (plan != null) {
                    totalPlans++;
                }
            }

            long daysBetween = (endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis()) / (1000 * 60 * 60 * 24) + 1;
            int estimatedMeals = (int) (totalAnimals * daysBetween * 2.5);

            int finalTotalAnimals = totalAnimals;
            int finalTotalPlans = totalPlans;
            runOnUiThread(() -> {
                String summary = String.format(Locale.getDefault(),
                        "📊 Résumé:\n\n" +
                                "• %d animaux sélectionnés\n" +
                                "• %d plans alimentaires disponibles\n" +
                                "• %d jours de planning\n" +
                                "• ~%d repas à générer\n\n" +
                                "Status: %s",
                        finalTotalAnimals,
                        finalTotalPlans,
                        daysBetween,
                        estimatedMeals,
                        finalTotalPlans == selectedSpecies.size() ? "✅ Prêt" : "⚠️ Plans manquants"
                );
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
                StringBuilder log = new StringBuilder();
                log.append("📝 JOURNAL DE GÉNÉRATION:\n\n");

                // Pour chaque espèce sélectionnée
                for (String species : selectedSpecies) {
                    List<AnimalEntity> animals = animalDao.getBySpecies(species);

                    log.append("=== ").append(species).append(" ===\n");
                    log.append("Animaux: ").append(animals.size()).append("\n");

                    // Vérifier et créer un plan si nécessaire
                    AnimalFoodPlanEntity plan = planDao.getPlanBySpecies(species);
                    if (plan == null) {
                        log.append("⚠️ Création plan par défaut...\n");
                        createDefaultPlan(species);
                        plan = planDao.getPlanBySpecies(species);
                    }

                    if (plan != null) {
                        log.append("✅ Plan: ").append(plan.getMealsPerDay()).append(" repas/jour\n");

                        for (AnimalEntity animal : animals) {
                            Calendar currentDay = (Calendar) startCalendar.clone();

                            while (currentDay.before(endCalendar) || currentDay.equals(endCalendar)) {
                                String date = dateFormat.format(currentDay.getTime());

                                // Vérifier si des horaires existent déjà
                                List<AnimalFeedingScheduleEntity> existing =
                                        scheduleDao.getSchedulesForAnimalAndDate(animal.getId(), date);

                                if (existing.isEmpty()) {
                                    int created = createDailySchedules(animal, plan, date);
                                    totalGenerated += created;
                                    log.append("  • Animal ").append(animal.getName())
                                            .append(": ").append(created).append(" repas pour ").append(date).append("\n");
                                }

                                currentDay.add(Calendar.DAY_OF_MONTH, 1);
                            }
                        }
                    } else {
                        log.append("❌ Pas de plan disponible\n");
                    }
                    log.append("\n");
                }

                // Programmer les notifications
                if (totalGenerated > 0) {
                    NotificationScheduler.scheduleUpcomingNotifications(this);
                    log.append("\n✅ ").append(totalGenerated).append(" repas générés au total\n");
                    log.append("🔔 Notifications programmées\n");
                }

                final String finalLog = log.toString();
                final int finalTotal = totalGenerated;

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnGenerate.setEnabled(true);

                    // Afficher le journal
                    tvDebug.setText(finalLog);
                    Log.d("GENERATE_SCHEDULE", finalLog);

                    if (finalTotal > 0) {
                        Toast.makeText(this,
                                "✅ " + finalTotal + " repas générés et notifications programmées !",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "⚠️ Aucun repas généré. Vérifiez les plans alimentaires.",
                                Toast.LENGTH_LONG).show();
                    }

                    // Ne pas fermer automatiquement
                    // finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnGenerate.setEnabled(true);
                    tvDebug.setText("❌ Erreur: " + e.getMessage());
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

                // Calculer les quantités
                double totalFood = plan.getTotalDailyFood();
                int mealsPerDay = plan.getMealsPerDay();

                // Ajuster selon le poids de l'animal (optionnel)
                double weightFactor = animal.getWeight() / 500.0; // Poids de référence 500kg
                double adjustedFood = totalFood * weightFactor;

                double hayQty = (adjustedFood * plan.getHayPercentage() / 100) / mealsPerDay;
                double grainsQty = (adjustedFood * plan.getGrainsPercentage() / 100) / mealsPerDay;
                double supplementsQty = (adjustedFood * plan.getSupplementsPercentage() / 100) / mealsPerDay;
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

                Log.d("GENERATE_SCHEDULE", "Créé: " + animal.getName() + " - " + date + " " + time);
            }

            return mealsCreated;

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("GENERATE_SCHEDULE", "Erreur JSON pour plan: " + plan.getFeedingTimes());
            return 0;
        }
    }

    private String determineCategory(AnimalEntity animal) {
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