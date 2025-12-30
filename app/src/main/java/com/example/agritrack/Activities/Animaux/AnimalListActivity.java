package com.example.agritrack.Activities.Animaux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Adapters.AnimalAdapter;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class AnimalListActivity extends AppCompatActivity {

    private String category;
    private AnimalDao animalDao;
    private AnimalAdapter adapter;
    private TextView tvEmpty;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animal_list);

        // Récupérer la catégorie
        category = getIntent().getStringExtra("CATEGORY");
        if (category == null || category.isEmpty()) {
            Toast.makeText(this, "Catégorie non spécifiée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser les vues
        recyclerView = findViewById(R.id.recyclerAnimals);
        tvEmpty = findViewById(R.id.tvEmpty);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddAnimal);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Mettre à jour le titre
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            tvTitle.setText("Mes " + category + "s");
        }

        // Initialiser la base de données
        AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(this);
        animalDao = database.animalDao();

        // Configurer le RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialiser l'adapter AVANT de charger les données
        adapter = new AnimalAdapter(this, null, animalDao);
        recyclerView.setAdapter(adapter);

        // Charger les animaux initialement
        loadAnimals();

        // Bouton d'ajout
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAnimalActivity.class);
            intent.putExtra("CATEGORY", category);
            startActivityForResult(intent, 1);
        });

        // Écouter les clics sur les éléments de la liste
        adapter.setOnItemClickListener(animal -> {
            Intent intent = new Intent(this, AnimalEditActivity.class);
            intent.putExtra("ANIMAL_ID", animal.getId());
            startActivityForResult(intent, 2);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAnimals();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 || requestCode == 2) {
            loadAnimals();
        }
    }

    private void loadAnimals() {
        new Thread(() -> {
            try {
                List<AnimalEntity> animals = animalDao.getBySpecies(category);

                runOnUiThread(() -> {
                    if (animals == null || animals.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        tvEmpty.setText("Aucune " + category.toLowerCase() + " trouvée\n\nAppuyez sur + pour en ajouter");
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setAnimalList(animals);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}