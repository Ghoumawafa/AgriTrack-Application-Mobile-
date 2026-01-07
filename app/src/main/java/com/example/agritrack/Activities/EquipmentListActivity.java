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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.EquipmentDao;
import com.example.agritrack.Database.EquipmentEntity;
import com.example.agritrack.R;
import com.example.agritrack.Adapters.EquipmentListAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
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
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EquipmentListActivity extends AppCompatActivity {

    public static final String EXTRA_EQUIPMENT_ID = "equipment_id";

    private EquipmentDao equipmentDao;
    private List<EquipmentEntity> equipments = new ArrayList<>();
    private EquipmentListAdapter adapter;
    private EditText inputSearch;
    private Spinner spinnerType;
    private Button btnFromDate, btnToDate, btnFilter, btnReset;
    private String fromDateStr = "", toDateStr = "";
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private BottomNavigationView bottomNavigationView;

    private RecyclerView recyclerView;

    private static final int REQ_LOCATION_PERMISSION = 1201;
    private static final int OVERPASS_RADIUS_METERS = 15000;
    private FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_list);

        setupBottomNavigation();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        equipmentDao = AgriTrackRoomDatabase.getInstance(this).equipmentDao();

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnAdd = findViewById(R.id.btnAddEquipment);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, EquipmentEditActivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.equipmentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EquipmentListAdapter(equipment -> {
            if (equipment == null) return;
            Intent intent = new Intent(this, EquipmentEditActivity.class);
            intent.putExtra(EXTRA_EQUIPMENT_ID, equipment.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // filter/search UI
        inputSearch = findViewById(R.id.inputSearch);
        spinnerType = findViewById(R.id.spinnerType);
        btnFromDate = findViewById(R.id.btnFromDate);
        btnToDate = findViewById(R.id.btnToDate);
        btnFilter = findViewById(R.id.btnFilter);
        btnReset = findViewById(R.id.btnReset);
        Button btnScan = findViewById(R.id.btnScanEquipment);

        // populate spinner with "Tous" + types
        String[] types = getResources().getStringArray(R.array.equipment_types);
        List<String> typeList = new ArrayList<>();
        typeList.add("Tous");
        for (String t : types) typeList.add(t);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        btnFromDate.setOnClickListener(v -> showDatePicker(true));
        btnToDate.setOnClickListener(v -> showDatePicker(false));

        btnFilter.setOnClickListener(v -> refreshList());
        btnReset.setOnClickListener(v -> {
            inputSearch.setText("");
            spinnerType.setSelection(0);
            fromDateStr = "";
            toDateStr = "";
            btnFromDate.setText("Depuis");
            btnToDate.setText("Jusqu'à");
            refreshList();
        });

        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, EquipmentScannerActivity.class);
            startActivity(intent);
        });

        MaterialButton btnNearbyServices = findViewById(R.id.btnNearbyServices);
        if (btnNearbyServices != null) {
            btnNearbyServices.setOnClickListener(v -> fetchAndShowNearbyServices());
        }
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
                    runOnUiThread(() -> showNearbyServicesDialog(places));
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
        if (out.size() > 10) return new ArrayList<>(out.subList(0, 10));
        return out;
    }

    private void showNearbyServicesDialog(List<NearbyPlace> places) {
        if (places == null || places.isEmpty()) {
            Toast.makeText(this, "Aucun service trouvé dans un rayon de 15 km.", Toast.LENGTH_SHORT).show();
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
                    openInMaps(places.get(which));
                })
                .setNegativeButton("Fermer", null)
                .show();
    }

    private void openInMaps(NearbyPlace p) {
        String label = Uri.encode(p.name);
        Uri uri = Uri.parse("geo:" + p.lat + "," + p.lon + "?q=" + p.lat + "," + p.lon + "(" + label + ")");
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
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
                    startActivity(new Intent(EquipmentListActivity.this, AccueilActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(EquipmentListActivity.this, NotificationsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(EquipmentListActivity.this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }
    private void refreshList() {
        List<EquipmentEntity> all = equipmentDao.getAll();
        String search = inputSearch != null ? inputSearch.getText().toString().trim().toLowerCase() : "";
        String selectedType = "Tous";
        if (spinnerType != null && spinnerType.getSelectedItem() != null) selectedType = spinnerType.getSelectedItem().toString();

        Date from = parseDate(fromDateStr);
        Date to = parseDate(toDateStr);

        equipments = new ArrayList<>();
        for (EquipmentEntity e : all) {
            boolean keep = true;
            String name = e.getName() != null ? e.getName().toLowerCase() : "";
            String type = e.getType() != null ? e.getType() : "";

            if (!TextUtils.isEmpty(search)) {
                if (!name.contains(search) && !type.toLowerCase().contains(search)) {
                    keep = false;
                }
            }

            if (keep && selectedType != null && !selectedType.equals("Tous") && !selectedType.equals(type)) {
                keep = false;
            }

            if (keep && (from != null || to != null)) {
                Date pd = parseDate(e.getPurchaseDate());
                if (pd == null) {
                    keep = false;
                } else {
                    if (from != null && pd.before(from)) keep = false;
                    if (to != null && pd.after(to)) keep = false;
                }
            }

            if (keep) {
                equipments.add(e);
            }
        }

        if (adapter != null) {
            adapter.submitList(equipments);
        }
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = Calendar.getInstance();
        String currentText = isFrom ? fromDateStr : toDateStr;
        if (!TextUtils.isEmpty(currentText) && currentText.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                int y = Integer.parseInt(currentText.substring(0, 4));
                int m = Integer.parseInt(currentText.substring(5, 7)) - 1;
                int d = Integer.parseInt(currentText.substring(8, 10));
                cal.set(y, m, d);
            } catch (Exception ignored) {}
        }

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dlg = new DatePickerDialog(this, (view, y, m, d) -> {
            String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
            if (isFrom) {
                fromDateStr = formatted;
                if (btnFromDate != null) btnFromDate.setText(formatted);
            } else {
                toDateStr = formatted;
                if (btnToDate != null) btnToDate.setText(formatted);
            }
        }, year, month, day);
        dlg.show();
    }

    private Date parseDate(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            return sdf.parse(t);
        } catch (ParseException e) {
            return null;
        }
    }
}
