package com.example.agritrack.Activities.Animaux;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddAnimalActivity extends AppCompatActivity {

    TextView tvBirthDate;
    Spinner spinnerSpecies, spinnerBreed, spinnerGender, spinnerHealthStatus;
    Button btnSave, btnCancel, btnSelectDate;
    ImageButton btnBack; // Ajout du bouton retour
    EditText etName, etWeight;
    private AnimalDao animalDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animal_add); // Assurez-vous que c'est le bon nom de layout

        // Initialiser TOUTES les vues
        etName = findViewById(R.id.etName);
        etWeight = findViewById(R.id.etWeight);
        tvBirthDate = findViewById(R.id.tvBirthDate);
        spinnerSpecies = findViewById(R.id.spinnerSpecies);
        spinnerBreed = findViewById(R.id.spinnerBreed);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerHealthStatus = findViewById(R.id.spinnerHealthStatus);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnBack = findViewById(R.id.btnBack); // Initialiser le bouton retour

        // Initialiser la base de données et obtenir le DAO
        AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(this);
        animalDao = database.animalDao();

        // Initialiser le texte de la date de naissance
        tvBirthDate.setText("Sélectionner une date");

        // Spinner Gender - utiliser le tableau du arrays.xml
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_array,
                android.R.layout.simple_spinner_item
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Spinner Health Status - utiliser le tableau du arrays.xml
        ArrayAdapter<CharSequence> healthAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.health_array,
                android.R.layout.simple_spinner_item
        );
        healthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHealthStatus.setAdapter(healthAdapter);

        // Spinner Species - utiliser le tableau du arrays.xml
        ArrayAdapter<CharSequence> speciesAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.species_array,
                android.R.layout.simple_spinner_item
        );
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecies.setAdapter(speciesAdapter);

        // Si une catégorie est passée par intent, sélectionner cette espèce
        String category = getIntent().getStringExtra("CATEGORY");
        if (category != null) {
            int position = speciesAdapter.getPosition(category);
            if (position >= 0) {
                spinnerSpecies.setSelection(position);
            }
        }

        // Spinner Breed - charger toutes les races initialement
        ArrayAdapter<CharSequence> breedAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.breeds_array,
                android.R.layout.simple_spinner_item
        );
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(breedAdapter);

        // Écouter les changements d'espèce pour filtrer les races
        spinnerSpecies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterBreedsBySpecies(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Configurer les listeners des boutons
        btnBack.setOnClickListener(v -> {
            finish(); // Retour à l'écran précédent
        });

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveAnimal());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void filterBreedsBySpecies(String species) {
        // Créer un nouvel adaptateur avec les races filtrées
        ArrayAdapter<String> filteredAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item
        );

        // Filtrer les races selon l'espèce
        switch (species) {
            case "Vache":
                filteredAdapter.add("Holstein");
                filteredAdapter.add("Charolaise");
                filteredAdapter.add("Limousine");
                filteredAdapter.add("Montbéliarde");
                break;
            case "Mouton":
                filteredAdapter.add("Mérinos");
                filteredAdapter.add("Suffolk");
                filteredAdapter.add("Texel");
                break;
            case "Chèvre":
                filteredAdapter.add("Alpine");
                filteredAdapter.add("Saanen");
                filteredAdapter.add("Boer");
                break;
            case "Poule":
                filteredAdapter.add("Leghorn");
                filteredAdapter.add("Sussex");
                filteredAdapter.add("Rhode Island Red");
                break;
            default:
                // Si aucune correspondance, montrer toutes les races du tableau
                ArrayAdapter<CharSequence> originalAdapter = ArrayAdapter.createFromResource(
                        this,
                        R.array.breeds_array,
                        android.R.layout.simple_spinner_item
                );
                for (int i = 0; i < originalAdapter.getCount(); i++) {
                    filteredAdapter.add(originalAdapter.getItem(i).toString());
                }
                break;
        }

        filteredAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(filteredAdapter);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String selectedDate = format.format(calendar.getTime());
                    tvBirthDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void saveAnimal() {
        // Validation des champs
        String name = etName.getText().toString().trim();
        String weightText = etWeight.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir le nom de l'animal", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return;
        }

        double weight = 0;
        if (!weightText.isEmpty()) {
            try {
                weight = Double.parseDouble(weightText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Poids invalide", Toast.LENGTH_SHORT).show();
                etWeight.requestFocus();
                return;
            }
        }

        String birthDate = tvBirthDate.getText().toString();
        if (birthDate.equals("Sélectionner une date")) {
            Toast.makeText(this, "Veuillez sélectionner une date de naissance", Toast.LENGTH_SHORT).show();
            tvBirthDate.requestFocus();
            return;
        }

        // Vérifier qu'une espèce est sélectionnée
        if (spinnerSpecies.getSelectedItem() == null) {
            Toast.makeText(this, "Veuillez sélectionner une espèce", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier qu'une race est sélectionnée
        if (spinnerBreed.getSelectedItem() == null || spinnerBreed.getSelectedItem().toString().isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une race", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer l'entité AnimalEntity
        AnimalEntity animal = new AnimalEntity(
                name,
                spinnerSpecies.getSelectedItem().toString(),
                spinnerBreed.getSelectedItem().toString(),
                birthDate,
                weight,
                spinnerGender.getSelectedItem() != null ? spinnerGender.getSelectedItem().toString() : "Non spécifié",
                spinnerHealthStatus.getSelectedItem() != null ? spinnerHealthStatus.getSelectedItem().toString() : "Sain"
        );

        // Sauvegarder dans la base de données
        try {
            long newId = animalDao.insert(animal);

            if (newId != -1) {
                Toast.makeText(this, "Animal ajouté avec succès!", Toast.LENGTH_SHORT).show();
                finish(); // Retour à la liste des animaux
            } else {
                Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}