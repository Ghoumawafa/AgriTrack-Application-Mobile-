package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.agritrack.R;
import com.example.agritrack.Utils.StorageHelper;
import com.example.agritrack.Utils.FileStorageHelper;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private StorageHelper storageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        storageHelper = new StorageHelper(this);
        FileStorageHelper fileStorageHelper = new FileStorageHelper(this);



        email = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);
        Button loginBtn = findViewById(R.id.btnLogin);
        TextView signupLink = findViewById(R.id.linkSignUp);

        loginBtn.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();

            if (validateInputs(userEmail, userPassword)) {
                if (storageHelper.validateUserCredentials(userEmail, userPassword)) {
                    storageHelper.setUserLoggedIn(true);
                    String userName = storageHelper.getUserNameByEmail(userEmail);
                    storageHelper.saveUserPreferences(userEmail, userName, "Ferme AgriTrack");

                    fileStorageHelper.logActivity("Connexion: " + userEmail);
                    Toast.makeText(this, "Connexion réussie!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(this, com.example.agritrack.Activities.AccueilActivity.class));
                    finish();
                } else {
                    password.setError("Email ou mot de passe incorrect");
                    Toast.makeText(this, "Identifiants invalides", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signupLink.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.agritrack.Activities.SignupActivity.class));
        });
    }

    private boolean validateInputs(String email, String password) {
        boolean valid = true;
        if (email.isEmpty()) {
            this.email.setError("Email requis");
            valid = false;
        }
        if (password.isEmpty()) {
            this.password.setError("Mot de passe requis");
            valid = false;
        }
        return valid;
    }
}