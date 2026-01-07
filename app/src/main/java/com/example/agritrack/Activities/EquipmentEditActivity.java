package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.Calendar;
import java.util.Locale;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_edit);

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
