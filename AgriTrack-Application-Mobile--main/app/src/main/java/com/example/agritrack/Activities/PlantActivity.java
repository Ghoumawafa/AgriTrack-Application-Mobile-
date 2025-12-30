package com.example.agritrack.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Adapters.PlantAdapter;
import com.example.agritrack.Database.AppDatabase;
import com.example.agritrack.Models.Plant;
import com.example.agritrack.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlantActivity extends AppCompatActivity {
    private static final String TAG = "PlantActivity";

    private RecyclerView recyclerView;
    private PlantAdapter adapter;
    private List<Plant> plantList = new ArrayList<>();
    private List<Plant> filteredList = new ArrayList<>();
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Spinner spinnerFilter;
    private String currentFilter = "Tous";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant);

        try {
            db = AppDatabase.getInstance(this);

            // Toolbar setup
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Plantes");
                }
                toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
            }

            // RecyclerView setup
            recyclerView = findViewById(R.id.recyclerViewPlants);
            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                adapter = new PlantAdapter(this, filteredList, new PlantAdapter.OnItemActionListener() {
                    @Override
                    public void onEdit(Plant plant) {
                        showAddPlantDialog(plant);
                    }

                    @Override
                    public void onDelete(Plant plant) {
                        showDeleteConfirmation(plant);
                    }

                    @Override
                    public void onViewTreatments(Plant plant) {
                        Intent intent = new Intent(PlantActivity.this, PlantTreatmentActivity.class);
                        intent.putExtra("plantId", plant.getId());
                        intent.putExtra("plantName", plant.getName());
                        startActivity(intent);
                    }
                });
                recyclerView.setAdapter(adapter);
            }

            // Filter spinner setup
            spinnerFilter = findViewById(R.id.spinnerFilter);
            setupFilterSpinner();

            // FAB setup
            FloatingActionButton fab = findViewById(R.id.fab_add_plant);
            if (fab != null) {
                fab.setOnClickListener(v -> showAddPlantDialog(null));
            }

            // Bottom navigation setup
            setupBottomNavigation();

            // Load data
            loadData();

        } catch (Exception ex) {
            Log.e(TAG, "onCreate failed", ex);
            Toast.makeText(this, "Erreur interne — impossible d'ouvrir Plantes", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupFilterSpinner() {
        List<String> filters = new ArrayList<>();
        filters.add("Tous");
        filters.add("Céréale");
        filters.add("Fruit");
        filters.add("Légume");
        filters.add("Semis");
        filters.add("Croissance");
        filters.add("Floraison");
        filters.add("Récolte");

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filters);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filters.get(position);
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void applyFilter() {
        filteredList.clear();
        if ("Tous".equals(currentFilter)) {
            filteredList.addAll(plantList);
        } else {
            for (Plant plant : plantList) {
                if (currentFilter.equals(plant.getType()) || currentFilter.equals(plant.getGrowthStage())) {
                    filteredList.add(plant);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                try {
                    if (id == R.id.nav_home) {
                        startActivity(new Intent(PlantActivity.this, AccueilActivity.class));
                        return true;
                    } else if (id == R.id.nav_notifications) {
                        startActivity(new Intent(PlantActivity.this, NotificationsActivity.class));
                        return true;
                    } else if (id == R.id.nav_profile) {
                        startActivity(new Intent(PlantActivity.this, ProfileActivity.class));
                        return true;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Navigation failed", e);
                    Toast.makeText(PlantActivity.this, "Impossible d'ouvrir la page demandée", Toast.LENGTH_SHORT).show();
                }
                return false;
            });
        }
    }

    private void loadData() {
        executor.execute(() -> {
            List<Plant> data = new ArrayList<>();
            try {
                List<Plant> fromDb = db.plantDao().getAllPlants();
                if (fromDb != null) data.addAll(fromDb);
            } catch (Exception e) {
                Log.w(TAG, "Failed to read plants", e);
            }
            runOnUiThread(() -> {
                plantList.clear();
                plantList.addAll(data);
                applyFilter();
            });
        });
    }

    private void showAddPlantDialog(Plant existingPlant) {
        boolean isEdit = existingPlant != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isEdit ? "Modifier Plante" : "Ajouter Plante");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText inputName = new EditText(this);
        inputName.setHint("Nom de la plante");
        if (isEdit) inputName.setText(existingPlant.getName());
        layout.addView(inputName);

        Spinner spinnerType = new Spinner(this);
        String[] types = {"Céréale", "Fruit", "Légume", "Autre"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        if (isEdit && existingPlant.getType() != null) {
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(existingPlant.getType())) {
                    spinnerType.setSelection(i);
                    break;
                }
            }
        }
        layout.addView(spinnerType);

        Spinner spinnerStage = new Spinner(this);
        String[] stages = {"Semis", "Croissance", "Floraison", "Récolte"};
        ArrayAdapter<String> stageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stages);
        stageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStage.setAdapter(stageAdapter);
        if (isEdit && existingPlant.getGrowthStage() != null) {
            for (int i = 0; i < stages.length; i++) {
                if (stages[i].equals(existingPlant.getGrowthStage())) {
                    spinnerStage.setSelection(i);
                    break;
                }
            }
        }
        layout.addView(spinnerStage);

        EditText inputLocation = new EditText(this);
        inputLocation.setHint("Zone/Emplacement");
        if (isEdit) inputLocation.setText(existingPlant.getLocation());
        layout.addView(inputLocation);

        EditText inputQuantity = new EditText(this);
        inputQuantity.setHint("Quantité");
        inputQuantity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (isEdit) inputQuantity.setText(String.valueOf(existingPlant.getQuantity()));
        layout.addView(inputQuantity);

        EditText inputArea = new EditText(this);
        inputArea.setHint("Surface (m²)");
        inputArea.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (isEdit) inputArea.setText(String.valueOf(existingPlant.getArea()));
        layout.addView(inputArea);

        EditText inputNotes = new EditText(this);
        inputNotes.setHint("Notes");
        if (isEdit) inputNotes.setText(existingPlant.getNotes());
        layout.addView(inputNotes);

        Button btnDate = new Button(this);
        final Calendar calendar = Calendar.getInstance();
        if (isEdit && existingPlant.getPlantingDate() != null) {
            calendar.setTime(existingPlant.getPlantingDate());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        btnDate.setText("Date: " + sdf.format(calendar.getTime()));
        btnDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        btnDate.setText("Date: " + sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
        layout.addView(btnDate);

        builder.setView(layout);

        builder.setPositiveButton(isEdit ? "Modifier" : "Ajouter", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String type = spinnerType.getSelectedItem().toString();
            String stage = spinnerStage.getSelectedItem().toString();
            String location = inputLocation.getText().toString().trim();
            String quantityStr = inputQuantity.getText().toString().trim();
            String areaStr = inputArea.getText().toString().trim();
            String notes = inputNotes.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Le nom est requis", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = quantityStr.isEmpty() ? 1 : Integer.parseInt(quantityStr);
            double area = areaStr.isEmpty() ? 0.0 : Double.parseDouble(areaStr);

            Plant plant = isEdit ? existingPlant : new Plant();
            plant.setName(name);
            plant.setType(type);
            plant.setGrowthStage(stage);
            plant.setLocation(location);
            plant.setQuantity(quantity);
            plant.setArea(area);
            plant.setNotes(notes);
            plant.setPlantingDate(calendar.getTime());

            savePlant(plant, isEdit);
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void savePlant(Plant plant, boolean isEdit) {
        executor.execute(() -> {
            try {
                if (isEdit) {
                    db.plantDao().update(plant);
                } else {
                    db.plantDao().insert(plant);
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, isEdit ? "Plante modifiée" : "Plante ajoutée", Toast.LENGTH_SHORT).show();
                    loadData();
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to save plant", e);
                runOnUiThread(() -> Toast.makeText(this, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showDeleteConfirmation(Plant plant) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer Plante")
                .setMessage("Êtes-vous sûr de vouloir supprimer '" + plant.getName() + "' ? Tous les traitements associés seront également supprimés.")
                .setPositiveButton("Supprimer", (dialog, which) -> deletePlant(plant))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deletePlant(Plant plant) {
        executor.execute(() -> {
            try {
                db.plantDao().delete(plant);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Plante supprimée", Toast.LENGTH_SHORT).show();
                    loadData();
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete plant", e);
                runOnUiThread(() -> Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}

