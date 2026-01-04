package com.example.agritrack.Activities.Animaux;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Adapters.CategoryAdapter;
import com.example.agritrack.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnimalCategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;

    // Catégories par défaut
    private List<String> categories = new ArrayList<>(Arrays.asList(
            "Vache", "Mouton", "Lapin", "Chèvre", "Poule"
    ));

    private List<String> descriptions = new ArrayList<>(Arrays.asList(
            "Gestion du troupeau",
            "Suivi des moutons",
            "Élevage des lapins",
            "Contrôle des chèvres",
            "Gestion des volailles"
    ));

    private List<Integer> images = new ArrayList<>(Arrays.asList(
            R.drawable.ic_vache,
            R.drawable.ic_mouton,
            R.drawable.ic_lapin,
            R.drawable.ic_chevre,
            R.drawable.ic_poule
    ));

    private static final String PREFS_NAME = "AnimalCategories";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_DESCRIPTIONS = "descriptions";
    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animal_activity_category);

        ImageButton btnBack = findViewById(R.id.btnBack);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        recyclerView = findViewById(R.id.recyclerCategories);

        // Initialiser SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Configurer RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Charger les catégories
        loadCategories();

        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Bouton d'ajout
        fabAdd.setOnClickListener(v -> showAddCategoryDialog());

        // Debug
        Toast.makeText(this, "Catégories: " + categories.size(), Toast.LENGTH_SHORT).show();
    }

    private void loadCategories() {
        // Essayer de charger depuis SharedPreferences
        String savedCategories = sharedPreferences.getString(KEY_CATEGORIES, null);
        String savedDescriptions = sharedPreferences.getString(KEY_DESCRIPTIONS, null);

        if (savedCategories != null && savedDescriptions != null) {
            try {
                Type listType = new TypeToken<List<String>>(){}.getType();
                List<String> loadedCategories = gson.fromJson(savedCategories, listType);
                List<String> loadedDescriptions = gson.fromJson(savedDescriptions, listType);

                if (loadedCategories != null && !loadedCategories.isEmpty()) {
                    categories = loadedCategories;
                    descriptions = loadedDescriptions;

                    // Mettre à jour les images
                    updateImages();
                }
            } catch (Exception e) {
                e.printStackTrace();
                // En cas d'erreur, garder les valeurs par défaut
                saveDefaultCategories();
            }
        } else {
            // Première exécution : sauvegarder les catégories par défaut
            saveDefaultCategories();
        }

        updateAdapter();
    }

    private void updateImages() {
        images.clear();
        int[] availableImages = {
                R.drawable.ic_vache,
                R.drawable.ic_mouton,
                R.drawable.ic_lapin,
                R.drawable.ic_chevre,
                R.drawable.ic_poule
        };

        for (int i = 0; i < categories.size(); i++) {
            images.add(availableImages[i % availableImages.length]);
        }
    }

    private void saveDefaultCategories() {
        // Réinitialiser aux valeurs par défaut
        categories = new ArrayList<>(Arrays.asList(
                "Vache", "Mouton", "Lapin", "Chèvre", "Poule"
        ));
        descriptions = new ArrayList<>(Arrays.asList(
                "Gestion du troupeau",
                "Suivi des moutons",
                "Élevage des lapins",
                "Contrôle des chèvres",
                "Gestion des volailles"
        ));
        updateImages();
        saveCategories();
    }

    private void saveCategories() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_CATEGORIES, gson.toJson(categories));
            editor.putString(KEY_DESCRIPTIONS, gson.toJson(descriptions));
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateAdapter() {
        // Convertir List en tableaux
        String[] categoriesArray = categories.toArray(new String[0]);
        String[] descriptionsArray = descriptions.toArray(new String[0]);
        int[] imagesArray = new int[images.size()];
        for (int i = 0; i < images.size(); i++) {
            imagesArray[i] = images.get(i);
        }

        if (adapter == null) {
            adapter = new CategoryAdapter(
                    categoriesArray,
                    descriptionsArray,
                    imagesArray,
                    category -> {
                        // Navigation vers AnimalListActivity
                        Intent intent = new Intent(
                                AnimalCategoryActivity.this,
                                AnimalListActivity.class
                        );
                        intent.putExtra("CATEGORY", category);
                        startActivity(intent);
                    }
            );
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(categoriesArray, descriptionsArray, imagesArray);
        }
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter une catégorie");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_simple_input, null);
        TextInputEditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        TextInputEditText etCategoryDescription = dialogView.findViewById(R.id.etCategoryDescription);

        builder.setView(dialogView);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String name = etCategoryName.getText().toString().trim();
            String description = etCategoryDescription.getText().toString().trim();

            if (validateInput(name, description)) {
                addCategory(name, description);
            }
        });

        builder.setNegativeButton("Annuler", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean validateInput(String name, String description) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Le nom est requis", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "La description est requise", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Vérifier si existe déjà
        for (String cat : categories) {
            if (cat.equalsIgnoreCase(name)) {
                Toast.makeText(this, "Cette catégorie existe déjà", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void addCategory(String name, String description) {
        categories.add(name);
        descriptions.add(description);

        // Ajouter image
        int imageIndex = (categories.size() - 1) % 5;
        int[] availableImages = {
                R.drawable.ic_vache,
                R.drawable.ic_mouton,
                R.drawable.ic_lapin,
                R.drawable.ic_chevre,
                R.drawable.ic_poule
        };
        images.add(availableImages[imageIndex]);

        // Sauvegarder
        saveCategories();

        // Mettre à jour
        updateAdapter();

        Toast.makeText(this, "Catégorie ajoutée : " + name, Toast.LENGTH_SHORT).show();
    }
}