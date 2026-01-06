package com.example.agritrack.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;

import android.app.Activity;
import android.speech.RecognizerIntent;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.agritrack.Adapters.IrrigationAdapter;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.IrrigationDao;
import com.example.agritrack.Database.IrrigationEntity;
import com.example.agritrack.R;
import com.example.agritrack.Utils.Esp32RtdbClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IrrigationActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private AgriTrackRoomDatabase db;
    private IrrigationDao irrigationDao;
    private IrrigationAdapter adapter;

    // Minimal control UI
    private TextView tvIrrigationStatus;
    private TextView tvModeLabel;
    private TextView tvSensorLabel;
    private SwitchCompat switchModeManual;
    private MaterialButton btnToggleIrrigation;

    // Local UI state (since /irrigation/zone1 currently stores enabled + ledOn only)
    private volatile boolean zoneEnabledUi = false;
    private volatile boolean zoneLedOnUi = false;
    private volatile boolean manualModeUi = false; // app-side mode selection (also written to Firebase via Esp32RtdbClient)

    // ESP32 pin lists (as requested)
    private static final Integer[] SENSOR_PINS = new Integer[]{12, 13, 14, 15, 16};
    private static final Integer[] ACTUATOR_PINS = new Integer[]{17, 18, 19, 21, 22, 23, 25};

    private static final int DEFAULT_THRESHOLD = 1500;

    private static final String DB_BASE = "https://agritrack-48076-default-rtdb.firebaseio.com";
    private static final String AUTH = "MDIMJUvH7ZZsputknLwJdCvcgsbRJbf0BTGnEQqq";
    private static final OkHttpClient http = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Polling for real-time display
    private final Handler zonePollHandler = new Handler(Looper.getMainLooper());
    private final long ZONE_POLL_MS = 1500;
    private final Runnable zonePollRunnable = new Runnable() {
        @Override
        public void run() {
            fetchZone1StateAndUpdate();
            zonePollHandler.postDelayed(this, ZONE_POLL_MS);
        }
    };

    private static final int REQ_SPEECH = 901;
    private Button btnVoiceCommand;

    // Single-zone key used across app + ESP32: /irrigation/zone1
    private static final String ZONE1_KEY = "zone1";

    // Stats + calendar UI (optional: present only if included in activity_irrigation.xml)
    private TextView tvWaterUsedToday;
    private TextView tvEstimatedPerIrrigation;
    private TextView tvWaterSaved;
    private TextView tvEfficiency;
    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private TextView tvDateWaterUsage;
    private TextView tvDateEvents;
    private long selectedDateMillis = -1L;

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
        setupControlUi();
        setupStatsAndCalendar();

        setupVoice();

        refreshList();
        refreshStatsAndSelectedDay();

        // start real-time polling to update irrigation indicator in list
        startZone1Polling();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopZone1Polling();
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
        // keep stats in sync with new records
        refreshStatsAndSelectedDay();
    }

    private void setupControlUi() {
        tvIrrigationStatus = findViewById(R.id.tvIrrigationStatus);
        tvModeLabel = findViewById(R.id.tvModeLabel);
        tvSensorLabel = findViewById(R.id.tvSensorLabel);
        switchModeManual = findViewById(R.id.switchModeManual);
        btnToggleIrrigation = findViewById(R.id.btnToggleIrrigation);

        if (switchModeManual != null) {
            switchModeManual.setChecked(false);
            switchModeManual.setOnCheckedChangeListener((buttonView, isChecked) -> {
                manualModeUi = isChecked;
                if (tvModeLabel != null) tvModeLabel.setText("Mode: " + (manualModeUi ? "Manual" : "Auto"));

                // Keep existing irrigation logic intact: still write mode/manualState/threshold for ESP32 logic compatibility
                // In manual: manualState should follow current ledOn
                Esp32RtdbClient.upsertZoneConfig(
                        ZONE1_KEY,
                        manualModeUi ? "manual" : "auto",
                        zoneLedOnUi,
                        DEFAULT_THRESHOLD,
                        null
                );
            });
        }

        if (btnToggleIrrigation != null) {
            btnToggleIrrigation.setOnClickListener(v -> {
                // One tap: toggle ledOn
                boolean next = !zoneLedOnUi;
                patchZone1EnabledLed(true, next);
                // immediate UI feedback (poll will confirm)
                zoneEnabledUi = true;
                zoneLedOnUi = next;
                renderZoneUi();

                // If user is in manual mode, keep manualState in sync too (so ESP32 doesn't override)
                if (manualModeUi) {
                    Esp32RtdbClient.upsertZoneConfig(ZONE1_KEY, "manual", next, DEFAULT_THRESHOLD, null);
                }
            });
        }

        renderZoneUi();
    }

    private void renderZoneUi() {
        if (tvIrrigationStatus != null) {
            boolean running = zoneEnabledUi && zoneLedOnUi;
            tvIrrigationStatus.setText(running ? "ON" : "OFF");
            tvIrrigationStatus.setBackgroundColor(getResources().getColor(
                    running ? android.R.color.holo_green_dark : android.R.color.holo_red_dark
            ));
        }
        if (tvModeLabel != null) {
            tvModeLabel.setText("Mode: " + (manualModeUi ? "Manual" : "Auto"));
        }
        if (btnToggleIrrigation != null) {
            btnToggleIrrigation.setText((zoneEnabledUi && zoneLedOnUi) ? "Turn OFF" : "Turn ON");
        }
        if (tvSensorLabel != null) {
            // Keep simulated sensor value (or later replace with real value from Firebase if you add it)
            tvSensorLabel.setText("🔘 2000");
        }
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_add_irrigation, null);

        TextInputEditText editTerrain = dialogView.findViewById(R.id.editTerrainName);
        TextInputEditText editQuantity = dialogView.findViewById(R.id.editQuantity);
        Spinner spinnerMode = dialogView.findViewById(R.id.spinnerMode);
        SwitchCompat switchManual = dialogView.findViewById(R.id.switchManualState);
        TextInputEditText editThreshold = dialogView.findViewById(R.id.editThreshold);
        Spinner spinnerMethod = dialogView.findViewById(R.id.spinnerMethod);
        Spinner spinnerSensorPin = dialogView.findViewById(R.id.spinnerSensorPin);
        Spinner spinnerActuatorPin = dialogView.findViewById(R.id.spinnerActuatorPin);
        View btnSave = dialogView.findViewById(R.id.btnSave);
        if (btnSave != null) btnSave.setVisibility(View.GONE);

        String[] modes = new String[]{"auto", "manual"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, modes);
        spinnerMode.setAdapter(modeAdapter);
        spinnerMode.setSelection(0);

        // FIX: don't hard-lock manual switch false; let mode selection control enablement
        switchManual.setChecked(false);
        switchManual.setEnabled("manual".equalsIgnoreCase(String.valueOf(spinnerMode.getSelectedItem())));

        spinnerMode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                boolean isManual = "manual".equalsIgnoreCase(String.valueOf(spinnerMode.getSelectedItem()));
                switchManual.setEnabled(isManual);
                // Do not auto-force OFF here; user might want ON immediately in manual
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        String[] methods = new String[]{"Goutte-à-goutte", "Aspersion", "Arrosage manuel"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methods);
        spinnerMethod.setAdapter(adapter);

        // Populate pin spinners
        if (spinnerSensorPin != null) {
            ArrayAdapter<Integer> sensorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SENSOR_PINS);
            spinnerSensorPin.setAdapter(sensorAdapter);
            spinnerSensorPin.setSelection(0); // default: GPIO12
        }
        if (spinnerActuatorPin != null) {
            ArrayAdapter<Integer> actuatorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ACTUATOR_PINS);
            spinnerActuatorPin.setAdapter(actuatorAdapter);
            spinnerActuatorPin.setSelection(0); // default: GPIO17
        }

        // FIX: ensure threshold has a default value always
        if (editThreshold != null) editThreshold.setText(String.valueOf(DEFAULT_THRESHOLD));

        new AlertDialog.Builder(this)
                .setTitle("Nouvelle Irrigation")
                .setView(dialogView)
                .setPositiveButton("Enregistrer", (d, which) -> {
                    String terrain = safeTrim(editTerrain.getText().toString());
                    double qty = parseDouble(editQuantity.getText().toString(), 0.0);
                    String method = spinnerMethod.getSelectedItem() != null ? String.valueOf(spinnerMethod.getSelectedItem()) : "";

                    String mode = spinnerMode.getSelectedItem() != null ? String.valueOf(spinnerMode.getSelectedItem()) : "auto";
                    boolean manualState = switchManual.isChecked();
                    int threshold = parseInt(editThreshold.getText().toString(), DEFAULT_THRESHOLD);

                    Integer sensorPin = spinnerSensorPin != null ? (Integer) spinnerSensorPin.getSelectedItem() : null;
                    Integer actuatorPin = spinnerActuatorPin != null ? (Integer) spinnerActuatorPin.getSelectedItem() : null;

                    IrrigationEntity entity = new IrrigationEntity();
                    entity.setTerrainName(terrain);
                    entity.setIrrigationDate(new Date());
                    entity.setWaterQuantity(qty);
                    // Make stats meaningful: use entered quantity as estimated "waterUsed" until you have a flow sensor.
                    entity.setWaterUsed(qty);
                    entity.setMethod(method);
                    entity.setStatus("planifié");
                    entity.setSensorPin(sensorPin != null ? sensorPin : -1);
                    entity.setActuatorPin(actuatorPin != null ? actuatorPin : -1);

                    irrigationDao.insert(entity);

                    // FIX: push config to the single zone in Firebase (mode/manualState are what ESP32 reads)
                    Esp32RtdbClient.upsertZoneConfig(ZONE1_KEY, mode, manualState, threshold, null);
                    patchZone1EnabledLed(true, "manual".equalsIgnoreCase(mode) && manualState);

                    // reflect mode immediately in simplified UI
                    manualModeUi = "manual".equalsIgnoreCase(mode);
                    if (switchModeManual != null) switchModeManual.setChecked(manualModeUi);

                    Toast.makeText(this,
                            "Sent to Firebase: mode=" + mode + " manual=" + manualState +
                                    " sensorPin=" + sensorPin + " actuatorPin=" + actuatorPin,
                            Toast.LENGTH_SHORT).show();

                    refreshList();
                    Toast.makeText(this, "Irrigation créée avec succès!", Toast.LENGTH_SHORT).show();
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

    private void setupVoice() {
        btnVoiceCommand = findViewById(R.id.btnVoiceCommand);
        if (btnVoiceCommand == null) return;
        btnVoiceCommand.setOnClickListener(v -> startSpeechToText());
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say: turn irrigation on/off");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        try {
            startActivityForResult(intent, REQ_SPEECH);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_SPEECH) return;
        if (resultCode != Activity.RESULT_OK || data == null) {
            Toast.makeText(this, "No voice input", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (results == null || results.isEmpty()) {
            Toast.makeText(this, "No voice results", Toast.LENGTH_SHORT).show();
            return;
        }

        Boolean cmd = null; // true=ON, false=OFF
        String matched = null;
        for (String r : results) {
            cmd = parseIrrigationCommand(r);
            if (cmd != null) { matched = r; break; }
        }
        if (cmd == null) {
            Toast.makeText(this, "Command not recognized. Try: \"Turn irrigation on/off\"", Toast.LENGTH_LONG).show();
            return;
        }

        boolean turnOn = cmd;
        patchZone1EnabledLed(true, turnOn);
        Toast.makeText(this,
                (turnOn ? "Irrigation ON" : "Irrigation OFF") + (matched != null ? " (" + matched + ")" : ""),
                Toast.LENGTH_SHORT).show();
    }

    // Accent-tolerant + variation-tolerant command parser (EN/FR)
    private static Boolean parseIrrigationCommand(String raw) {
        if (TextUtils.isEmpty(raw)) return null;
        String s = raw.toLowerCase(Locale.ROOT).trim();
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        boolean mentionsIrrigation =
                s.contains("irrig") || s.contains("arros") || s.contains("water") || s.contains("pompe") || s.contains("pump");

        // If user didn't mention irrigation at all, do NOT accept generic "on/off" to avoid accidental triggers.
        if (!mentionsIrrigation) return null;

        // OFF intents (check first)
        if (containsAny(s,
                "turn off", "switch off", "stop", "shutdown", "shut down", "disable", "deactivate",
                "etein", "eteindre", "arrete", "arreter", "stopper", "desact", "coupe", "couper", "off")) {
            return Boolean.FALSE;
        }

        // ON intents
        if (containsAny(s,
                "turn on", "switch on", "start", "enable", "activate", "open",
                "allume", "allumer", "demarre", "demarrer", "active", "activer", "on")) {
            return Boolean.TRUE;
        }

        return null;
    }

    private static boolean containsAny(String s, String... needles) {
        for (String n : needles) {
            if (s.contains(n)) return true;
        }
        return false;
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

    private static void patchZone1EnabledLed(boolean enabled, boolean ledOn) {
        String url = DB_BASE + "/irrigation/zone1.json?auth=" + AUTH;
        String bodyJson = "{\"enabled\":" + (enabled ? "true" : "false") + ",\"ledOn\":" + (ledOn ? "true" : "false") + "}";
        Request req = new Request.Builder()
                .url(url)
                .patch(RequestBody.create(bodyJson, JSON))
                .build();
        http.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { /* ignore */ }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) { response.close(); }
        });
    }

    private void startZone1Polling() {
        zonePollHandler.removeCallbacks(zonePollRunnable);
        zonePollHandler.post(zonePollRunnable);
    }

    private void stopZone1Polling() {
        zonePollHandler.removeCallbacks(zonePollRunnable);
    }

    private void fetchZone1StateAndUpdate() {
        String url = DB_BASE + "/irrigation/zone1.json?auth=" + AUTH;
        Request req = new Request.Builder().url(url).get().build();
        http.newCall(req).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) { }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                String body = "";
                try {
                    if (response.body() != null) body = response.body().string();
                } finally {
                    response.close();
                }

                boolean enabled = false;
                boolean ledOn = false;
                try {
                    if (!TextUtils.isEmpty(body)) {
                        JSONObject o = new JSONObject(body);
                        if (o.has("enabled") && !o.isNull("enabled")) enabled = o.getBoolean("enabled");
                        if (o.has("ledOn") && !o.isNull("ledOn")) ledOn = o.getBoolean("ledOn");
                    }
                } catch (JSONException ignored) { }

                final boolean fe = enabled;
                final boolean fl = ledOn;
                runOnUiThread(() -> {
                    // update list indicators
                    if (adapter != null) adapter.updateZoneState(fe, fl);

                    // update top control card indicators
                    zoneEnabledUi = fe;
                    zoneLedOnUi = fl;
                    renderZoneUi();
                });
            }
        });
    }

    private void setupStatsAndCalendar() {
        // These views exist only if you include layout_irrigation_stats.xml in activity_irrigation.xml.
        // Null-safe: if not present, stats/calendar simply won't show.
        tvWaterUsedToday = findViewById(R.id.tvWaterUsedToday);
        tvEstimatedPerIrrigation = findViewById(R.id.tvEstimatedPerIrrigation);
        tvWaterSaved = findViewById(R.id.tvWaterSaved);
        tvEfficiency = findViewById(R.id.tvEfficiency);
        calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvDateWaterUsage = findViewById(R.id.tvDateWaterUsage);
        tvDateEvents = findViewById(R.id.tvDateEvents);

        if (calendarView != null) {
            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                selectedDateMillis = c.getTimeInMillis();
                refreshStatsAndSelectedDay();
            });
        }
    }

    private void refreshStatsAndSelectedDay() {
        // --- Today totals (Room) ---
        double todayTotal = 0.0;
        int todayCount = 0;
        try {
            Double dUsed = irrigationDao.getTotalWaterUsedToday();
            todayTotal = (dUsed != null ? dUsed : 0.0);
            // Fallback if older rows have waterUsed=0 (or null sum)
            if (todayTotal <= 0.0) {
                Double dQty = irrigationDao.getTotalWaterQuantityToday();
                todayTotal = (dQty != null ? dQty : 0.0);
            }
            // Today count (sum relates to ALL events today)
            todayCount = irrigationDao.getIrrigationCountOnDate(startOfTodayMillis());
        } catch (Exception ignored) { }

        if (tvWaterUsedToday != null) {
            tvWaterUsedToday.setText(String.format(Locale.getDefault(), "%.1f L", todayTotal));
        }

        // --- Estimated per irrigation ---
        double estimatedPer = 0.0;
        try {
            Double avgUsed = irrigationDao.getAverageWaterUsed();
            estimatedPer = (avgUsed != null ? avgUsed : 0.0);
            if (estimatedPer <= 0.0) {
                Double avgQty = irrigationDao.getAverageWaterQuantity();
                estimatedPer = (avgQty != null ? avgQty : 0.0);
            }
        } catch (Exception ignored) { }

        if (tvEstimatedPerIrrigation != null) {
            tvEstimatedPerIrrigation.setText(String.format(Locale.getDefault(), "%.1f L", estimatedPer));
        }

        // --- Simple benefit indicators (text-only) ---
        int savedPct = manualModeUi ? 0 : 25;
        if (tvWaterSaved != null) {
            tvWaterSaved.setText("💡 Saved: ~" + savedPct + "% • " + todayCount + " event(s) today");
        }
        if (tvEfficiency != null) {
            tvEfficiency.setText((!manualModeUi && todayTotal > 0) ? "✅ Efficient" : "ℹ️ Monitor");
        }

        // --- Selected day details ---
        if (selectedDateMillis <= 0) {
            selectedDateMillis = startOfTodayMillis();
        }
        String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(selectedDateMillis));
        if (tvSelectedDate != null) tvSelectedDate.setText("Selected: " + dateStr);

        double dayTotal = 0.0;
        int dayCount = 0;
        List<IrrigationEntity> dayEvents = null;
        try {
            Double d = irrigationDao.getTotalWaterUsedOnDate(selectedDateMillis);
            dayTotal = d != null ? d : 0.0;
            dayCount = irrigationDao.getIrrigationCountOnDate(selectedDateMillis);
            dayEvents = irrigationDao.getIrrigationsByDate(selectedDateMillis);
            // Fallback on older rows if waterUsed isn't populated
            if (dayTotal <= 0.0 && dayCount > 0) {
                Double dq = irrigationDao.getTotalWaterQuantityOnDate(selectedDateMillis);
                dayTotal = dq != null ? dq : 0.0;
            }
        } catch (Exception ignored) { }

        if (tvDateWaterUsage != null) {
            if (dayCount > 0) {
                tvDateWaterUsage.setText(String.format(Locale.getDefault(),
                        "✅ Irrigation day • %d event(s) • %.1f L total", dayCount, dayTotal));
            } else {
                tvDateWaterUsage.setText("No irrigation events on this day");
            }
        }

        if (tvDateEvents != null) {
            if (dayEvents == null || dayEvents.isEmpty()) {
                tvDateEvents.setText("");
            } else {
                StringBuilder sb = new StringBuilder();
                int limit = Math.min(dayEvents.size(), 5);
                for (int i = 0; i < limit; i++) {
                    IrrigationEntity e = dayEvents.get(i);
                    String name = (e.getTerrainName() != null && !e.getTerrainName().trim().isEmpty()) ? e.getTerrainName().trim() : "Terrain";
                    // If older rows have waterUsed=0, show waterQuantity instead
                    double liters = e.getWaterUsed() > 0 ? e.getWaterUsed() : e.getWaterQuantity();
                    sb.append("• ").append(name)
                      .append(" — ")
                      .append(String.format(Locale.getDefault(), "%.1f L", liters))
                      .append("\n");
                }
                if (dayEvents.size() > limit) sb.append("… +").append(dayEvents.size() - limit).append(" more");
                tvDateEvents.setText(sb.toString().trim());
            }
        }
    }

    private long startOfTodayMillis() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long startOfDayMillis(long anyMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(anyMillis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long endOfDayMillis(long anyMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startOfDayMillis(anyMillis));
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.MILLISECOND, -1);
        return c.getTimeInMillis();
    }
}
