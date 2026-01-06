package com.example.agritrack.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.agritrack.Adapters.PlantAdapter;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.PlantDao;
import com.example.agritrack.Database.PlantEntity;
import com.example.agritrack.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class PlantActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private AgriTrackRoomDatabase db;
    private PlantDao plantDao;

    private Spinner spinnerFilter;
    private PlantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = AgriTrackRoomDatabase.getInstance(this);
        plantDao = db.plantDao();

        setupBottomNavigation();
        setupList();
        setupFilter();
        setupFab();

        refreshFilterOptions();
        refreshPlants();
    }

    private void setupList() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewPlants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PlantAdapter(this, plantDao, plant -> {
            Intent intent = new Intent(PlantActivity.this, PlantTreatmentActivity.class);
            intent.putExtra("plant_id", plant.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupFilter() {
        spinnerFilter = findViewById(R.id.spinnerFilter);
        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                refreshPlants();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab_add_plant);
        fab.setOnClickListener(v -> showAddDialog());
    }

    private void refreshFilterOptions() {
        List<String> types = new ArrayList<>();
        types.add("Tous");
        List<String> distinct = plantDao.getDistinctTypes();
        if (distinct != null) {
            for (String t : distinct) {
                if (!TextUtils.isEmpty(t)) types.add(t);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinnerFilter.setAdapter(adapter);
    }

    private void refreshPlants() {
        String selected = spinnerFilter.getSelectedItem() != null ? String.valueOf(spinnerFilter.getSelectedItem()) : "Tous";
        List<PlantEntity> plants;
        if ("Tous".equalsIgnoreCase(selected)) {
            plants = plantDao.getAllPlants();
        } else {
            plants = plantDao.getByType(selected);
        }
        adapter.submitList(plants);
    }

    private void showAddDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        EditText editName = new EditText(this);
        editName.setHint("Nom de la plante");

        EditText editType = new EditText(this);
        editType.setHint("Type (ex: Céréale)");

        EditText editStage = new EditText(this);
        editStage.setHint("Stade (ex: Croissance)");

        EditText editQuantity = new EditText(this);
        editQuantity.setHint("Quantité");
        editQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText editLocation = new EditText(this);
        editLocation.setHint("Zone/Emplacement (ex: A1)");

        EditText editDate = new EditText(this);
        editDate.setHint("Date plantation (ex: 2025-12-29)");

        container.addView(editName);
        container.addView(editType);
        container.addView(editStage);
        container.addView(editQuantity);
        container.addView(editLocation);
        container.addView(editDate);

        new AlertDialog.Builder(this)
                .setTitle("Nouvelle Plante")
                .setView(container)
                .setPositiveButton("Enregistrer", (d, which) -> {
                    String name = safeTrim(editName.getText().toString());
                    String type = safeTrim(editType.getText().toString());
                    String stage = safeTrim(editStage.getText().toString());
                    String location = safeTrim(editLocation.getText().toString());
                    String date = safeTrim(editDate.getText().toString());
                    int qty = parseInt(editQuantity.getText().toString(), 0);

                    if (TextUtils.isEmpty(name)) return;

                    PlantEntity plant = new PlantEntity(name, type, stage, qty, location, date);
                    plantDao.insert(plant);

                    refreshFilterOptions();
                    refreshPlants();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(PlantActivity.this, AccueilActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    Intent intent = new Intent(PlantActivity.this, NotificationsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(PlantActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static int parseInt(String value, int fallback) {
        try {
            if (TextUtils.isEmpty(value)) return fallback;
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
