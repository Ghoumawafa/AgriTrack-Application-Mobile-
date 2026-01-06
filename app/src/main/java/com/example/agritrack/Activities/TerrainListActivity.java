package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.TerrainDao;
import com.example.agritrack.Database.TerrainEntity;
import com.example.agritrack.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

    // Preview (map + weather)
    private MapView previewMapView;
    private MapboxMap previewMap;
    private Marker previewMarker;
    private MaterialTextView txtPreviewTitle;
    private MaterialTextView txtPreviewCoords;
    private MaterialTextView txtPreviewWeatherStatus;
    private MaterialTextView txtPreviewWeatherDetails;
    private Double previewLat;
    private Double previewLon;
    private final ExecutorService previewNetworkExecutor = Executors.newSingleThreadExecutor();

    // Click behavior
    private long lastTappedTerrainId = -1;
    private long lastTapAtMs = 0;
    private boolean shownTapHint = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Mapbox initialization is required before creating MapView
        Mapbox.getInstance(this);
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

        // Preview UI
        txtPreviewTitle = findViewById(R.id.txtPreviewTitle);
        txtPreviewCoords = findViewById(R.id.txtPreviewCoords);
        txtPreviewWeatherStatus = findViewById(R.id.txtPreviewWeatherStatus);
        txtPreviewWeatherDetails = findViewById(R.id.txtPreviewWeatherDetails);

        previewMapView = findViewById(R.id.terrainPreviewMapView);
        if (previewMapView != null) {
            previewMapView.onCreate(savedInstanceState);
            previewMapView.getMapAsync(mapLibreMap -> {
                previewMap = mapLibreMap;
                previewMap.setStyle(new Style.Builder().fromUri("asset://osm_raster_style.json"), style -> {
                    if (previewLat != null && previewLon != null) {
                        updatePreviewMap(previewLat, previewLon, false);
                    }
                });
            });
        }

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
            // Single tap shows preview; double tap opens edit.
            long now = SystemClock.elapsedRealtime();
            boolean isDoubleTap = terrain.getId() == lastTappedTerrainId && (now - lastTapAtMs) < 650;
            lastTappedTerrainId = terrain.getId();
            lastTapAtMs = now;

            if (isDoubleTap) {
                Intent intent = new Intent(this, TerrainEditActivity.class);
                intent.putExtra(EXTRA_TERRAIN_ID, terrain.getId());
                startActivity(intent);
            } else {
                showTerrainPreview(terrain);
                if (!shownTapHint) {
                    shownTapHint = true;
                    Toast.makeText(this, "Touchez 2 fois pour modifier", Toast.LENGTH_SHORT).show();
                }
            }
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
        if (previewMapView != null) previewMapView.onResume();
        refreshList();
    }

    private void refreshList() {
        List<TerrainEntity> fromDb = terrainDao.getAll();
        allTerrains.clear();
        if (fromDb != null) {
            allTerrains.addAll(fromDb);
        }
        applyFilters();

        // Auto-preview first item when entering the list.
        if (!visibleTerrains.isEmpty()) {
            showTerrainPreview(visibleTerrains.get(0));
        } else {
            clearPreview("Aucun terrain à afficher.");
        }
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

    private void showTerrainPreview(TerrainEntity terrain) {
        if (terrain == null) {
            clearPreview("Touchez un terrain pour afficher l'aperçu.");
            return;
        }

        if (txtPreviewTitle != null) {
            txtPreviewTitle.setText("Aperçu — " + terrain.getName());
        }

        Double lat = terrain.getLatitude();
        Double lon = terrain.getLongitude();
        previewLat = lat;
        previewLon = lon;

        if (lat == null || lon == null) {
            if (txtPreviewCoords != null) txtPreviewCoords.setText("Coordonnées: —");
            if (txtPreviewWeatherStatus != null) {
                txtPreviewWeatherStatus.setText("Ce terrain n'a pas de coordonnées enregistrées.");
            }
            if (txtPreviewWeatherDetails != null) txtPreviewWeatherDetails.setText("");
            if (previewMap != null && previewMarker != null) {
                previewMap.removeMarker(previewMarker);
                previewMarker = null;
            }
            return;
        }

        if (txtPreviewCoords != null) {
            txtPreviewCoords.setText(String.format(Locale.getDefault(), "Coordonnées: %.6f, %.6f", lat, lon));
        }

        updatePreviewMap(lat, lon, true);
        loadPreviewWeather(lat, lon);
    }

    private void clearPreview(String status) {
        if (txtPreviewTitle != null) txtPreviewTitle.setText("Aperçu du terrain");
        if (txtPreviewCoords != null) txtPreviewCoords.setText("Coordonnées: —");
        if (txtPreviewWeatherStatus != null) txtPreviewWeatherStatus.setText(status);
        if (txtPreviewWeatherDetails != null) txtPreviewWeatherDetails.setText("");
        previewLat = null;
        previewLon = null;
        if (previewMap != null && previewMarker != null) {
            previewMap.removeMarker(previewMarker);
            previewMarker = null;
        }
    }

    private void updatePreviewMap(double lat, double lon, boolean animate) {
        if (previewMap == null) return;

        LatLng latLng = new LatLng(lat, lon);
        if (animate) {
            previewMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0));
        } else {
            previewMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0));
        }

        if (previewMarker != null) {
            previewMap.removeMarker(previewMarker);
        }
        previewMarker = previewMap.addMarker(new MarkerOptions().position(latLng));
    }

    private void loadPreviewWeather(double lat, double lon) {
        if (txtPreviewWeatherStatus == null || txtPreviewWeatherDetails == null) return;

        txtPreviewWeatherStatus.setText("Chargement de la météo...");
        txtPreviewWeatherDetails.setText("");

        previewNetworkExecutor.execute(() -> {
            try {
                String urlStr = String.format(Locale.US,
                        "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=temperature_2m,wind_speed_10m,precipitation&timezone=auto",
                        lat, lon);
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                int code = conn.getResponseCode();
                if (code < 200 || code >= 300) {
                    throw new Exception("HTTP " + code);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                conn.disconnect();

                JSONObject root = new JSONObject(sb.toString());
                JSONObject current = root.optJSONObject("current");
                if (current == null) {
                    throw new Exception("Réponse météo invalide");
                }

                String time = current.optString("time", "");
                double temp = current.optDouble("temperature_2m", Double.NaN);
                double wind = current.optDouble("wind_speed_10m", Double.NaN);
                double precip = current.optDouble("precipitation", Double.NaN);

                final String details = String.format(Locale.getDefault(),
                        "Température: %s°C\nVent: %s km/h\nPrécipitations: %s mm\nHeure: %s",
                        Double.isNaN(temp) ? "—" : String.format(Locale.getDefault(), "%.1f", temp),
                        Double.isNaN(wind) ? "—" : String.format(Locale.getDefault(), "%.1f", wind),
                        Double.isNaN(precip) ? "—" : String.format(Locale.getDefault(), "%.1f", precip),
                        time == null || time.isEmpty() ? "—" : time
                );

                runOnUiThread(() -> {
                    if (previewLat == null || previewLon == null) return;
                    if (Math.abs(previewLat - lat) > 1e-9 || Math.abs(previewLon - lon) > 1e-9) return;
                    txtPreviewWeatherStatus.setText("Météo actuelle (Open‑Meteo)");
                    txtPreviewWeatherDetails.setText(details);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtPreviewWeatherStatus.setText("Impossible de charger la météo.");
                    txtPreviewWeatherDetails.setText("");
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (previewMapView != null) previewMapView.onStart();
    }

    @Override
    protected void onPause() {
        if (previewMapView != null) previewMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (previewMapView != null) previewMapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (previewMapView != null) previewMapView.onDestroy();
        previewNetworkExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (previewMapView != null) previewMapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (previewMapView != null) previewMapView.onSaveInstanceState(outState);
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
