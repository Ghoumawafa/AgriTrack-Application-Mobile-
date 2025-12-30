// java
package com.example.agritrack.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Adapters.IrrigationAdapter;
import com.example.agritrack.Database.AppDatabase;
import com.example.agritrack.Models.Irrigation;
import com.example.agritrack.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IrrigationActivity extends AppCompatActivity {
    private static final String TAG = "IrrigationActivity";

    private RecyclerView recyclerView;
    private IrrigationAdapter adapter;
    private List<Irrigation> irrigationList = new ArrayList<>();
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference zonesRef;     // /irrigation/zones
    private ChildEventListener zonesListener; // Keep reference to detach in onDestroy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irrigation);

        try {
            // Toolbar with back arrow and long-press mode dialog
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Irrigation");
                }
                toolbar.setNavigationOnClickListener(v -> onBackPressed());
                toolbar.setOnLongClickListener(v -> {
                    showModeDialog(); // global mode dialog (keeps legacy)
                    return true;
                });
            } else {
                setTitle("Irrigation");
            }

            // Local DB + RecyclerView + Adapter
            db = AppDatabase.getInstance(this);
            recyclerView = findViewById(R.id.recyclerViewIrrigation);
            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                adapter = new IrrigationAdapter(this, irrigationList, new IrrigationAdapter.OnItemActionListener() {
                    @Override
                    public void onEdit(Irrigation irrigation) {
                        showAddIrrigationDialog(irrigation);
                    }

                    @Override
                    public void onDelete(Irrigation irrigation) {
                        confirmDelete(irrigation);
                    }

                    @Override
                    public void onToggleMode(Irrigation irrigation) {
                        // toggle between "auto" and "manual"
                        String newMode = "auto".equalsIgnoreCase(irrigation.getMode()) ? "manual" : "auto";
                        writeModeToRemote(irrigation, newMode);
                    }

                    @Override
                    public void onToggleManual(Irrigation irrigation) {
                        boolean newState = !irrigation.isManualState();
                        writeManualStateToRemote(irrigation, newState);
                    }

                    @Override
                    public void onConfigure(Irrigation irrigation) {
                        showConfigureDialog(irrigation);
                    }
                });
                recyclerView.setAdapter(adapter);
            }

            FloatingActionButton fab = findViewById(R.id.fab_add_irrigation);
            if (fab != null) fab.setOnClickListener(v -> showAddIrrigationDialog(null));

            // Bottom navigation safe wiring (reuses menu from Accueil)
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                // mark irrigation menu item checked when title contains "irrig"
                for (int i = 0; i < bottomNav.getMenu().size(); i++) {
                    MenuItem mi = bottomNav.getMenu().getItem(i);
                    String title = mi.getTitle() != null ? mi.getTitle().toString().toLowerCase(Locale.ROOT) : "";
                    if (title.contains("irrig")) {
                        mi.setChecked(true);
                        break;
                    }
                }
                bottomNav.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    try {
                        if (id == R.id.nav_home) {
                            startActivity(new Intent(IrrigationActivity.this, AccueilActivity.class));
                            return true;
                        } else if (id == R.id.nav_notifications) {
                            startActivity(new Intent(IrrigationActivity.this, NotificationsActivity.class));
                            return true;
                        } else if (id == R.id.nav_profile) {
                            startActivity(new Intent(IrrigationActivity.this, ProfileActivity.class));
                            return true;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Navigation failed", e);
                        Toast.makeText(IrrigationActivity.this, "Impossible d'ouvrir la page demandée", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                });
            }

            // Firebase initialization and zonesRef
            try {
                if (FirebaseApp.getApps(this).isEmpty()) FirebaseApp.initializeApp(this);
                firebaseDatabase = FirebaseDatabase.getInstance("https://agritrack-48076-default-rtdb.firebaseio.com/");
                zonesRef = firebaseDatabase.getReference("irrigation").child("zones");
                attachZonesListener();
            } catch (Exception e) {
                Log.w(TAG, "Firebase init failed (app will continue local-only)", e);
                firebaseDatabase = null;
                zonesRef = null;
            }

            // initial local load
            loadData();

        } catch (Exception ex) {
            Log.e(TAG, "onCreate failed", ex);
            Toast.makeText(this, "Erreur interne — impossible d'ouvrir Irrigation", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Keep existing loadData() but ensure it refreshes UI
    private void loadData() {
        executor.execute(() -> {
            List<Irrigation> data = new ArrayList<>();
            try {
                List<Irrigation> fromDb = db.irrigationDao().getAllIrrigations();
                if (fromDb != null) data.addAll(fromDb);
            } catch (Exception e) {
                Log.w(TAG, "Failed to read irrigations", e);
            }
            runOnUiThread(() -> {
                irrigationList.clear();
                irrigationList.addAll(data);
                if (adapter != null) adapter.notifyDataSetChanged();
            });
        });
    }

    // Attach ChildEventListener on /irrigation/zones to reflect remote changes
    private void attachZonesListener() {
        if (zonesRef == null) return;

        // Create and store listener reference for proper cleanup
        zonesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                syncZoneFromSnapshot(snapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                syncZoneFromSnapshot(snapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // mark local irrigation hardware disabled if a remote node removed
                String key = snapshot.getKey();
                if (key == null) return;
                final String finalKey = key;
                executor.execute(() -> {
                    try {
                        List<Irrigation> all = db.irrigationDao().getAllIrrigations();
                        if (all == null) return;
                        for (Irrigation irr : all) {
                            if (finalKey.equals(irr.getRemoteKey())) {
                                irr.setHardwareEnabled(false);
                                irr.setRemoteKey(null);
                                db.irrigationDao().update(irr);
                                runOnUiThread(() -> loadData());
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling child removal", e);
                    }
                });
            }

            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "zones listener cancelled: " + error.getMessage());
                runOnUiThread(() -> Toast.makeText(IrrigationActivity.this,
                    "Connexion Firebase interrompue", Toast.LENGTH_SHORT).show());
            }
        };

        zonesRef.addChildEventListener(zonesListener);
    }

    // sync remote zone snapshot into local Room (match by remoteKey)
    private void syncZoneFromSnapshot(@NonNull DataSnapshot snapshot) {
        try {
            String key = snapshot.getKey();
            if (key == null) return;
            String mode = snapshot.child("mode").getValue(String.class);
            Boolean manualState = snapshot.child("manualState").getValue(Boolean.class);
            Double sensorValue = null;
            try { sensorValue = snapshot.child("sensorValue").getValue(Double.class); } catch (Exception ignored){}
            // hardware child
            DataSnapshot hw = snapshot.child("hardware");
            Integer pumpPin = null, sensorPin = null;
            Boolean hwEnabled = null;
            if (hw != null) {
                if (hw.child("pumpMotor").exists()) {
                    pumpPin = hw.child("pumpMotor").child("pin").getValue(Integer.class);
                    hwEnabled = hw.child("pumpMotor").child("enabled").getValue(Boolean.class);
                }
                if (hw.child("soilSensor").exists()) {
                    sensorPin = hw.child("soilSensor").child("pin").getValue(Integer.class);
                    // hwEnabled may be separate for sensors; ignore for now
                }
            }

            // ================= FIX START =================
            // Create final copies of the local variables to use them inside the lambda.
            final String finalRemoteKey = key;
            final String finalMode = mode;
            final Boolean finalManualState = manualState;
            final Double finalSensorValue = sensorValue;
            final Integer finalPumpPin = pumpPin;
            final Integer finalSensorPin = sensorPin;
            final Boolean finalHwEnabled = hwEnabled;
            // ================= FIX END =================

            executor.execute(() -> {
                List<Irrigation> all = db.irrigationDao().getAllIrrigations();
                if (all == null) return;
                for (Irrigation irr : all) {
                    if (finalRemoteKey.equals(irr.getRemoteKey())) {
                        // Now use the final copies inside the lambda
                        if (finalMode != null) irr.setMode(finalMode);
                        if (finalManualState != null) irr.setManualState(finalManualState);
                        if (finalSensorValue != null) irr.setSensorValue(finalSensorValue);
                        if (finalPumpPin != null) irr.setPumpPin(finalPumpPin);
                        if (finalSensorPin != null) irr.setSensorPin(finalSensorPin);
                        if (finalHwEnabled != null) irr.setHardwareEnabled(finalHwEnabled);

                        db.irrigationDao().update(irr);
                        runOnUiThread(this::loadData);
                        return; // Use return instead of break for clarity
                    }
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "syncZoneFromSnapshot failed", e);
        }
    }

    // write mode to remote node; if no remoteKey, push a new zone and store its key locally
    private void writeModeToRemote(Irrigation irrigation, String mode) {
        if (zonesRef == null) {
            Toast.makeText(this, "Firebase non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        if (irrigation == null || mode == null || mode.isEmpty()) {
            Log.w(TAG, "Invalid parameters for writeModeToRemote");
            return;
        }

        try {
            if (irrigation.getRemoteKey() == null) {
                // create remote node
                DatabaseReference newRef = zonesRef.push();
                String key = newRef.getKey();
                if (key == null) {
                    Log.e(TAG, "Failed to generate Firebase key");
                    return;
                }

                // create minimal remote payload with error handling
                newRef.child("mode").setValue(mode)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Mode set successfully for new zone: " + key))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to set mode", e);
                        runOnUiThread(() -> Toast.makeText(this, "Erreur Firebase", Toast.LENGTH_SHORT).show());
                    });

                newRef.child("manualState").setValue(irrigation.isManualState());

                // hardware child
                newRef.child("hardware").child("pumpMotor").child("pin").setValue(irrigation.getPumpPin());
                newRef.child("hardware").child("pumpMotor").child("enabled").setValue(irrigation.isHardwareEnabled());

                // set remoteKey locally
                irrigation.setRemoteKey(key);
                irrigation.setMode(mode);
                executor.execute(() -> {
                    try {
                        db.irrigationDao().update(irrigation);
                        runOnUiThread(this::loadData);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to update local DB", e);
                    }
                });
            } else {
                // Update existing node
                zonesRef.child(irrigation.getRemoteKey()).child("mode").setValue(mode)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Mode updated successfully"))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update mode", e);
                        runOnUiThread(() -> Toast.makeText(this, "Erreur mise à jour Firebase", Toast.LENGTH_SHORT).show());
                    });

                irrigation.setMode(mode);
                executor.execute(() -> {
                    try {
                        db.irrigationDao().update(irrigation);
                        runOnUiThread(this::loadData);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to update local DB", e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "writeModeToRemote failed", e);
            Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeManualStateToRemote(Irrigation irrigation, boolean state) {
        if (zonesRef == null) {
            Toast.makeText(this, "Remote unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (irrigation.getRemoteKey() == null) {
                // push minimal node
                DatabaseReference newRef = zonesRef.push();
                String key = newRef.getKey();
                if (key == null) return;
                newRef.child("mode").setValue("manual");
                newRef.child("manualState").setValue(state);
                irrigation.setRemoteKey(key);
                irrigation.setMode("manual");
                irrigation.setManualState(state);
                executor.execute(() -> db.irrigationDao().update(irrigation));
                runOnUiThread(this::loadData);
            } else {
                zonesRef.child(irrigation.getRemoteKey()).child("manualState").setValue(state);
                zonesRef.child(irrigation.getRemoteKey()).child("mode").setValue(state ? "manual" : irrigation.getMode());
                irrigation.setManualState(state);
                executor.execute(() -> db.irrigationDao().update(irrigation));
                runOnUiThread(this::loadData);
            }
        } catch (Exception e) {
            Log.w(TAG, "writeManualStateToRemote failed", e);
            Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show();
        }
    }

    // Configure pins dialog
    private void showConfigureDialog(Irrigation irrigation) {
        // simple dialog with two numeric inputs and a switch
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText pumpPinInput = new EditText(this);
        pumpPinInput.setHint("Pump pin (e.g. 26)");
        pumpPinInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (irrigation.getPumpPin() > 0) pumpPinInput.setText(String.valueOf(irrigation.getPumpPin()));
        final EditText sensorPinInput = new EditText(this);
        sensorPinInput.setHint("Sensor pin (e.g. 34)");
        sensorPinInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (irrigation.getSensorPin() > 0) sensorPinInput.setText(String.valueOf(irrigation.getSensorPin()));
        final Switch enabledSwitch = new Switch(this);
        enabledSwitch.setText("Hardware enabled");
        enabledSwitch.setChecked(irrigation.isHardwareEnabled());

        layout.addView(pumpPinInput);
        layout.addView(sensorPinInput);
        layout.addView(enabledSwitch);

        new AlertDialog.Builder(this)
                .setTitle("Configure hardware")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    int pumpPin = -1, sensorPin = -1;
                    try { pumpPin = Integer.parseInt(pumpPinInput.getText().toString().trim()); } catch (Exception ignored){}
                    try { sensorPin = Integer.parseInt(sensorPinInput.getText().toString().trim()); } catch (Exception ignored){}
                    boolean enabled = enabledSwitch.isChecked();
                    applyHardwareConfigRemote(irrigation, pumpPin, sensorPin, enabled);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // write hardware config under zonesRef/<remoteKey>/hardware
    private void applyHardwareConfigRemote(Irrigation irrigation, int pumpPin, int sensorPin, boolean enabled) {
        if (zonesRef == null) {
            Toast.makeText(this, "Remote unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (irrigation.getRemoteKey() == null) {
                DatabaseReference newRef = zonesRef.push();
                String key = newRef.getKey();
                if (key == null) return;
                newRef.child("mode").setValue(irrigation.getMode());
                newRef.child("manualState").setValue(irrigation.isManualState());
                newRef.child("hardware").child("pumpMotor").child("pin").setValue(pumpPin);
                newRef.child("hardware").child("pumpMotor").child("enabled").setValue(enabled);
                newRef.child("hardware").child("soilSensor").child("pin").setValue(sensorPin);
                irrigation.setRemoteKey(key);
                irrigation.setPumpPin(pumpPin);
                irrigation.setSensorPin(sensorPin);
                irrigation.setHardwareEnabled(enabled);
                executor.execute(() -> db.irrigationDao().update(irrigation));
                runOnUiThread(this::loadData);
            } else {
                DatabaseReference hwRef = zonesRef.child(irrigation.getRemoteKey()).child("hardware");
                hwRef.child("pumpMotor").child("pin").setValue(pumpPin);
                hwRef.child("pumpMotor").child("enabled").setValue(enabled);
                hwRef.child("soilSensor").child("pin").setValue(sensorPin);
                irrigation.setPumpPin(pumpPin);
                irrigation.setSensorPin(sensorPin);
                irrigation.setHardwareEnabled(enabled);
                executor.execute(() -> db.irrigationDao().update(irrigation));
                runOnUiThread(this::loadData);
            }
        } catch (Exception e) {
            Log.w(TAG, "applyHardwareConfigRemote failed", e);
        }
    }

    private void confirmDelete(Irrigation irrigation) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Supprimer cette irrigation ?")
                .setPositiveButton("Supprimer", (d, w) -> {
                    executor.execute(() -> {
                        try {
                            // delete remote node if exists
                            if (irrigation.getRemoteKey() != null && zonesRef != null) {
                                zonesRef.child(irrigation.getRemoteKey()).removeValue();
                            }
                            db.irrigationDao().delete(irrigation);
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(IrrigationActivity.this, "Erreur suppression", Toast.LENGTH_SHORT).show());
                            Log.w(TAG, "delete failed", e);
                            return;
                        }
                        runOnUiThread(() -> {
                            loadData();
                            Toast.makeText(IrrigationActivity.this, "Irrigation supprimée", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // showAddIrrigationDialog: on create, push remote and save remoteKey to local row
    private void showAddIrrigationDialog(Irrigation irrigationToEdit) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_add_irrigation, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.setCanceledOnTouchOutside(true);

        EditText editTerrain = view.findViewById(R.id.editTerrainName);
        EditText editQuantity = view.findViewById(R.id.editQuantity);
        Spinner spinnerMethod = view.findViewById(R.id.spinnerMethod);
        Button btnSave = view.findViewById(R.id.btnSave);

        String[] methods = new String[]{"Goutte-à-goutte", "Aspersion", "Inondation", "Autre"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, methods);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerMethod != null) spinnerMethod.setAdapter(adapterSpinner);

        final boolean isEdit = (irrigationToEdit != null);
        if (isEdit) {
            if (editTerrain != null) editTerrain.setText(irrigationToEdit.getTerrainName());
            if (editQuantity != null) editQuantity.setText(String.valueOf(irrigationToEdit.getWaterQuantity()));
            if (spinnerMethod != null) {
                int pos = adapterSpinner.getPosition(irrigationToEdit.getMethod());
                spinnerMethod.setSelection(pos >= 0 ? pos : 0);
            }
            if (btnSave != null) btnSave.setText("Mettre à jour");
        } else {
            if (btnSave != null) btnSave.setText("Enregistrer");
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                btnSave.setEnabled(false);
                btnSave.setText(isEdit ? "Mise à jour..." : "Enregistrement...");

                String name = editTerrain != null ? editTerrain.getText().toString().trim() : "";
                String qtyStr = editQuantity != null ? editQuantity.getText().toString().trim() : "";
                String method = (spinnerMethod != null && spinnerMethod.getSelectedItem() != null)
                        ? spinnerMethod.getSelectedItem().toString()
                        : "";

                if (name.isEmpty() || qtyStr.isEmpty()) {
                    Toast.makeText(this, "Veuillez remplir les champs", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText(isEdit ? "Mettre à jour" : "Enregistrer");
                    return;
                }

                double qty;
                try {
                    qty = Double.parseDouble(qtyStr);
                    if (qty < 0) throw new NumberFormatException("Negative");
                } catch (NumberFormatException ex) {
                    Toast.makeText(this, "Quantité invalide", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText(isEdit ? "Mettre à jour" : "Enregistrer");
                    return;
                }

                final String finalName = name;
                final double finalQty = qty;
                final String finalMethod = method.isEmpty() ? "N/A" : method;

                executor.execute(() -> {
                    try {
                        if (isEdit) {
                            irrigationToEdit.setTerrainName(finalName);
                            irrigationToEdit.setWaterQuantity(finalQty);
                            irrigationToEdit.setMethod(finalMethod);
                            db.irrigationDao().update(irrigationToEdit);
                        } else {
                            Irrigation newIrrigation = new Irrigation(finalName, new Date(), finalQty, finalMethod);
                            long newRowId = db.irrigationDao().insert(newIrrigation);
                            newIrrigation.setId((int)newRowId);

                            if (zonesRef != null) {
                                DatabaseReference newRef = zonesRef.push();
                                String key = newRef.getKey();
                                if (key != null) {
                                    newRef.child("mode").setValue(newIrrigation.getMode());
                                    newRef.child("manualState").setValue(newIrrigation.isManualState());
                                    newRef.child("sensorValue").setValue(newIrrigation.getSensorValue());
                                    newRef.child("hardware").child("pumpMotor").child("pin").setValue(newIrrigation.getPumpPin());
                                    newRef.child("hardware").child("pumpMotor").child("enabled").setValue(newIrrigation.isHardwareEnabled());
                                    newRef.child("hardware").child("soilSensor").child("pin").setValue(newIrrigation.getSensorPin());
                                    newIrrigation.setRemoteKey(key);
                                    db.irrigationDao().update(newIrrigation);
                                }
                            }
                         }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(IrrigationActivity.this, "Erreur lors de l'opération", Toast.LENGTH_SHORT).show();
                            btnSave.setEnabled(true);
                            btnSave.setText(isEdit ? "Mettre à jour" : "Enregistrer");
                        });
                        Log.w(TAG, "save irrigation failed", e);
                        return;
                    }

                    runOnUiThread(() -> {
                        hideKeyboard(view);
                        loadData();
                        bottomSheetDialog.dismiss();
                        Toast.makeText(IrrigationActivity.this, isEdit ? "Irrigation mise à jour !" : "Irrigation enregistrée !", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        }

        bottomSheetDialog.setOnDismissListener(dialog -> {
            View btn = view.findViewById(R.id.btnSave);
            if (btn != null) {
                btn.setEnabled(true);
                if (btn instanceof Button) ((Button) btn).setText(isEdit ? "Mettre à jour" : "Enregistrer");
            }
        });

        bottomSheetDialog.show();
    }

    // showModeDialog kept for backward compatibility (global)
    private void showModeDialog() {
        Toast.makeText(this, "Long-press mode dialog (per-zone controls available on each item)", Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && view != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Detach Firebase listener to prevent memory leaks
        if (zonesRef != null && zonesListener != null) {
            zonesRef.removeEventListener(zonesListener);
            zonesListener = null;
        }

        // Shutdown executor service
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
