// java
package com.example.agritrack.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IrrigationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private IrrigationAdapter adapter;
    private List<Irrigation> irrigationList = new ArrayList<>();
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irrigation);

        // set up toolbar as AppBar with back arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // show back arrow
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Irrigation");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = AppDatabase.getInstance(this);
        recyclerView = findViewById(R.id.recyclerViewIrrigation);
        FloatingActionButton fab = findViewById(R.id.fab_add_irrigation);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // create adapter with listener
        adapter = new IrrigationAdapter(this, irrigationList, new IrrigationAdapter.OnItemActionListener() {
            @Override
            public void onEdit(Irrigation irrigation) {
                // show bottom sheet prefilled for editing
                showAddIrrigationDialog(irrigation);
            }

            @Override
            public void onDelete(Irrigation irrigation) {
                // confirm then delete
                new AlertDialog.Builder(IrrigationActivity.this)
                        .setTitle("Supprimer")
                        .setMessage("Voulez-vous supprimer cette irrigation ?")
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            executor.execute(() -> {
                                try {
                                    db.irrigationDao().delete(irrigation);
                                } catch (Exception e) {
                                    runOnUiThread(() -> Toast.makeText(IrrigationActivity.this, "Erreur suppression", Toast.LENGTH_SHORT).show());
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
        });
        recyclerView.setAdapter(adapter);

        loadData();

        fab.setOnClickListener(v -> showAddIrrigationDialog(null));

        // --- bottom navigation handling (reuse same menu & behavior as Accueil) ---
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            // try to mark the Irrigation item checked by matching title text (safe if there is no specific nav_irrigation id)
            for (int i = 0; i < bottomNav.getMenu().size(); i++) {
                MenuItem mi = bottomNav.getMenu().getItem(i);
                String title = mi.getTitle() != null ? mi.getTitle().toString().toLowerCase(Locale.ROOT) : "";
                if (title.contains("irrig")) {
                    mi.setChecked(true);
                    break;
                }
            }

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    // go to Accueil (Home)
                    startActivity(new Intent(IrrigationActivity.this, AccueilActivity.class));
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    // open Alerts / Notifications (same as Accueil)
                    startActivity(new Intent(IrrigationActivity.this, NotificationsActivity.class));
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // open Profile (same as Accueil)
                    startActivity(new Intent(IrrigationActivity.this, ProfileActivity.class));
                    return true;
                }
                // unknown item: do nothing
                return false;
            });
        }
    }

    private void loadData() {
        executor.execute(() -> {
            List<Irrigation> data = null;
            try {
                data = db.irrigationDao().getAllIrrigations();
            } catch (Exception e) {
                // DB read error
            }
            final List<Irrigation> finalData = data;
            runOnUiThread(() -> {
                irrigationList.clear();
                if (finalData != null) irrigationList.addAll(finalData);
                adapter.notifyDataSetChanged();
            });
        });
    }

    // if irrigationToEdit == null => create mode, else edit mode
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

        // populate spinner with options
        String[] methods = new String[] { "Goutte-à-goutte", "Aspersion", "Inondation", "Autre" };
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, methods);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMethod.setAdapter(adapterSpinner);

        final boolean isEdit = (irrigationToEdit != null);
        if (isEdit) {
            // prefill fields
            editTerrain.setText(irrigationToEdit.getTerrainName());
            editQuantity.setText(String.valueOf(irrigationToEdit.getWaterQuantity()));
            // select method if present
            int pos = adapterSpinner.getPosition(irrigationToEdit.getMethod());
            spinnerMethod.setSelection(pos >= 0 ? pos : 0);
            btnSave.setText("Mettre à jour");
        } else {
            btnSave.setText("Enregistrer");
        }

        btnSave.setOnClickListener(v -> {
            // Prevent double clicks
            btnSave.setEnabled(false);
            btnSave.setText(isEdit ? "Mise à jour..." : "Enregistrement...");

            String name = editTerrain.getText().toString().trim();
            String qtyStr = editQuantity.getText().toString().trim();
            String method = spinnerMethod.getSelectedItem() != null ? spinnerMethod.getSelectedItem().toString() : "";

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

            final String finalMethod = method.isEmpty() ? "N/A" : method;

            executor.execute(() -> {
                try {
                    if (isEdit) {
                        // update existing entity
                        irrigationToEdit.setTerrainName(name);
                        irrigationToEdit.setWaterQuantity(qty);
                        irrigationToEdit.setMethod(finalMethod);
                        // keep existing irrigationDate; optionally update date if desired
                        db.irrigationDao().update(irrigationToEdit);
                    } else {
                        Irrigation newIrrigation = new Irrigation(name, new Date(), qty, finalMethod);
                        db.irrigationDao().insert(newIrrigation);
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Erreur lors de l'opération", Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(true);
                        btnSave.setText(isEdit ? "Mettre à jour" : "Enregistrer");
                    });
                    return;
                }

                runOnUiThread(() -> {
                    hideKeyboard(view);
                    loadData();
                    bottomSheetDialog.dismiss();
                    Toast.makeText(this, isEdit ? "Irrigation mise à jour !" : "Irrigation enregistrée !", Toast.LENGTH_SHORT).show();
                });
            });
        });

        bottomSheetDialog.setOnDismissListener(dialog -> {
            // ensure button re-enabled if dialog closed manually
            View btn = view.findViewById(R.id.btnSave);
            if (btn != null) {
                btn.setEnabled(true);
                if (btn instanceof Button) ((Button) btn).setText(isEdit ? "Mettre à jour" : "Enregistrer");
            }
        });

        bottomSheetDialog.show();
    }

    private void hideKeyboard(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception ignored) { }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
