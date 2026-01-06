package com.example.agritrack.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.widget.SwitchCompat;

import com.example.agritrack.Adapters.IrrigationAdapter;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.IrrigationDao;
import com.example.agritrack.Database.IrrigationEntity;
import com.example.agritrack.R;
import com.example.agritrack.Utils.Esp32RtdbClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Date;

public class IrrigationActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private AgriTrackRoomDatabase db;
    private IrrigationDao irrigationDao;
    private IrrigationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irrigation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = AgriTrackRoomDatabase.getInstance(this);
        irrigationDao = db.irrigationDao();

        setupBottomNavigation();
        setupList();
        setupFab();

        refreshList();
    }

    private void setupList() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewIrrigation);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IrrigationAdapter(this, irrigationDao);
        recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab_add_irrigation);
        fab.setOnClickListener(v -> showAddDialog());
    }

    private void refreshList() {
        adapter.submitList(irrigationDao.getAllIrrigations());
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_add_irrigation, null);

        EditText editTerrain = dialogView.findViewById(R.id.editTerrainName);
        EditText editQuantity = dialogView.findViewById(R.id.editQuantity);
        Spinner spinnerMode = dialogView.findViewById(R.id.spinnerMode);
        SwitchCompat switchManual = dialogView.findViewById(R.id.switchManualState);
        EditText editThreshold = dialogView.findViewById(R.id.editThreshold);
        Spinner spinnerMethod = dialogView.findViewById(R.id.spinnerMethod);
        View btnSave = dialogView.findViewById(R.id.btnSave);
        if (btnSave != null) btnSave.setVisibility(View.GONE);

        String[] modes = new String[]{"auto", "manual"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, modes);
        spinnerMode.setAdapter(modeAdapter);
        spinnerMode.setSelection(0);

        switchManual.setChecked(false);
        switchManual.setEnabled(false);

        editThreshold.setText("1500");

        spinnerMode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                boolean isManual = "manual".equalsIgnoreCase(String.valueOf(spinnerMode.getSelectedItem()));
                switchManual.setEnabled(isManual);
                if (!isManual) {
                    switchManual.setChecked(false);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        String[] methods = new String[]{"Goutte-à-goutte", "Aspersion", "Arrosage manuel"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methods);
        spinnerMethod.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Nouvelle Irrigation")
                .setView(dialogView)
                .setPositiveButton("Enregistrer", (d, which) -> {
                    String terrain = safeTrim(editTerrain.getText().toString());
                    double qty = parseDouble(editQuantity.getText().toString(), 0.0);
                    String method = spinnerMethod.getSelectedItem() != null ? String.valueOf(spinnerMethod.getSelectedItem()) : "";

                    String mode = spinnerMode.getSelectedItem() != null ? String.valueOf(spinnerMode.getSelectedItem()) : "auto";
                    boolean manualState = switchManual.isChecked();
                    int threshold = parseInt(editThreshold.getText().toString(), 1500);

                    if (TextUtils.isEmpty(terrain)) return;

                    IrrigationEntity irrigation = new IrrigationEntity(
                            terrain,
                            new Date(),
                            qty,
                            method,
                            "Planifié",
                            null
                    );
                    irrigationDao.insert(irrigation);

                    // Push to Firebase RTDB so ESP32 can act
                    Esp32RtdbClient.upsertZoneConfig(terrain, mode, manualState, threshold, null);

                    refreshList();
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
                    Intent intent = new Intent(IrrigationActivity.this, AccueilActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    Intent intent = new Intent(IrrigationActivity.this, NotificationsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(IrrigationActivity.this, ProfileActivity.class);
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

    private static double parseDouble(String value, double fallback) {
        try {
            if (TextUtils.isEmpty(value)) return fallback;
            return Double.parseDouble(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
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
