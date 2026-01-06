package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.EquipmentDao;
import com.example.agritrack.Database.EquipmentEntity;
import com.example.agritrack.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EquipmentListActivity extends AppCompatActivity {

    public static final String EXTRA_EQUIPMENT_ID = "equipment_id";

    private EquipmentDao equipmentDao;
    private List<EquipmentEntity> equipments = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private EditText inputSearch;
    private Spinner spinnerType;
    private Button btnFromDate, btnToDate, btnFilter, btnReset;
    private String fromDateStr = "", toDateStr = "";
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_list);

        equipmentDao = AgriTrackRoomDatabase.getInstance(this).equipmentDao();

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnAdd = findViewById(R.id.btnAddEquipment);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, EquipmentEditActivity.class);
            startActivity(intent);
        });

        ListView listView = findViewById(R.id.equipmentListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

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

        listView.setOnItemClickListener((parent, view, position, id) -> {
            EquipmentEntity equipment = equipments.get(position);
            Intent intent = new Intent(this, EquipmentEditActivity.class);
            intent.putExtra(EXTRA_EQUIPMENT_ID, equipment.getId());
            startActivity(intent);
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
        List<String> rows = new ArrayList<>();
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
                String row = String.format(Locale.getDefault(), "%s — %s — %s",
                        e.getName(),
                        e.getType(),
                        e.getStatus());
                rows.add(row);
            }
        }

        adapter.clear();
        adapter.addAll(rows);
        adapter.notifyDataSetChanged();
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
