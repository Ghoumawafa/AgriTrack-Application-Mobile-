package com.example.agritrack.Activities.Animaux;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Adapters.CategoryAdapter;
import com.example.agritrack.R;

public class AnimalCategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animal_activity_category);
        ImageButton btnBack = findViewById(R.id.btnBack);
        RecyclerView recyclerView = findViewById(R.id.recyclerCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String[] categories = {"Vache", "Mouton", "Lapin", "Chèvre", "Poule"};

        String[] descriptions = {
                "Gestion du troupeau",
                "Suivi des moutons",
                "Élevage des lapins",
                "Contrôle des chèvres",
                "Gestion des volailles"
        };

        int[] images = {
                R.drawable.ic_vache,
                R.drawable.ic_mouton,
                R.drawable.ic_lapin,
                R.drawable.ic_chevre,
                R.drawable.ic_poule
        };

        CategoryAdapter adapter = new CategoryAdapter(
                categories,
                descriptions,
                images,
                category -> {
                    Intent intent = new Intent(
                            AnimalCategoryActivity.this,
                            AnimalListActivity.class
                    );
                    intent.putExtra("CATEGORY", category);
                    startActivity(intent);
                }
        );

        recyclerView.setAdapter(adapter);
        btnBack.setOnClickListener(v -> {
            finish(); // Retour à l'écran précédent
        });

    }}