package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.agritrack.R;
import com.example.agritrack.Utils.WeatherUtils;

public class ApiConfigActivity extends AppCompatActivity {

    private EditText etApiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_config);

        etApiKey = findViewById(R.id.etApiKey);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnBack = findViewById(R.id.btnBack);

        // Charger la clé existante
        String currentKey = WeatherUtils.getApiKey(this);
        if (!currentKey.equals(WeatherUtils.DEFAULT_API_KEY)) {
            etApiKey.setText(currentKey);
        } else {
            etApiKey.setHint("Votre clé API OpenWeatherMap");
        }

        btnSave.setOnClickListener(v -> saveApiKey());
        btnBack.setOnClickListener(v -> finish());
    }

    private void saveApiKey() {
        String apiKey = etApiKey.getText().toString().trim();

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer une clé API", Toast.LENGTH_SHORT).show();
            return;
        }

        WeatherUtils.saveApiKey(this, apiKey);
        Toast.makeText(this, "Clé API sauvegardée avec succès!", Toast.LENGTH_SHORT).show();
        finish();
    }
}