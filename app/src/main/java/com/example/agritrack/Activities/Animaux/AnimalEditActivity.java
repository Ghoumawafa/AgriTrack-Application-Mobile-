package com.example.agritrack.Activities.Animaux;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.R;

public class AnimalEditActivity extends AppCompatActivity {

    EditText etName, etWeight;
    TextView tvBirthDate;
    Spinner spinnerSpecies, spinnerBreed, spinnerGender, spinnerHealthStatus;
    Button btnSave, btnCancel;

    private AnimalEntity animal;
    private AnimalDao animalDao;
    private long animalId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animal_add);

        // Initialiser la base de données et obtenir le DAO
        AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(this);
        animalDao = database.animalDao();

        etName = findViewById(R.id.etName);
        etWeight = findViewById(R.id.etWeight);
        tvBirthDate = findViewById(R.id.tvBirthDate);
        spinnerSpecies = findViewById(R.id.spinnerSpecies);
        spinnerBreed = findViewById(R.id.spinnerBreed);  // Changé de etBreed à spinnerBreed
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerHealthStatus = findViewById(R.id.spinnerHealthStatus);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Configurer les Spinners
        setupSpinners();

        animalId = getIntent().getLongExtra("ANIMAL_ID", -1);

        if (animalId != -1) {
            // Récupérer l'animal depuis la base de données
            animal = animalDao.getById(animalId);

            if (animal != null) {
                etName.setText(animal.getName());
                etWeight.setText(String.valueOf(animal.getWeight()));
                tvBirthDate.setText(animal.getBirthDate());

                // Sélectionner les valeurs dans les Spinners
                setSpinnerValue(spinnerSpecies, animal.getSpecies());
                setSpinnerValue(spinnerBreed, animal.getBreed());
                setSpinnerValue(spinnerGender, animal.getGender());
                setSpinnerValue(spinnerHealthStatus, animal.getHealthStatus());
            }
        }

        btnSave.setOnClickListener(v -> saveAnimal());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        // Spinner Gender
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_array,
                android.R.layout.simple_spinner_item
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Spinner Health Status
        ArrayAdapter<CharSequence> healthAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.health_array,
                android.R.layout.simple_spinner_item
        );
        healthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHealthStatus.setAdapter(healthAdapter);

        // Spinner Species
        ArrayAdapter<CharSequence> speciesAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.species_array,
                android.R.layout.simple_spinner_item
        );
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecies.setAdapter(speciesAdapter);

        // Spinner Breed
        ArrayAdapter<CharSequence> breedAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.breeds_array,
                android.R.layout.simple_spinner_item
        );
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(breedAdapter);
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

    private void saveAnimal() {
        if (animal != null) {
            // Mettre à jour les valeurs
            animal.setName(etName.getText().toString());
            animal.setBreed(spinnerBreed.getSelectedItem().toString());  // Récupérer du Spinner
            animal.setSpecies(spinnerSpecies.getSelectedItem().toString());
            animal.setGender(spinnerGender.getSelectedItem().toString());
            animal.setHealthStatus(spinnerHealthStatus.getSelectedItem().toString());

            String weightText = etWeight.getText().toString();
            if (!weightText.isEmpty()) {
                try {
                    animal.setWeight(Double.parseDouble(weightText));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Poids invalide", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            animal.setBirthDate(tvBirthDate.getText().toString());

            // Sauvegarder les modifications directement avec le DAO
            try {
                int updated = animalDao.update(animal);
                if (updated > 0) {
                    Toast.makeText(this, "Animal modifié", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}