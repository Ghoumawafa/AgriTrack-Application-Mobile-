package com.example.agritrack.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Activities.Animaux.AnimalEditActivity;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.R;

import java.util.List;

public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder> {

    private Context context;
    private List<AnimalEntity> animalList;
    private AnimalDao animalDao;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AnimalEntity animal);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public AnimalAdapter(Context context, List<AnimalEntity> animalList, AnimalDao animalDao) {
        this.context = context;
        this.animalList = animalList;
        this.animalDao = animalDao;
    }

    public void setAnimalList(List<AnimalEntity> animalList) {
        this.animalList = animalList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.animal_item, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        AnimalEntity animal = animalList.get(position);

        // Définir l'emoji selon l'espèce
        String emoji = getEmojiForSpecies(animal.getSpecies());
        holder.tvAnimalEmoji.setText(emoji);

        holder.tvAnimalName.setText(animal.getName());
        holder.tvAnimalSpecies.setText(animal.getSpecies() + " - " + animal.getBreed());
        holder.tvAnimalHealth.setText("Statut: " + animal.getHealthStatus());
        holder.tvAnimalWeight.setText("Poids: " + animal.getWeight() + " kg");

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnimalEditActivity.class);
            intent.putExtra("ANIMAL_ID", animal.getId());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            // Suppression en arrière-plan simplifiée
            new Thread(() -> {
                int deleted = animalDao.delete(animal);

                // Mettre à jour l'UI via Handler
                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> {
                    if (deleted > 0) {
                        // Trouver et supprimer l'animal
                        int animalPosition = -1;
                        for (int i = 0; i < animalList.size(); i++) {
                            if (animalList.get(i).getId() == animal.getId()) {
                                animalPosition = i;
                                break;
                            }
                        }

                        if (animalPosition != -1) {
                            animalList.remove(animalPosition);
                            notifyItemRemoved(animalPosition);
                            Toast.makeText(context, "Animal supprimé", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        // Clic sur l'item entier
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(animal);
            }
        });
    }


    @Override
    public int getItemCount() {
        return animalList != null ? animalList.size() : 0;
    }

    static class AnimalViewHolder extends RecyclerView.ViewHolder {
        TextView tvAnimalEmoji, tvAnimalName, tvAnimalSpecies, tvAnimalHealth, tvAnimalWeight;
        ImageButton btnEdit, btnDelete;

        AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAnimalEmoji = itemView.findViewById(R.id.tvAnimalEmoji);
            tvAnimalName = itemView.findViewById(R.id.tvAnimalName);
            tvAnimalSpecies = itemView.findViewById(R.id.tvAnimalSpecies);
            tvAnimalHealth = itemView.findViewById(R.id.tvAnimalHealth);
            tvAnimalWeight = itemView.findViewById(R.id.tvAnimalWeight);
            btnEdit = itemView.findViewById(R.id.btnEditAnimal);
            btnDelete = itemView.findViewById(R.id.btnDeleteAnimal);
        }
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
}