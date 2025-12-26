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

public class SignupActivity extends AppCompatActivity {
    private EditText fullName, email, password, confirmPassword;
    private StorageHelper storageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        storageHelper = new StorageHelper(this);
        FileStorageHelper fileStorageHelper = new FileStorageHelper(this);

        fullName = findViewById(R.id.inputFullName);
        email = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);
        confirmPassword = findViewById(R.id.inputConfirmPassword);
        Button signupBtn = findViewById(R.id.btnSignUp);
        TextView loginLink = findViewById(R.id.linkLogin);

        signupBtn.setOnClickListener(v -> {
            String name = fullName.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();
            String userConfirmPassword = confirmPassword.getText().toString().trim();

            if (validateInputs(name, userEmail, userPassword, userConfirmPassword)) {
                if (storageHelper.isEmailRegistered(userEmail)) {
                    this.email.setError("Email déjà utilisé");
                    Toast.makeText(this, "Un compte existe déjà", Toast.LENGTH_SHORT).show();
                    return;
                }

                storageHelper.saveUserAccount(userEmail, userPassword, name);
                storageHelper.setUserLoggedIn(true);
                storageHelper.saveUserPreferences(userEmail, name, "Ferme de " + name);

                fileStorageHelper.logActivity("Inscription: " + userEmail);
                Toast.makeText(this, "Compte créé avec succès!", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(this, com.example.agritrack.Activities.AccueilActivity.class));
                finish();
            }
        });

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.agritrack.Activities.LoginActivity.class));
            finish();
        });
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        boolean valid = true;

        if (name.isEmpty()) {
            fullName.setError("Nom complet requis");
            valid = false;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("Email valide requis");
            valid = false;
        }
        if (password.isEmpty()) {
            this.password.setError("Mot de passe requis");
            valid = false;
        }
        if (confirmPassword.isEmpty()) {
            this.confirmPassword.setError("Confirmation requise");
            valid = false;
        }
        if (!password.equals(confirmPassword)) {
            this.confirmPassword.setError("Mots de passe différents");
            valid = false;
        }
        if (password.length() < 6) {
            this.password.setError("Minimum 6 caractères");
            valid = false;
        }

        return valid;
    }
}