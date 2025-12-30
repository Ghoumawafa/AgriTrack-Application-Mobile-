package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.agritrack.R;
import com.example.agritrack.Utils.StorageHelper;
import com.example.agritrack.Utils.FileStorageHelper;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private StorageHelper storageHelper;
    private ProgressBar progressBar;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        storageHelper = new StorageHelper(this);
        FileStorageHelper fileStorageHelper = new FileStorageHelper(this);



        email = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);
        loginBtn = findViewById(R.id.btnLogin);
        TextView signupLink = findViewById(R.id.linkSignUp);
        progressBar = findViewById(R.id.progressBar);

        // Hide progress bar initially
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        loginBtn.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();

            if (validateInputs(userEmail, userPassword)) {
                // Show loading indicator
                setLoading(true);

                // Simulate async operation (in real app, this would be network call)
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (storageHelper.validateUserCredentials(userEmail, userPassword)) {
                        storageHelper.setUserLoggedIn(true);
                        String userName = storageHelper.getUserNameByEmail(userEmail);
                        storageHelper.saveUserPreferences(userEmail, userName, "Ferme AgriTrack");

                        fileStorageHelper.logActivity("Connexion: " + userEmail);
                        Toast.makeText(this, "Connexion réussie!", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, AccueilActivity.class));
                        finish();
                    } else {
                        setLoading(false);
                        password.setError("Email ou mot de passe incorrect");
                        Toast.makeText(this, "Identifiants invalides. Veuillez réessayer.", Toast.LENGTH_LONG).show();
                    }
                }, 500); // Small delay to show loading state
            }
        });

        signupLink.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.agritrack.Activities.SignupActivity.class));
        });
    }

    private boolean validateInputs(String email, String password) {
        boolean valid = true;

        // Clear previous errors
        this.email.setError(null);
        this.password.setError(null);

        // Validate email
        if (TextUtils.isEmpty(email)) {
            this.email.setError("Email requis");
            this.email.requestFocus();
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("Format d'email invalide");
            this.email.requestFocus();
            valid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            this.password.setError("Mot de passe requis");
            if (valid) this.password.requestFocus();
            valid = false;
        } else if (password.length() < 6) {
            this.password.setError("Le mot de passe doit contenir au moins 6 caractères");
            if (valid) this.password.requestFocus();
            valid = false;
        }

        return valid;
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        loginBtn.setEnabled(!isLoading);
        email.setEnabled(!isLoading);
        password.setEnabled(!isLoading);
    }
}