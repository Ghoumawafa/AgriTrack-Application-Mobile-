package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.TerrainDao;
import com.example.agritrack.Database.TerrainEntity;
import com.example.agritrack.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TerrainListActivity extends AppCompatActivity {

    public static final String EXTRA_TERRAIN_ID = "terrain_id";

    private TerrainDao terrainDao;

    private final List<TerrainEntity> allTerrains = new ArrayList<>();
    private final List<TerrainEntity> visibleTerrains = new ArrayList<>();
    private ArrayAdapter<TerrainEntity> terrainsAdapter;

    private ArrayAdapter<String> soilTypeAdapter;
    private TextInputEditText searchInput;
    private AutoCompleteTextView soilTypeDropdown;
    private String selectedSoilType = "Tous";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terrain_list);

        terrainDao = AgriTrackRoomDatabase.getInstance(this).terrainDao();

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnAdd = findViewById(R.id.btnAddTerrain);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, TerrainEditActivity.class);
            startActivity(intent);
        });

        ListView listView = findViewById(R.id.terrainListView);
        terrainsAdapter = new ArrayAdapter<TerrainEntity>(this, R.layout.item_terrain_row, android.R.id.text1, visibleTerrains) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                android.widget.TextView textView = view.findViewById(android.R.id.text1);
                TerrainEntity t = getItem(position);
                if (t != null) {
                    String row = String.format(Locale.getDefault(), "%s — %.2f ha — %s",
                            t.getName(),
                            t.getArea(),
                            t.getLocation()
                    );
                    textView.setText(row);
                }
                return view;
            }

            @Override
            public long getItemId(int position) {
                TerrainEntity item = getItem(position);
                return item != null ? item.getId() : -1;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
        listView.setAdapter(terrainsAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            TerrainEntity terrain = visibleTerrains.get(position);
            Intent intent = new Intent(this, TerrainEditActivity.class);
            intent.putExtra(EXTRA_TERRAIN_ID, terrain.getId());
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            TerrainEntity terrain = visibleTerrains.get(position);
            showTerrainActionsDialog(terrain);
            return true;
        });

        searchInput = findViewById(R.id.searchTerrainsInput);
        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s != null ? s.toString() : "";
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        soilTypeDropdown = findViewById(R.id.soilTypeFilterDropdown);
        if (soilTypeDropdown != null) {
            List<String> soilOptions = new ArrayList<>();
            soilOptions.add("Tous");
            String[] predefined = getResources().getStringArray(R.array.soil_types);
            if (predefined != null) {
                for (String s : predefined) {
                    if (s != null && !s.trim().isEmpty()) {
                        soilOptions.add(s.trim());
                    }
                }
            }

            soilTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, soilOptions);
            soilTypeDropdown.setAdapter(soilTypeAdapter);
            soilTypeDropdown.setText(selectedSoilType, false);
            soilTypeDropdown.setOnItemClickListener((parent, view, position, id) -> {
                String picked = soilTypeAdapter.getItem(position);
                selectedSoilType = picked != null ? picked : "Tous";
                applyFilters();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        List<TerrainEntity> fromDb = terrainDao.getAll();
        allTerrains.clear();
        if (fromDb != null) {
            allTerrains.addAll(fromDb);
        }
        applyFilters();
    }

    private void applyFilters() {
        String q = searchQuery != null ? searchQuery.trim().toLowerCase(Locale.getDefault()) : "";
        String soil = selectedSoilType != null ? selectedSoilType.trim() : "Tous";
        boolean filterBySoil = soil != null && !soil.isEmpty() && !soil.equalsIgnoreCase("Tous");

        visibleTerrains.clear();
        for (TerrainEntity t : allTerrains) {
            if (t == null) continue;

            if (filterBySoil) {
                String soilType = t.getSoilType() != null ? t.getSoilType().trim() : "";
                if (!soilType.equalsIgnoreCase(soil)) {
                    continue;
                }
            }

            if (!q.isEmpty()) {
                String name = t.getName() != null ? t.getName() : "";
                String location = t.getLocation() != null ? t.getLocation() : "";
                String soilType = t.getSoilType() != null ? t.getSoilType() : "";
                String haystack = (name + " " + location + " " + soilType).toLowerCase(Locale.getDefault());
                if (!haystack.contains(q)) {
                    continue;
                }
            }
            visibleTerrains.add(t);
        }

        if (terrainsAdapter != null) {
            terrainsAdapter.notifyDataSetChanged();
        }
    }

    private void showTerrainActionsDialog(TerrainEntity terrain) {
        if (terrain == null) return;

        String[] actions = new String[]{"Modifier", "Supprimer"};
        new AlertDialog.Builder(this)
                .setTitle(terrain.getName())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(this, TerrainEditActivity.class);
                        intent.putExtra(EXTRA_TERRAIN_ID, terrain.getId());
                        startActivity(intent);
                    } else if (which == 1) {
                        confirmAndDeleteTerrain(terrain);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void confirmAndDeleteTerrain(TerrainEntity terrain) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer ce terrain ?")
                .setMessage("Cette action est irréversible.")
                .setPositiveButton("Supprimer", (d, w) -> {
                    terrainDao.delete(terrain);
                    refreshList();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}
