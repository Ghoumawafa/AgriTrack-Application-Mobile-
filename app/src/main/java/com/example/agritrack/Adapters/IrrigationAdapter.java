package com.example.agritrack.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

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

public class IrrigationAdapter extends RecyclerView.Adapter<IrrigationAdapter.ViewHolder> {

    private final Context context;
    private final IrrigationDao irrigationDao;
    private final List<IrrigationEntity> items = new ArrayList<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

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

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Supprimer")
                    .setMessage("Supprimer cette irrigation ?")
                    .setPositiveButton("Supprimer", (d, which) -> {
                        irrigationDao.delete(item);
                        // Safety: ensure ESP32 isn't left running in manual mode
                        Esp32RtdbClient.setManualOff(item.getTerrainName(), null);
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
        ImageButton btnEdit;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTerrainName = itemView.findViewById(R.id.txtTerrainName);
            txtDetails = itemView.findViewById(R.id.txtDetails);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDate = itemView.findViewById(R.id.txtDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
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
        View btnSave = dialogView.findViewById(R.id.btnSave);
        if (btnSave != null) btnSave.setVisibility(View.GONE);

        String[] modes = new String[]{"auto", "manual"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, modes);
        spinnerMode.setAdapter(modeAdapter);
        spinnerMode.setSelection(0);

        switchManual.setChecked(false);
        switchManual.setEnabled(false);

        editThreshold.setText("1500");

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
                    irrigationDao.update(irrigation);

                    String mode = spinnerMode.getSelectedItem() != null ? String.valueOf(spinnerMode.getSelectedItem()) : "auto";
                    boolean manualState = switchManual.isChecked();
                    int threshold = parseInt(editThreshold.getText().toString(), 1500);
                    Esp32RtdbClient.upsertZoneConfig(irrigation.getTerrainName(), mode, manualState, threshold, null);

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
}
