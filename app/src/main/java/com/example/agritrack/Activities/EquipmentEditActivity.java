package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.EquipmentDao;
import com.example.agritrack.Database.EquipmentEntity;
import com.example.agritrack.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EquipmentEditActivity extends AppCompatActivity {

    private EquipmentDao equipmentDao;

    private long equipmentId = -1;
    private EquipmentEntity current;

    private EditText inputName;
    private AutoCompleteTextView inputType;
    private EditText inputPurchaseDate;
    private EditText inputCost;
    private EditText inputUsageHours;

    private MaterialButtonToggleGroup statusToggle;
    private String selectedStatus = "Actif";

    private BottomNavigationView bottomNavigationView;

    private static final int REQ_LOCATION_PERMISSION = 1101;
    private static final int OVERPASS_RADIUS_METERS = 15000;
    private FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_edit);

        setupBottomNavigation();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        equipmentDao = AgriTrackRoomDatabase.getInstance(this).equipmentDao();

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // If opened from scanner with a scanned code, prefill name when creating new entry
        String scanned = getIntent().getStringExtra("scanned_code");

        inputName = findViewById(R.id.inputEquipmentName);
        inputType = findViewById(R.id.inputEquipmentType);
        inputPurchaseDate = findViewById(R.id.inputEquipmentPurchaseDate);
        inputCost = findViewById(R.id.inputEquipmentCost);
        inputUsageHours = findViewById(R.id.inputEquipmentUsageHours);

        statusToggle = findViewById(R.id.toggleEquipmentStatus);
        if (statusToggle != null) {
            statusToggle.check(R.id.btnStatusActive);
            statusToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) return;
                if (checkedId == R.id.btnStatusActive) {
                    selectedStatus = "Actif";
                } else if (checkedId == R.id.btnStatusBroken) {
                    selectedStatus = "En panne";
                }
            });
        }

        if (inputType != null) {
            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    getResources().getStringArray(R.array.equipment_types)
            );
            inputType.setAdapter(typeAdapter);
        }

        if (inputPurchaseDate != null) {
            inputPurchaseDate.setOnClickListener(v -> showPurchaseDatePicker());
            inputPurchaseDate.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    showPurchaseDatePicker();
                }
            });
        }

        if (inputUsageHours != null) {
            inputUsageHours.setOnClickListener(v -> showUsageHoursPicker());
            inputUsageHours.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    showUsageHoursPicker();
                }
            });
        }

        Button btnSave = findViewById(R.id.btnSaveEquipment);
        Button btnDelete = findViewById(R.id.btnDeleteEquipment);

        MaterialButton btnNearbyServices = findViewById(R.id.btnNearbyServices);
        if (btnNearbyServices != null) {
            btnNearbyServices.setOnClickListener(v -> fetchAndShowNearbyServices());
        }

        equipmentId = getIntent().getLongExtra(EquipmentListActivity.EXTRA_EQUIPMENT_ID, -1);
        if (equipmentId != -1) {
            current = equipmentDao.getById(equipmentId);
            if (current != null) {
                inputName.setText(current.getName());
                if (inputType != null) {
                    inputType.setText(current.getType(), false);
                }
                inputPurchaseDate.setText(current.getPurchaseDate());
                inputCost.setText(String.valueOf(current.getCost()));
                inputUsageHours.setText(String.valueOf(current.getUsageHours()));

                String status = current.getStatus() != null ? current.getStatus().trim() : "";
                if (status.equalsIgnoreCase("En panne")) {
                    selectedStatus = "En panne";
                    if (statusToggle != null) statusToggle.check(R.id.btnStatusBroken);
                } else {
                    selectedStatus = "Actif";
                    if (statusToggle != null) statusToggle.check(R.id.btnStatusActive);
                }
                btnDelete.setVisibility(View.VISIBLE);
            } else {
                btnDelete.setVisibility(View.GONE);
            }
        } else {
            btnDelete.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(scanned)) {
                inputName.setText(scanned);
            }
        }

        btnSave.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String type = inputType != null ? inputType.getText().toString().trim() : "";
            String purchaseDate = inputPurchaseDate.getText().toString().trim();
            String status = selectedStatus != null ? selectedStatus.trim() : "";

            if (TextUtils.isEmpty(name)) {
                inputName.setError("Nom requis");
                return;
            }

            if (TextUtils.isEmpty(type)) type = "";
            if (TextUtils.isEmpty(purchaseDate)) purchaseDate = "";
            if (TextUtils.isEmpty(status)) status = "";

            double cost = 0.0;
            String costRaw = inputCost.getText().toString().trim();
            if (!TextUtils.isEmpty(costRaw)) {
                try {
                    cost = Double.parseDouble(costRaw);
                } catch (NumberFormatException ignored) {
                    inputCost.setError("Coût invalide");
                    return;
                }
            }

            int usageHours = 0;
            String hoursRaw = inputUsageHours.getText().toString().trim();
            if (!TextUtils.isEmpty(hoursRaw)) {
                try {
                    usageHours = Integer.parseInt(hoursRaw);
                } catch (NumberFormatException ignored) {
                    inputUsageHours.setError("Heures invalides");
                    return;
                }
            }

            if (current == null) {
                EquipmentEntity toInsert = new EquipmentEntity(name, type, purchaseDate, cost, status, usageHours);
                equipmentDao.insert(toInsert);
                Toast.makeText(this, "Matériel ajouté", Toast.LENGTH_SHORT).show();
            } else {
                current.setName(name);
                current.setType(type);
                current.setPurchaseDate(purchaseDate);
                current.setCost(cost);
                current.setStatus(status);
                current.setUsageHours(usageHours);
                equipmentDao.update(current);
                Toast.makeText(this, "Matériel mis à jour", Toast.LENGTH_SHORT).show();
            }
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            if (current != null) {
                equipmentDao.delete(current);
                Toast.makeText(this, "Matériel supprimé", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    private void fetchAndShowNearbyServices() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOCATION_PERMISSION
            );
            return;
        }

        Toast.makeText(this, "Recherche des services à proximité…", Toast.LENGTH_SHORT).show();

        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        queryOverpassAndShow(location.getLatitude(), location.getLongitude());
                        return;
                    }

                    // Fallback: some devices return null for getCurrentLocation.
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(last -> {
                                if (last == null) {
                                    Toast.makeText(this, "Impossible d'obtenir la localisation.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                queryOverpassAndShow(last.getLatitude(), last.getLongitude());
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Impossible d'obtenir la localisation.", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur localisation: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void queryOverpassAndShow(double lat, double lon) {
        networkExecutor.execute(() -> {
            try {
                String query = buildOverpassQuery(lat, lon);
                Request request = new Request.Builder()
                        .url("https://overpass-api.de/api/interpreter")
                        .post(RequestBody.create(query, MediaType.parse("text/plain; charset=utf-8")))
                        .header("User-Agent", "AgriTrack/1.0 (Android)")
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("HTTP " + response.code());
                    }

                    String json = response.body() != null ? response.body().string() : "";
                    List<NearbyPlace> places = parseOverpassResults(json, lat, lon);

                    runOnUiThread(() -> showNearbyServicesDialog(places, lat, lon));
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Erreur Overpass: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String buildOverpassQuery(double lat, double lon) {
        return String.format(Locale.US,
            "[out:json][timeout:25];(" +
                "node(around:%d,%f,%f)[\"amenity\"=\"car_repair\"];" +
                "way(around:%d,%f,%f)[\"amenity\"=\"car_repair\"];" +
                "node(around:%d,%f,%f)[\"shop\"=\"car_repair\"];" +
                "way(around:%d,%f,%f)[\"shop\"=\"car_repair\"];" +
                "node(around:%d,%f,%f)[\"amenity\"=\"fuel\"];" +
                "way(around:%d,%f,%f)[\"amenity\"=\"fuel\"];" +
                "node(around:%d,%f,%f)[\"shop\"=\"hardware\"];" +
                "way(around:%d,%f,%f)[\"shop\"=\"hardware\"];" +
                ");out center 20;",
            OVERPASS_RADIUS_METERS, lat, lon,
            OVERPASS_RADIUS_METERS, lat, lon,
            OVERPASS_RADIUS_METERS, lat, lon,
            OVERPASS_RADIUS_METERS, lat, lon,
            OVERPASS_RADIUS_METERS, lat, lon,
            OVERPASS_RADIUS_METERS, lat, lon,
            OVERPASS_RADIUS_METERS, lat, lon,
            OVERPASS_RADIUS_METERS, lat, lon
        );
    }

    private List<NearbyPlace> parseOverpassResults(String json, double originLat, double originLon) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONArray elements = root.optJSONArray("elements");
        if (elements == null) return new ArrayList<>();

        List<NearbyPlace> out = new ArrayList<>();
        for (int i = 0; i < elements.length(); i++) {
            JSONObject el = elements.optJSONObject(i);
            if (el == null) continue;

            JSONObject tags = el.optJSONObject("tags");
            if (tags == null) continue;

            String name = tags.optString("name", "").trim();
            if (name.isEmpty()) name = "(Sans nom)";

            String category = "Service";
            String amenity = tags.optString("amenity", "").trim();
            String shop = tags.optString("shop", "").trim();
            if ("car_repair".equalsIgnoreCase(amenity)) category = "Réparation";
            else if ("fuel".equalsIgnoreCase(amenity)) category = "Station";
            else if ("hardware".equalsIgnoreCase(shop)) category = "Quincaillerie";

            Double lat = null;
            Double lon = null;
            if (el.has("lat") && el.has("lon")) {
                lat = el.optDouble("lat");
                lon = el.optDouble("lon");
            } else {
                JSONObject center = el.optJSONObject("center");
                if (center != null && center.has("lat") && center.has("lon")) {
                    lat = center.optDouble("lat");
                    lon = center.optDouble("lon");
                }
            }

            if (lat == null || lon == null) continue;

            double distM = haversineMeters(originLat, originLon, lat, lon);
            out.add(new NearbyPlace(name, category, lat, lon, distM));
        }

        Collections.sort(out, Comparator.comparingDouble(p -> p.distanceMeters));
        if (out.size() > 10) {
            return new ArrayList<>(out.subList(0, 10));
        }
        return out;
    }

    private void showNearbyServicesDialog(List<NearbyPlace> places, double lat, double lon) {
        if (places == null || places.isEmpty()) {
            Toast.makeText(this,
                    String.format(Locale.getDefault(),
                            "Aucun service trouvé près de %.4f, %.4f (rayon %d m).",
                            lat, lon, OVERPASS_RADIUS_METERS),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[places.size()];
        for (int i = 0; i < places.size(); i++) {
            NearbyPlace p = places.get(i);
            String dist = p.distanceMeters >= 1000
                    ? String.format(Locale.getDefault(), "%.1f km", (p.distanceMeters / 1000.0))
                    : String.format(Locale.getDefault(), "%.0f m", p.distanceMeters);
            items[i] = String.format(Locale.getDefault(), "%s — %s (%s)", p.name, p.category, dist);
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Services à proximité")
                .setItems(items, (dialog, which) -> {
                    if (which < 0 || which >= places.size()) return;
                    NearbyPlace p = places.get(which);
                    openInMaps(p);
                })
                .setNegativeButton("Fermer", null)
                .show();
    }

    private void openInMaps(NearbyPlace p) {
        String label = Uri.encode(p.name);
        Uri uri = Uri.parse("geo:" + p.lat + "," + p.lon + "?q=" + p.lat + "," + p.lon + "(" + label + ")");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    static class NearbyPlace {
        final String name;
        final String category;
        final double lat;
        final double lon;
        final double distanceMeters;

        NearbyPlace(String name, String category, double lat, double lon, double distanceMeters) {
            this.name = name;
            this.category = category;
            this.lat = lat;
            this.lon = lon;
            this.distanceMeters = distanceMeters;
        }
    }

    @Override
    protected void onDestroy() {
        networkExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchAndShowNearbyServices();
            } else {
                Toast.makeText(this, "Permission localisation refusée.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) return;

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(EquipmentEditActivity.this, AccueilActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(EquipmentEditActivity.this, NotificationsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(EquipmentEditActivity.this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    private void showPurchaseDatePicker() {
        Calendar cal = Calendar.getInstance();

        // Try to reuse an already selected date if it matches yyyy-MM-dd.
        String currentText = inputPurchaseDate != null ? inputPurchaseDate.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(currentText) && currentText.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                int y = Integer.parseInt(currentText.substring(0, 4));
                int m = Integer.parseInt(currentText.substring(5, 7)) - 1;
                int d = Integer.parseInt(currentText.substring(8, 10));
                cal.set(y, m, d);
            } catch (Exception ignored) {
            }
        }

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dlg = new DatePickerDialog(this, (view, y, m, d) -> {
            String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
            if (inputPurchaseDate != null) {
                inputPurchaseDate.setText(formatted);
                inputPurchaseDate.clearFocus();
            }
        }, year, month, day);
        dlg.show();
    }

    private void showUsageHoursPicker() {
        int currentHours = 0;
        String currentText = inputUsageHours != null ? inputUsageHours.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(currentText)) {
            try {
                currentHours = Integer.parseInt(currentText);
            } catch (NumberFormatException ignored) {
            }
        }

        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(0);
        picker.setMaxValue(20000);
        picker.setValue(Math.max(0, Math.min(20000, currentHours)));

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Heures d'utilisation")
                .setView(picker)
                .setPositiveButton("OK", (d, w) -> {
                    if (inputUsageHours != null) {
                        inputUsageHours.setText(String.valueOf(picker.getValue()));
                        inputUsageHours.clearFocus();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}
