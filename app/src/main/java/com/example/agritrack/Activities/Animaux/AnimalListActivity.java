package com.example.agritrack.Activities.Animaux;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Adapters.AnimalAdapter;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.R;

import java.util.ArrayList;
import java.util.List;

public class AnimalListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnimalAdapter adapter;
    private AgriTrackRoomDatabase database;
    private AnimalDao animalDao;
    private String selectedCategory;
    private EditText etSearch;
    private TextView tvEmpty, tvAnimalCount, tvTitle;
    private ImageButton btnClearSearch;

    private List<AnimalEntity> allAnimals = new ArrayList<>();
    private List<AnimalEntity> filteredAnimals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animal_list);

        selectedCategory = getIntent().getStringExtra("CATEGORY");

        database = AgriTrackRoomDatabase.getInstance(this);
        animalDao = database.animalDao();

        initializeViews();
        loadAnimals();
    }

    private void initializeViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerAnimals);
        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvAnimalCount = findViewById(R.id.tvAnimalCount);
        tvTitle = findViewById(R.id.tvTitle);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        // Titre de la catégorie
        if (selectedCategory != null) {
            tvTitle.setText(selectedCategory + " - Liste des animaux");
        }

        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Configurer RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurer l'adapter
        adapter = new AnimalAdapter(this, filteredAnimals, animalDao);
        recyclerView.setAdapter(adapter);

        // Gestion du clic sur un animal
        adapter.setOnItemClickListener(animal -> {
            showAnimalDetails(animal);
        });

        // Configurer la recherche
        setupSearch();

        // Bouton pour ajouter un animal
        findViewById(R.id.fabAddAnimal).setOnClickListener(v -> {
            openAddAnimalScreen();
        });

        // Initialiser le compteur
        updateAnimalCount();
    }

    private void setupSearch() {
        // Ajouter la barre de recherche si elle n'existe pas dans le layout
        if (etSearch == null) {
            // Créer dynamiquement la barre de recherche
            createSearchBar();
        } else {
            // Listener pour la recherche en temps réel
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterAnimals(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Bouton pour effacer la recherche
        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                etSearch.setText("");
                etSearch.requestFocus();
            });

            // Afficher/cacher le bouton clear selon le contenu
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void createSearchBar() {
        // Si vous n'avez pas de barre de recherche dans votre layout,
        // vous pouvez l'ajouter programmatiquement
        // Ou mieux, modifiez votre layout pour l'inclure
    }

    private void loadAnimals() {
        new Thread(() -> {
            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                allAnimals = animalDao.getBySpecies(selectedCategory);
            } else {
                allAnimals = animalDao.getAll();
            }

            runOnUiThread(() -> {
                if (allAnimals.isEmpty()) {
                    tvEmpty.setText("Aucun animal trouvé pour " + selectedCategory);
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    // Initialiser avec tous les animaux
                    filteredAnimals.clear();
                    filteredAnimals.addAll(allAnimals);
                    adapter.setAnimalList(filteredAnimals);

                    // Afficher le nombre d'animaux
                    updateAnimalCount();
                }
            });
        }).start();
    }

    private void filterAnimals(String query) {
        filteredAnimals.clear();

        if (query.isEmpty()) {
            // Si la recherche est vide, montrer tous les animaux
            filteredAnimals.addAll(allAnimals);
        } else {
            // Filtrer selon le nom, race, ou autres critères
            String searchQuery = query.toLowerCase().trim();

            for (AnimalEntity animal : allAnimals) {
                boolean matches = false;

                // Recherche dans le nom (insensible à la casse)
                if (animal.getName() != null && animal.getName().toLowerCase().contains(searchQuery)) {
                    matches = true;
                }

                // Recherche dans la race
                if (!matches && animal.getBreed() != null && animal.getBreed().toLowerCase().contains(searchQuery)) {
                    matches = true;
                }

                // Recherche dans le sexe
                if (!matches && animal.getGender() != null && animal.getGender().toLowerCase().contains(searchQuery)) {
                    matches = true;
                }

                // Recherche dans le statut de santé
                if (!matches && animal.getHealthStatus() != null &&
                        animal.getHealthStatus().toLowerCase().contains(searchQuery)) {
                    matches = true;
                }

                // Recherche dans la date de naissance
                if (!matches && animal.getBirthDate() != null &&
                        animal.getBirthDate().toLowerCase().contains(searchQuery)) {
                    matches = true;
                }

                if (matches) {
                    filteredAnimals.add(animal);
                }
            }
        }

        adapter.setAnimalList(filteredAnimals);
        updateAnimalCount();

        // Afficher message si aucun résultat
        if (filteredAnimals.isEmpty() && !query.isEmpty()) {
            tvEmpty.setText("Aucun animal trouvé pour \"" + query + "\"");
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else if (filteredAnimals.isEmpty()) {
            tvEmpty.setText("Aucun animal dans cette catégorie");
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateAnimalCount() {
        String searchText = "";
        if (etSearch != null) {
            searchText = etSearch.getText().toString();
        }

        if (searchText.isEmpty()) {
            tvAnimalCount.setText(allAnimals.size() + " animaux");
        } else {
            tvAnimalCount.setText(filteredAnimals.size() + " sur " + allAnimals.size() + " animaux");
        }
    }

    private void showAnimalDetails(AnimalEntity animal) {
        // Créer un dialog personnalisé pour montrer les détails
        AnimalDetailsDialog dialog = new AnimalDetailsDialog(this, animal);
        dialog.show();

        // Rafraîchir après modification
        dialog.setOnDismissListener(dialogInterface -> {
            loadAnimals();
        });
    }

    private void openAddAnimalScreen() {
        // Ouvrir l'activité d'ajout d'animal
        Intent intent = new Intent(this, AnimalEditActivity.class);
        intent.putExtra("CATEGORY", selectedCategory);
        intent.putExtra("MODE", "ADD");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Recharger les animaux après ajout/modification
            loadAnimals();
            Toast.makeText(this, "Animal enregistré", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les animaux si on revient d'un ajout/modification
        loadAnimals();
    }
}