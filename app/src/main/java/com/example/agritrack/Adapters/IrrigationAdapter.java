package com.example.agritrack.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Database.IrrigationDao;
import com.example.agritrack.Database.IrrigationEntity;
import com.example.agritrack.R;
import com.example.agritrack.Utils.Esp32RtdbClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class IrrigationAdapter extends RecyclerView.Adapter<IrrigationAdapter.ViewHolder> {

    private final Context context;
    private final IrrigationDao irrigationDao;
    private final List<IrrigationEntity> items = new ArrayList<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    // Canonical single zone key (ignore terrainName for control)
    private static final String ZONE1_KEY = "zone1";

    // ESP32 pin lists (as requested)
    private static final Integer[] SENSOR_PINS = new Integer[]{12, 13, 14, 15, 16};
    private static final Integer[] ACTUATOR_PINS = new Integer[]{17, 18, 19, 21, 22, 23, 25};

    // Keep a single threshold default for quick manual actions
    private static final int DEFAULT_THRESHOLD = 1500;

    private static final String DB_BASE = "https://agritrack-48076-default-rtdb.firebaseio.com";
    private static final String AUTH = "MDIMJUvH7ZZsputknLwJdCvcgsbRJbf0BTGnEQqq";
    private static final OkHttpClient http = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // live zone state (updated by activity poller)
    private volatile boolean zoneEnabled = false;
    private volatile boolean zoneLedOn = false;

    public void updateZoneState(boolean enabled, boolean ledOn) {
        this.zoneEnabled = enabled;
        this.zoneLedOn = ledOn;
        // refresh visible items
        ((android.app.Activity) context).runOnUiThread(this::notifyDataSetChanged);
    }

    public IrrigationAdapter(Context context, IrrigationDao irrigationDao) {
        this.context = context;
        this.irrigationDao = irrigationDao;
    }

    public void submitList(List<IrrigationEntity> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_irrigation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IrrigationEntity item = items.get(position);

        holder.txtTerrainName.setText(nonEmpty(item.getTerrainName(), "Terrain"));
        holder.txtDetails.setText(nonEmpty(item.getMethod(), "-") + " • " + item.getWaterQuantity() + "L");
        holder.txtStatus.setText(nonEmpty(item.getStatus(), "-"));

        Date date = item.getIrrigationDate();
        holder.txtDate.setText(date != null ? dateFormat.format(date) : "-");

        // Real-time indicator from zone state
        if (!zoneEnabled) {
            holder.txtMode.setText("⛔ Disabled");
            holder.txtMode.setTextColor(context.getResources().getColor(android.R.color.white));
            holder.txtMode.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.layoutManualControls.setVisibility(View.GONE);
        } else if (zoneLedOn) {
            holder.txtMode.setText("💧 Irrigating");
            holder.txtMode.setTextColor(context.getResources().getColor(android.R.color.white));
            holder.txtMode.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
            holder.layoutManualControls.setVisibility(View.VISIBLE);
        } else {
            holder.txtMode.setText("⏸️ Stopped");
            holder.txtMode.setTextColor(context.getResources().getColor(android.R.color.white));
            holder.txtMode.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            holder.layoutManualControls.setVisibility(View.VISIBLE);
        }

        // Optional: make indicator more visible via small description text (if present in layout)
        if (holder.txtIrrigationState != null) {
            if (!zoneEnabled) {
                holder.txtIrrigationState.setText("Disabled");
                holder.txtIrrigationState.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            } else if (zoneLedOn) {
                holder.txtIrrigationState.setText("Running");
                holder.txtIrrigationState.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                holder.txtIrrigationState.setText("Idle");
                holder.txtIrrigationState.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            }
        }

        // Manual ON button -> write to /irrigation/zone1
        holder.btnManualOn.setOnClickListener(v -> {
            if (!zoneEnabled) {
                Toast.makeText(context, "Zone disabled in Firebase (enabled=false)", Toast.LENGTH_SHORT).show();
            }
            Esp32RtdbClient.upsertZoneConfig(ZONE1_KEY, "manual", true, DEFAULT_THRESHOLD, null);
            patchZone1LedOn(true); // ensure ESP32 sees ledOn=true
            Toast.makeText(context, "LED ON sent", Toast.LENGTH_SHORT).show();
        });

        // Manual OFF button -> write to /irrigation/zone1
        holder.btnManualOff.setOnClickListener(v -> {
            Esp32RtdbClient.upsertZoneConfig(ZONE1_KEY, "manual", false, DEFAULT_THRESHOLD, null);
            patchZone1LedOn(false); // ensure ESP32 sees ledOn=false
            Toast.makeText(context, "LED OFF sent", Toast.LENGTH_SHORT).show();
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Supprimer")
                    .setMessage("Supprimer cette irrigation ?")
                    .setPositiveButton("Supprimer", (d, which) -> {
                        irrigationDao.delete(item);
                        // Safety: ensure ESP32 isn't left running
                        Esp32RtdbClient.setManualOff(ZONE1_KEY, null);
                        int idx = holder.getAdapterPosition();
                        if (idx >= 0 && idx < items.size()) {
                            items.remove(idx);
                            notifyItemRemoved(idx);
                        } else {
                            notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });

        holder.btnEdit.setOnClickListener(v -> showEditDialog(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTerrainName;
        TextView txtDetails;
        TextView txtStatus;
        TextView txtDate;
        TextView txtMode;
        TextView txtIrrigationState;
        ImageButton btnEdit;
        ImageButton btnDelete;
        LinearLayout layoutManualControls;
        Button btnManualOn;
        Button btnManualOff;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTerrainName = itemView.findViewById(R.id.txtTerrainName);
            txtDetails = itemView.findViewById(R.id.txtDetails);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtMode = itemView.findViewById(R.id.txtMode);
            txtIrrigationState = itemView.findViewById(R.id.txtIrrigationState);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            layoutManualControls = itemView.findViewById(R.id.layoutManualControls);
            btnManualOn = itemView.findViewById(R.id.btnManualOn);
            btnManualOff = itemView.findViewById(R.id.btnManualOff);
        }
    }

    private void showEditDialog(IrrigationEntity irrigation) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.layout_add_irrigation, null);

        EditText editTerrain = dialogView.findViewById(R.id.editTerrainName);
        EditText editQuantity = dialogView.findViewById(R.id.editQuantity);
        Spinner spinnerMode = dialogView.findViewById(R.id.spinnerMode);
        SwitchCompat switchManual = dialogView.findViewById(R.id.switchManualState);
        EditText editThreshold = dialogView.findViewById(R.id.editThreshold);
        Spinner spinnerMethod = dialogView.findViewById(R.id.spinnerMethod);
        Spinner spinnerSensorPin = dialogView.findViewById(R.id.spinnerSensorPin);
        Spinner spinnerActuatorPin = dialogView.findViewById(R.id.spinnerActuatorPin);
        View btnSave = dialogView.findViewById(R.id.btnSave);
        if (btnSave != null) btnSave.setVisibility(View.GONE);

        String[] modes = new String[]{"auto", "manual"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, modes);
        spinnerMode.setAdapter(modeAdapter);
        spinnerMode.setSelection(0);

        // FIX: don't force a false manualState by default; only disable it in auto mode
        switchManual.setChecked(false);
        switchManual.setEnabled("manual".equalsIgnoreCase(String.valueOf(spinnerMode.getSelectedItem())));

        // FIX: ensure threshold field is not left empty/disabled state surprises
        editThreshold.setText(String.valueOf(DEFAULT_THRESHOLD));

        spinnerMode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                boolean isManual = "manual".equalsIgnoreCase(String.valueOf(spinnerMode.getSelectedItem()));
                switchManual.setEnabled(isManual);
                if (!isManual) {
                    switchManual.setChecked(false);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        String[] methods = new String[]{"Goutte-à-goutte", "Aspersion", "Arrosage manuel"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, methods);
        spinnerMethod.setAdapter(adapter);

        // Sensor pin spinner
        if (spinnerSensorPin != null) {
            ArrayAdapter<Integer> sensorAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, SENSOR_PINS);
            spinnerSensorPin.setAdapter(sensorAdapter);

            // Preselect saved value (fallback to first)
            int current = irrigation.getSensorPin();
            int idx = 0;
            for (int i = 0; i < SENSOR_PINS.length; i++) {
                if (SENSOR_PINS[i] != null && SENSOR_PINS[i] == current) { idx = i; break; }
            }
            spinnerSensorPin.setSelection(idx);
        }

        // Actuator pin spinner
        if (spinnerActuatorPin != null) {
            ArrayAdapter<Integer> actuatorAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, ACTUATOR_PINS);
            spinnerActuatorPin.setAdapter(actuatorAdapter);

            int current = irrigation.getActuatorPin();
            int idx = 0;
            for (int i = 0; i < ACTUATOR_PINS.length; i++) {
                if (ACTUATOR_PINS[i] != null && ACTUATOR_PINS[i] == current) { idx = i; break; }
            }
            spinnerActuatorPin.setSelection(idx);
        }

        editTerrain.setText(nonEmpty(irrigation.getTerrainName(), ""));
        editQuantity.setText(String.valueOf(irrigation.getWaterQuantity()));

        int methodIndex = 0;
        if (!TextUtils.isEmpty(irrigation.getMethod())) {
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].equalsIgnoreCase(irrigation.getMethod())) {
                    methodIndex = i;
                    break;
                }
            }
        }
        spinnerMethod.setSelection(methodIndex);

        new AlertDialog.Builder(context)
                .setTitle("Modifier Irrigation")
                .setView(dialogView)
                .setPositiveButton("Enregistrer", (d, which) -> {
                    irrigation.setTerrainName(safeTrim(editTerrain.getText().toString()));
                    irrigation.setWaterQuantity(parseDouble(editQuantity.getText().toString(), irrigation.getWaterQuantity()));
                    irrigation.setMethod(String.valueOf(spinnerMethod.getSelectedItem()));

                    // Keep waterUsed consistent for stats (fallback estimate = waterQuantity)
                    irrigation.setWaterUsed(irrigation.getWaterQuantity());

                    // Persist selected pins
                    Integer sensorPin = spinnerSensorPin != null ? (Integer) spinnerSensorPin.getSelectedItem() : null;
                    Integer actuatorPin = spinnerActuatorPin != null ? (Integer) spinnerActuatorPin.getSelectedItem() : null;
                    irrigation.setSensorPin(sensorPin != null ? sensorPin : irrigation.getSensorPin());
                    irrigation.setActuatorPin(actuatorPin != null ? actuatorPin : irrigation.getActuatorPin());

                    irrigationDao.update(irrigation);

                    String mode = spinnerMode.getSelectedItem() != null ? String.valueOf(spinnerMode.getSelectedItem()) : "auto";
                    boolean manualState = switchManual.isChecked();
                    int threshold = parseInt(editThreshold.getText().toString(), DEFAULT_THRESHOLD);

                    // FIX: always configure the single zone in Firebase (mode/manualState must be accurate)
                    Esp32RtdbClient.upsertZoneConfig(ZONE1_KEY, mode, manualState, threshold, null);

                    Toast.makeText(context, "Configuration mise à jour", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private static String nonEmpty(String value, String fallback) {
        if (TextUtils.isEmpty(value)) return fallback;
        return value;
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

    private static void patchZone1LedOn(boolean ledOn) {
        String url = DB_BASE + "/irrigation/zone1.json?auth=" + AUTH;
        String bodyJson = "{\"enabled\":true,\"ledOn\":" + (ledOn ? "true" : "false") + "}";
        Request req = new Request.Builder()
                .url(url)
                .patch(RequestBody.create(bodyJson, JSON))
                .build();
        http.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) { /* ignore */ }
            @Override public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) { response.close(); }
        });
    }
}
