package com.example.agritrack.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.agritrack.Adapters.PlantAdapter;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.PlantDao;
import com.example.agritrack.Database.PlantEntity;
import com.example.agritrack.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlantActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private AgriTrackRoomDatabase db;
    private PlantDao plantDao;
    private Spinner spinnerFilter;
    private PlantAdapter adapter;

    // Predefined options for Type spinner
    private final String[] PLANT_TYPES = {
            "Céréale",
            "Légume",
            "Arbre fruitier",
            "Plante aromatique",
            "Fleur",
            "Autre"
    };

    // Predefined options for Stage spinner
    private final String[] PLANT_STAGES = {
            "Germination",
            "Croissance",
            "Floraison",
            "Fructification",
            "Maturation",
            "Récolte",
            "Repos végétatif"
    };

    // Predefined zone options (you can customize these based on your farm layout)
    private final String[] FARM_ZONES = {
            "Zone A1",
            "Zone A2",
            "Zone A3",
            "Zone B1",
            "Zone B2",
            "Zone B3",
            "Zone C1",
            "Zone C2",
            "Zone C3",
            "Serre 1",
            "Serre 2",
            "Jardin extérieur",
            "Autre"
    };

    private Calendar selectedDate = Calendar.getInstance();

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
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshPlants();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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

        // Name field
        EditText editName = new EditText(this);
        editName.setHint("Nom de la plante");
        editName.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        container.addView(editName);
        addSpace(container);

        // Type field with label
        TextView typeLabel = createLabel("Type");
        container.addView(typeLabel);

        Spinner spinnerType = new Spinner(this);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                PLANT_TYPES
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        container.addView(spinnerType);
        addSpace(container);

        // Stage field with label
        TextView stageLabel = createLabel("Stade");
        container.addView(stageLabel);

        Spinner spinnerStage = new Spinner(this);
        ArrayAdapter<String> stageAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                PLANT_STAGES
        );
        stageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStage.setAdapter(stageAdapter);
        spinnerStage.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        container.addView(spinnerStage);
        addSpace(container);

        // Quantity field
        EditText editQuantity = new EditText(this);
        editQuantity.setHint("Quantité");
        editQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
        editQuantity.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        container.addView(editQuantity);
        addSpace(container);

        // Location field with select button
        TextView locationLabel = createLabel("Zone/Emplacement");
        container.addView(locationLabel);

        LinearLayout locationLayout = new LinearLayout(this);
        locationLayout.setOrientation(LinearLayout.HORIZONTAL);
        locationLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        final EditText editLocation = new EditText(this);
        editLocation.setHint("Cliquez pour sélectionner une zone");
        editLocation.setFocusable(false);
        editLocation.setClickable(true);
        editLocation.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button btnSelectLocation = new Button(this);
        btnSelectLocation.setText("Choisir");
        btnSelectLocation.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        btnSelectLocation.setOnClickListener(v -> showZoneSelectionDialog(editLocation));
        editLocation.setOnClickListener(v -> showZoneSelectionDialog(editLocation));

        locationLayout.addView(editLocation);
        locationLayout.addView(btnSelectLocation);
        container.addView(locationLayout);
        addSpace(container);

        // Date field with calendar button
        TextView dateLabel = createLabel("Date de plantation");
        container.addView(dateLabel);

        LinearLayout dateLayout = new LinearLayout(this);
        dateLayout.setOrientation(LinearLayout.HORIZONTAL);
        dateLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        final EditText editDate = new EditText(this);
        editDate.setHint("Cliquez pour sélectionner une date");
        editDate.setFocusable(false);
        editDate.setClickable(true);
        editDate.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button btnDatePicker = new Button(this);
        btnDatePicker.setText("📅");
        btnDatePicker.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        btnDatePicker.setOnClickListener(v -> showDatePickerDialog(editDate));
        editDate.setOnClickListener(v -> showDatePickerDialog(editDate));

        dateLayout.addView(editDate);
        dateLayout.addView(btnDatePicker);
        container.addView(dateLayout);

        new AlertDialog.Builder(this)
                .setTitle("Nouvelle Plante")
                .setView(container)
                .setPositiveButton("Enregistrer", (d, which) -> {
                    String name = safeTrim(editName.getText().toString());
                    String type = spinnerType.getSelectedItem().toString();
                    String stage = spinnerStage.getSelectedItem().toString();
                    String location = safeTrim(editLocation.getText().toString());
                    String date = safeTrim(editDate.getText().toString());
                    int qty = parseInt(editQuantity.getText().toString(), 0);

                    if (TextUtils.isEmpty(name)) {
                        editName.setError("Le nom est requis");
                        return;
                    }

                    if (TextUtils.isEmpty(location)) {
                        editLocation.setError("Veuillez sélectionner une zone");
                        return;
                    }

                    if (TextUtils.isEmpty(date)) {
                        editDate.setError("Veuillez sélectionner une date");
                        return;
                    }

                    PlantEntity plant = new PlantEntity(name, type, stage, qty, location, date);
                    plantDao.insert(plant);

                    refreshFilterOptions();
                    refreshPlants();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showDatePickerDialog(final EditText editDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
                        String formattedDate = sdf.format(selectedDate.getTime());
                        editDate.setText(formattedDate);
                    }
                },
                year, month, day
        );

        // Optional: Set min/max dates if needed
        // datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        // datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000));

        datePickerDialog.show();
    }

    private void showZoneSelectionDialog(final EditText editLocation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sélectionner une zone");

        builder.setItems(FARM_ZONES, (dialog, which) -> {
            String selectedZone = FARM_ZONES[which];
            editLocation.setText(selectedZone);

            // If "Autre" is selected, allow custom input
            if ("Autre".equals(selectedZone)) {
                showCustomZoneDialog(editLocation);
            }
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showCustomZoneDialog(final EditText editLocation) {
        EditText customZoneInput = new EditText(this);
        customZoneInput.setHint("Entrez le nom de la zone");

        new AlertDialog.Builder(this)
                .setTitle("Zone personnalisée")
                .setMessage("Entrez le nom de votre zone:")
                .setView(customZoneInput)
                .setPositiveButton("OK", (dialog, which) -> {
                    String customZone = safeTrim(customZoneInput.getText().toString());
                    if (!TextUtils.isEmpty(customZone)) {
                        editLocation.setText(customZone);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private TextView createLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(14);
        label.setTextColor(getResources().getColor(android.R.color.black));
        label.setPadding(0, 8, 0, 4);
        return label;
    }

    private void addSpace(LinearLayout container) {
        View space = new View(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (8 * getResources().getDisplayMetrics().density)));
        container.addView(space);
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