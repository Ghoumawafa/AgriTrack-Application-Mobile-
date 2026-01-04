package com.example.agritrack.Activities.Animaux;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnimalDetailsDialog extends Dialog {

    private AnimalEntity animal;
    private Context context;
    private AnimalDao animalDao;
    private OnDismissListener onDismissListener;

    public interface OnDismissListener {
        void onDismiss();
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = onDismissListener;
    }

    public AnimalDetailsDialog(Context context, AnimalEntity animal) {
        super(context);
        this.context = context;
        this.animal = animal;

        AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(context);
        animalDao = database.animalDao();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_animal_details);

        initializeViews();
    }

    private void initializeViews() {
        // Remplir les informations de l'animal
        TextView tvAnimalName = findViewById(R.id.tvAnimalName);
        TextView tvAnimalSpecies = findViewById(R.id.tvAnimalSpecies);
        TextView tvAnimalBreed = findViewById(R.id.tvAnimalBreed);
        TextView tvAnimalGender = findViewById(R.id.tvAnimalGender);
        TextView tvAnimalBirthDate = findViewById(R.id.tvAnimalBirthDate);
        TextView tvAnimalWeight = findViewById(R.id.tvAnimalWeight);
        TextView tvAnimalHealthStatus = findViewById(R.id.tvAnimalHealthStatus);
        TextView tvAnimalAge = findViewById(R.id.tvAnimalAge);

        // Nom avec emoji
        String emoji = getEmojiForSpecies(animal.getSpecies());
        tvAnimalName.setText(emoji + " " + animal.getName());

        tvAnimalSpecies.setText(animal.getSpecies());
        tvAnimalBreed.setText(animal.getBreed());
        tvAnimalGender.setText(animal.getGender());
        tvAnimalBirthDate.setText(animal.getBirthDate());
        tvAnimalWeight.setText(String.format(Locale.getDefault(), "%.1f kg", animal.getWeight()));
        tvAnimalHealthStatus.setText(animal.getHealthStatus());

        // Calculer l'âge
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date birthDate = sdf.parse(animal.getBirthDate());
            Date today = new Date();

            long diff = today.getTime() - birthDate.getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            long months = days / 30;
            long years = months / 12;

            String ageText;
            if (years > 0) {
                ageText = years + " an" + (years > 1 ? "s" : "");
                if (months % 12 > 0) {
                    ageText += " et " + (months % 12) + " mois";
                }
            } else if (months > 0) {
                ageText = months + " mois";
            } else {
                ageText = days + " jour" + (days > 1 ? "s" : "");
            }

            tvAnimalAge.setText("Âge: " + ageText);
        } catch (Exception e) {
            tvAnimalAge.setText("Âge: Inconnu");
        }

        // Boutons d'action
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnClose = findViewById(R.id.btnClose);

        btnEdit.setOnClickListener(v -> {
            // Ouvrir l'écran d'édition
            Intent intent = new Intent(context, AnimalEditActivity.class);
            intent.putExtra("ANIMAL_ID", animal.getId());
            intent.putExtra("MODE", "EDIT");
            context.startActivity(intent);
            dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmation();
        });

        btnClose.setOnClickListener(v -> dismiss());
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(context)
                .setTitle("Confirmer la suppression")
                .setMessage("Êtes-vous sûr de vouloir supprimer \"" + animal.getName() + "\" ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    deleteAnimal();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteAnimal() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            int deleted = animalDao.delete(animal);

            getOwnerActivity().runOnUiThread(() -> {
                if (deleted > 0) {
                    Toast.makeText(context, animal.getName() + " supprimé", Toast.LENGTH_SHORT).show();
                    if (onDismissListener != null) {
                        onDismissListener.onDismiss();
                    }
                    dismiss();
                } else {
                    Toast.makeText(context, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String getEmojiForSpecies(String species) {
        switch (species) {
            case "Vache": return "🐄";
            case "Mouton": return "🐑";
            case "Chèvre": return "🐐";
            case "Poule": return "🐓";
            default: return "🐾";
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }
}