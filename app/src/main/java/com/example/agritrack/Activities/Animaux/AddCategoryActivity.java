package com.example.agritrack.Activities.Animaux;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agritrack.R;

public class AddCategoryActivity extends AppCompatActivity {

    private EditText etCategoryName, etCategoryDescription;
    private Button btnSave, btnCancel;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_simple_input);

        // Initialiser les vues
        initViews();

        // Configurer les listeners
        setupListeners();
    }

    private void initViews() {
        etCategoryName = findViewById(R.id.etCategoryName);
        etCategoryDescription = findViewById(R.id.etCategoryDescription);
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupListeners() {
        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Bouton annuler
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        // Bouton enregistrer
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveCategory());
        }
    }

    private void saveCategory() {
        String name = etCategoryName.getText().toString().trim();
        String description = etCategoryDescription.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etCategoryName.setError("Le nom est requis");
            etCategoryName.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etCategoryDescription.setError("La description est requise");
            etCategoryDescription.requestFocus();
            return;
        }

        // Retourner les données à l'activité précédente
        android.content.Intent resultIntent = new android.content.Intent();
        resultIntent.putExtra("CATEGORY_NAME", name);
        resultIntent.putExtra("CATEGORY_DESCRIPTION", description);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Catégorie enregistrée", Toast.LENGTH_SHORT).show();
        finish();
    }
}