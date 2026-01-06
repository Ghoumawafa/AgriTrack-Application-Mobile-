package com.example.agritrack.Activities.Animaux;

import androidx.appcompat.app.AppCompatActivity;
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

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddAnimalActivity extends AppCompatActivity {

    private static final String TAG = "AddAnimalActivity";

    // Views
    private EditText etName, etWeight, etBirthDate;
    private Spinner spinnerSpecies, spinnerBreed, spinnerGender, spinnerHealthStatus;
    private Button btnSave, btnCancel, btnSelectDate;
    private ImageButton btnBack;

    // Database
    private AnimalDao animalDao;

    // Date formatter
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animal_add);

        // Initialiser la base de données
        AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(this);
        animalDao = database.animalDao();

        // Initialiser les vues
        initViews();

        // Configurer les spinners
        setupSpinners();

        // Configurer les listeners
        setupListeners();

        // Gérer la catégorie passée en paramètre
        handleCategoryIntent();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etWeight = findViewById(R.id.etWeight);
        etBirthDate = findViewById(R.id.tvBirthDate); // C'est bien tvBirthDate dans le XML
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
            etBirthDate.setText("Sélectionner une date");
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

    private void setupListeners() {
        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Bouton annuler
        btnCancel.setOnClickListener(v -> finish());

        // Bouton enregistrer
        btnSave.setOnClickListener(v -> saveAnimal());

        // Bouton sélection de date
        btnSelectDate.setOnClickListener(v -> {
            Log.d(TAG, "Bouton date cliqué");
            showDatePicker();
        });

        // Clic sur le champ date lui-même
        etBirthDate.setOnClickListener(v -> {
            Log.d(TAG, "Champ date cliqué");
            showDatePicker();
        });
    }

    private void handleCategoryIntent() {
        String category = getIntent().getStringExtra("CATEGORY");
        if (category != null && spinnerSpecies.getAdapter() != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinnerSpecies.getAdapter();
            int position = adapter.getPosition(category);
            if (position >= 0) {
                spinnerSpecies.setSelection(position);
            }
        }
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
        spinnerBreed.setAdapter(breedAdapter);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // Parser la date actuelle si elle existe
        String currentDate = etBirthDate.getText().toString();
        if (!currentDate.equals("Sélectionner une date")) {
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
                    etBirthDate.setTextColor(getResources().getColor(android.R.color.black));

                    Toast.makeText(this, "Date sélectionnée: " + selectedDate, Toast.LENGTH_SHORT).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Date maximale = aujourd'hui
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void saveAnimal() {
        // Validation du nom
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Le nom est requis");
            etName.requestFocus();
            return;
        }

        // Validation du poids
        double weight = 0;
        String weightText = etWeight.getText().toString().trim();
        if (!weightText.isEmpty()) {
            try {
                weight = Double.parseDouble(weightText);
                if (weight <= 0) {
                    etWeight.setError("Le poids doit être supérieur à 0");
                    etWeight.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etWeight.setError("Poids invalide");
                etWeight.requestFocus();
                return;
            }
        }

        // Validation de la date
        String birthDate = etBirthDate.getText().toString();
        if (birthDate.equals("Sélectionner une date") || birthDate.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une date de naissance", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation de l'espèce
        if (spinnerSpecies.getSelectedItem() == null) {
            Toast.makeText(this, "Veuillez sélectionner une espèce", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation de la race
        if (spinnerBreed.getSelectedItem() == null) {
            Toast.makeText(this, "Veuillez sélectionner une race", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer l'entité animal
        AnimalEntity animal = new AnimalEntity(
                name,
                spinnerSpecies.getSelectedItem().toString(),
                spinnerBreed.getSelectedItem().toString(),
                birthDate,
                weight,
                spinnerGender.getSelectedItem() != null ?
                        spinnerGender.getSelectedItem().toString() : "Non spécifié",
                spinnerHealthStatus.getSelectedItem() != null ?
                        spinnerHealthStatus.getSelectedItem().toString() : "Sain"
        );

        // Sauvegarder dans un thread séparé
        new Thread(() -> {
            try {
                long id = animalDao.insert(animal);

                runOnUiThread(() -> {
                    if (id > 0) {
                        Toast.makeText(this, "Animal ajouté avec succès!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Erreur sauvegarde", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}