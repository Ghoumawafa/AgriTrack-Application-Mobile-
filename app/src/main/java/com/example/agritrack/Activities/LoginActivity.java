package com.example.agritrack.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.agritrack.R;
import com.example.agritrack.Utils.StorageHelper;
import com.example.agritrack.Utils.FileStorageHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText email, password;
    private StorageHelper storageHelper;
    private FileStorageHelper fileStorageHelper;

    // Firebase Auth
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // Activity result launcher for Google Sign-In
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        storageHelper = new StorageHelper(this);
        fileStorageHelper = new FileStorageHelper(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Register activity result launcher for Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        Log.w(TAG, "Google sign in cancelled");
                        Toast.makeText(this, "Connexion Google annulée", Toast.LENGTH_SHORT).show();
                    }
                });

        email = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);
        Button loginBtn = findViewById(R.id.btnLogin);
        Button googleSignInBtn = findViewById(R.id.btnGoogleSignIn);
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

        // Google Sign-In button
        if (googleSignInBtn != null) {
            googleSignInBtn.setOnClickListener(v -> signInWithGoogle());
        }

        signupLink.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.agritrack.Activities.SignupActivity.class));
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Google sign in successful: " + account.getEmail());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            Toast.makeText(this, "Échec de la connexion Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user info to local storage
                            String userEmail = user.getEmail();
                            String userName = user.getDisplayName();

                            storageHelper.setUserLoggedIn(true);
                            storageHelper.saveUserPreferences(userEmail, userName, "Ferme AgriTrack");

                            // Save to database if not exists
                            if (!storageHelper.isEmailRegistered(userEmail)) {
                                storageHelper.saveUserAccount(userEmail, "", userName);
                            }

                            fileStorageHelper.logActivity("Connexion Google: " + userEmail);
                            Toast.makeText(this, "Connexion Google réussie!", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(this, AccueilActivity.class));
                            finish();
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Échec de l'authentification Firebase", Toast.LENGTH_SHORT).show();
                    }
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