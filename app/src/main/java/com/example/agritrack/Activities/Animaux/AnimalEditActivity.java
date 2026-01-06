package com.example.agritrack.Activities.Animaux;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AnimalEditActivity extends AppCompatActivity {

    private static final String TAG = "AnimalEditActivity";

    // Views
    private EditText etName, etWeight, etBirthDate;
    private Spinner spinnerSpecies, spinnerBreed, spinnerGender, spinnerHealthStatus;
    private Button btnSave, btnCancel, btnSelectDate;
    private ImageButton btnBack;

    // Database
    private AnimalEntity animal;
    private AnimalDao animalDao;
    private long animalId;

    // Date formatter
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // IMPORTANT : Utiliser le layout animal_edit.xml
        setContentView(R.layout.animal_edit);

        // Initialiser la base de données
        AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(this);
        animalDao = database.animalDao();

        // Initialiser les vues
        initViews();

        // Configurer les spinners
        setupSpinners();

        // Charger l'animal à modifier
        loadAnimalData();

        // Configurer les listeners
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etWeight = findViewById(R.id.etWeight);
        etBirthDate = findViewById(R.id.tvBirthDate);
        spinnerSpecies = findViewById(R.id.spinnerSpecies);
        spinnerBreed = findViewById(R.id.spinnerBreed);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerHealthStatus = findViewById(R.id.spinnerHealthStatus);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnBack = findViewById(R.id.btnBack);

        // Configuration du champ date
        if (etBirthDate != null) {
            etBirthDate.setFocusable(false);
            etBirthDate.setClickable(true);
            etBirthDate.setCursorVisible(false);
        }
    }

    private void setupSpinners() {
        // Spinner Genre
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Spinner Statut de santé
        ArrayAdapter<CharSequence> healthAdapter = ArrayAdapter.createFromResource(
                this, R.array.health_array, android.R.layout.simple_spinner_item);
        healthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHealthStatus.setAdapter(healthAdapter);

        // Spinner Espèce
        ArrayAdapter<CharSequence> speciesAdapter = ArrayAdapter.createFromResource(
                this, R.array.species_array, android.R.layout.simple_spinner_item);
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecies.setAdapter(speciesAdapter);

        // Spinner Race (initial)
        ArrayAdapter<CharSequence> breedAdapter = ArrayAdapter.createFromResource(
                this, R.array.breeds_array, android.R.layout.simple_spinner_item);
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(breedAdapter);

        // Filtrer les races selon l'espèce
        spinnerSpecies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String species = parent.getItemAtPosition(position).toString();
                filterBreedsBySpecies(species);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadAnimalData() {
        animalId = getIntent().getLongExtra("ANIMAL_ID", -1);

        if (animalId != -1) {
            new Thread(() -> {
                animal = animalDao.getById(animalId);

                if (animal != null) {
                    runOnUiThread(() -> {
                        etName.setText(animal.getName());
                        etWeight.setText(String.valueOf(animal.getWeight()));
                        etBirthDate.setText(animal.getBirthDate());

                        setSpinnerValue(spinnerSpecies, animal.getSpecies());

                        // Attendre que les races soient filtrées avant de sélectionner
                        spinnerSpecies.post(() -> {
                            setSpinnerValue(spinnerBreed, animal.getBreed());
                        });

                        setSpinnerValue(spinnerGender, animal.getGender());
                        setSpinnerValue(spinnerHealthStatus, animal.getHealthStatus());
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Animal introuvable", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }).start();
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveAnimal());

        btnSelectDate.setOnClickListener(v -> {
            Log.d(TAG, "Bouton date cliqué");
            showDatePicker();
        });

        etBirthDate.setOnClickListener(v -> {
            Log.d(TAG, "Champ date cliqué");
            showDatePicker();
        });
    }

    private void filterBreedsBySpecies(String species) {
        ArrayAdapter<String> breedAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item);

        switch (species) {
            case "Vache":
                breedAdapter.add("Holstein");
                breedAdapter.add("Charolaise");
                breedAdapter.add("Limousine");
                breedAdapter.add("Montbéliarde");
                break;
            case "Mouton":
                breedAdapter.add("Mérinos");
                breedAdapter.add("Suffolk");
                breedAdapter.add("Texel");
                break;
            case "Chèvre":
                breedAdapter.add("Alpine");
                breedAdapter.add("Saanen");
                breedAdapter.add("Boer");
                break;
            case "Poule":
                breedAdapter.add("Leghorn");
                breedAdapter.add("Sussex");
                breedAdapter.add("Rhode Island Red");
                break;
            case "Lapin":
                breedAdapter.add("Nain");
                breedAdapter.add("Géant");
                breedAdapter.add("Angora");
                break;
            default:
                breedAdapter.add("Race standard");
                break;
        }

        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Conserver la sélection actuelle si possible
        String currentBreed = spinnerBreed.getSelectedItem() != null ?
                spinnerBreed.getSelectedItem().toString() : "";

        spinnerBreed.setAdapter(breedAdapter);

        if (!currentBreed.isEmpty()) {
            int position = breedAdapter.getPosition(currentBreed);
            if (position >= 0) {
                spinnerBreed.setSelection(position);
            }
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (spinner.getAdapter() != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        String currentDate = etBirthDate.getText().toString();
        if (!currentDate.isEmpty() && !currentDate.equals("Sélectionner une date")) {
            try {
                Date date = dateFormat.parse(currentDate);
                if (date != null) {
                    calendar.setTime(date);
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur parsing date", e);
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String selectedDate = dateFormat.format(calendar.getTime());
                    etBirthDate.setText(selectedDate);

                    Toast.makeText(this, "Date modifiée: " + selectedDate, Toast.LENGTH_SHORT).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void saveAnimal() {
        if (animal == null) {
            Toast.makeText(this, "Erreur: animal introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Le nom est requis");
            etName.requestFocus();
            return;
        }

        String weightText = etWeight.getText().toString().trim();
        double weight = 0;
        if (!weightText.isEmpty()) {
            try {
                weight = Double.parseDouble(weightText);
                if (weight <= 0) {
                    etWeight.setError("Poids invalide");
                    etWeight.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etWeight.setError("Poids invalide");
                etWeight.requestFocus();
                return;
            }
        }

        String birthDate = etBirthDate.getText().toString();
        if (birthDate.equals("Sélectionner une date") || birthDate.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mettre à jour l'animal
        animal.setName(name);
        animal.setSpecies(spinnerSpecies.getSelectedItem().toString());
        animal.setBreed(spinnerBreed.getSelectedItem().toString());
        animal.setBirthDate(birthDate);
        animal.setWeight(weight);
        animal.setGender(spinnerGender.getSelectedItem() != null ?
                spinnerGender.getSelectedItem().toString() : "Non spécifié");
        animal.setHealthStatus(spinnerHealthStatus.getSelectedItem() != null ?
                spinnerHealthStatus.getSelectedItem().toString() : "Sain");

        // Sauvegarder
        new Thread(() -> {
            try {
                int updated = animalDao.update(animal);

                runOnUiThread(() -> {
                    if (updated > 0) {
                        Toast.makeText(this, "Animal modifié avec succès!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Erreur modification", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}