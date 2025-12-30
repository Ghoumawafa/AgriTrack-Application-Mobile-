package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.TerrainDao;
import com.example.agritrack.Database.TerrainEntity;
import com.example.agritrack.R;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TerrainEditActivity extends AppCompatActivity {

    private static final int REQ_LOCATION_PERMISSIONS = 1201;

    private TerrainDao terrainDao;

    private long terrainId = -1;
    private TerrainEntity current;

    private EditText inputName;
    private EditText inputLocation;
    private EditText inputArea;
    private AutoCompleteTextView inputSoilType;

    private ArrayAdapter<String> soilTypeAdapter;

    private MapView mapView;
    private MapboxMap map;
    private Marker marker;
    private Double selectedLat;
    private Double selectedLon;

    private MaterialTextView txtCoords;
    private MaterialTextView txtWeatherStatus;
    private MaterialTextView txtWeatherDetails;

    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();

    private FusedLocationProviderClient fusedLocationClient;
    private boolean triedAutoLocate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Mapbox.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terrain_edit);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        terrainDao = AgriTrackRoomDatabase.getInstance(this).terrainDao();

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        inputName = findViewById(R.id.inputTerrainName);
        inputLocation = findViewById(R.id.inputTerrainLocation);
        inputArea = findViewById(R.id.inputTerrainArea);
        inputSoilType = findViewById(R.id.inputTerrainSoilType);

        soilTypeAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            getResources().getStringArray(R.array.soil_types)
        );
        if (inputSoilType != null) {
            inputSoilType.setAdapter(soilTypeAdapter);
        }

        txtCoords = findViewById(R.id.txtTerrainCoords);
        txtWeatherStatus = findViewById(R.id.txtWeatherStatus);
        txtWeatherDetails = findViewById(R.id.txtWeatherDetails);

        mapView = findViewById(R.id.terrainMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapLibreMap -> {
            map = mapLibreMap;
            map.setStyle(new Style.Builder().fromUri("asset://osm_raster_style.json"), style -> {
                map.addOnMapClickListener(point -> {
                    setSelectedLocation(point.getLatitude(), point.getLongitude(), true);
                    return true;
                });

                // If we already have coordinates (from DB), show them.
                if (selectedLat != null && selectedLon != null) {
                    setSelectedLocation(selectedLat, selectedLon, false);
                } else {
                    // New terrain: try GPS auto-location once the style is ready.
                    ensureAutoLocate();
                }
            });
        });

        Button btnSave = findViewById(R.id.btnSaveTerrain);
        Button btnDelete = findViewById(R.id.btnDeleteTerrain);

        terrainId = getIntent().getLongExtra(TerrainListActivity.EXTRA_TERRAIN_ID, -1);
        if (terrainId != -1) {
            current = terrainDao.getById(terrainId);
            if (current != null) {
                inputName.setText(current.getName());
                inputLocation.setText(current.getLocation());
                inputArea.setText(String.valueOf(current.getArea()));
                if (inputSoilType != null) {
                    inputSoilType.setText(current.getSoilType(), false);
                }

                selectedLat = current.getLatitude();
                selectedLon = current.getLongitude();
                updateCoordsText();
                maybeLoadWeather();

                btnDelete.setVisibility(View.VISIBLE);
            } else {
                btnDelete.setVisibility(View.GONE);
            }
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        // If we are creating a new terrain and the map is not yet ready, we'll
        // still attempt auto-location once we can.
        if (terrainId == -1 && selectedLat == null && selectedLon == null) {
            ensureAutoLocate();
        }

        btnSave.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String location = inputLocation.getText().toString().trim();
            String soilType = inputSoilType != null ? inputSoilType.getText().toString().trim() : "";

            if (TextUtils.isEmpty(name)) {
                inputName.setError("Nom requis");
                return;
            }
            if (TextUtils.isEmpty(location)) {
                location = "";
            }
            if (TextUtils.isEmpty(soilType)) {
                soilType = "";
            }

            double area = 0.0;
            String areaRaw = inputArea.getText().toString().trim();
            if (!TextUtils.isEmpty(areaRaw)) {
                try {
                    area = Double.parseDouble(areaRaw);
                } catch (NumberFormatException ignored) {
                    inputArea.setError("Surface invalide");
                    return;
                }
            }

            if (current == null) {
                TerrainEntity toInsert = new TerrainEntity(name, location, area, soilType);
                toInsert.setLatitude(selectedLat);
                toInsert.setLongitude(selectedLon);
                long id = terrainDao.insert(toInsert);
                Toast.makeText(this, "Terrain ajouté", Toast.LENGTH_SHORT).show();
                terrainId = id;
                current = terrainDao.getById(id);
            } else {
                current.setName(name);
                current.setLocation(location);
                current.setArea(area);
                current.setSoilType(soilType);
                current.setLatitude(selectedLat);
                current.setLongitude(selectedLon);
                terrainDao.update(current);
                Toast.makeText(this, "Terrain mis à jour", Toast.LENGTH_SHORT).show();
            }
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            if (current != null) {
                terrainDao.delete(current);
                Toast.makeText(this, "Terrain supprimé", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    private void setSelectedLocation(double lat, double lon, boolean animateCamera) {
        selectedLat = lat;
        selectedLon = lon;
        updateCoordsText();
        maybeLoadWeather();

        // Auto-fill the text field so the user doesn't need to type the location.
        if (inputLocation != null) {
            inputLocation.setText(String.format(Locale.getDefault(), "%.6f, %.6f", lat, lon));
        }

        if (map == null) return;

        LatLng latLng = new LatLng(lat, lon);
        if (animateCamera) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0));
        } else if (map.getCameraPosition() == null || map.getCameraPosition().zoom < 3.0) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0));
        }

        if (marker != null) {
            map.removeMarker(marker);
        }
        marker = map.addMarker(new MarkerOptions().position(latLng));
    }

    private void ensureAutoLocate() {
        if (triedAutoLocate) return;
        if (selectedLat != null && selectedLon != null) return;
        triedAutoLocate = true;

        if (hasLocationPermission()) {
            fetchAndSelectCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION_PERMISSIONS
            );
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void fetchAndSelectCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            setSelectedLocation(location.getLatitude(), location.getLongitude(), true);
                            return;
                        }
                        CancellationTokenSource cts = new CancellationTokenSource();
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                                .addOnSuccessListener(this, fresh -> {
                                    if (fresh != null) {
                                        setSelectedLocation(fresh.getLatitude(), fresh.getLongitude(), true);
                                    } else {
                                        Toast.makeText(this, "Impossible d'obtenir la position GPS.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(this, e -> Toast.makeText(this, "Erreur GPS: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(this, e -> Toast.makeText(this, "Erreur GPS: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (SecurityException se) {
            // Permission race - ignore.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION_PERMISSIONS) {
            if (hasLocationPermission()) {
                fetchAndSelectCurrentLocation();
            } else {
                Toast.makeText(this, "Autorisation de localisation refusée. Vous pouvez toujours choisir sur la carte.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateCoordsText() {
        if (txtCoords == null) return;
        if (selectedLat == null || selectedLon == null) {
            txtCoords.setText("Coordonnées: —");
        } else {
            txtCoords.setText(String.format(Locale.getDefault(), "Coordonnées: %.6f, %.6f", selectedLat, selectedLon));
        }
    }

    private void maybeLoadWeather() {
        if (txtWeatherStatus == null || txtWeatherDetails == null) return;
        if (selectedLat == null || selectedLon == null) {
            txtWeatherStatus.setText("Sélectionnez un emplacement pour charger la météo.");
            txtWeatherDetails.setText("");
            return;
        }

        final double lat = selectedLat;
        final double lon = selectedLon;

        txtWeatherStatus.setText("Chargement de la météo...");
        txtWeatherDetails.setText("");

        networkExecutor.execute(() -> {
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

                String details = String.format(Locale.getDefault(),
                        "Température: %s°C\nVent: %s km/h\nPrécipitations: %s mm\nHeure: %s",
                        Double.isNaN(temp) ? "—" : String.format(Locale.getDefault(), "%.1f", temp),
                        Double.isNaN(wind) ? "—" : String.format(Locale.getDefault(), "%.1f", wind),
                        Double.isNaN(precip) ? "—" : String.format(Locale.getDefault(), "%.1f", precip),
                        TextUtils.isEmpty(time) ? "—" : time
                );

                runOnUiThread(() -> {
                    // Avoid updating UI if user moved to a different location meanwhile.
                    if (selectedLat == null || selectedLon == null) return;
                    txtWeatherStatus.setText("Météo actuelle (Open‑Meteo)");
                    txtWeatherDetails.setText(details);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtWeatherStatus.setText("Impossible de charger la météo.");
                    txtWeatherDetails.setText("");
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mapView != null) mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) mapView.onDestroy();
        networkExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}
