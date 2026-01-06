package com.example.agritrack.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.example.agritrack.R;
import com.example.agritrack.Utils.FileStorageHelper;
import java.util.concurrent.Executor;

public class EmpreinteActivity extends AppCompatActivity {
    private Button btnBiometric, btnExitTest;
    private FileStorageHelper fileStorageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empreinte);

        fileStorageHelper = new FileStorageHelper(this);

        btnBiometric = findViewById(R.id.btnBiometric);
       btnExitTest = findViewById(R.id.btnExitTest);

        btnBiometric.setOnClickListener(v -> checkBiometricAndShowPrompt());

        btnExitTest.setOnClickListener(v -> {
            // Directly go to LoginActivity (bypass biometric) for teammates
            SharedPreferences prefs = getSharedPreferences("agritrack_test", MODE_PRIVATE);
            prefs.edit().putBoolean("test_mode_active", true).apply();
            fileStorageHelper.logActivity("Bypass biometric -> LoginActivity (Exit pressed)");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void checkBiometricAndShowPrompt() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuth = biometricManager.canAuthenticate();
        switch (canAuth) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                showBiometricPrompt();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "❌ Pas de capteur biométrique", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "⚠️ Capteur biométrique indisponible", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "⚠️ Aucun empreinte/enrôlement trouvé", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "⚠️ Biométrie indisponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        runOnUiThread(() -> {
                            fileStorageHelper.logActivity("Biometric succeeded - go to LoginActivity");
                            Toast.makeText(EmpreinteActivity.this, "✅ Empreinte OK", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EmpreinteActivity.this, LoginActivity.class));
                            finish();
                        });
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        runOnUiThread(() -> Toast.makeText(EmpreinteActivity.this, "❌ " + errString, Toast.LENGTH_SHORT).show());
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("🔐 Empreinte requise")
                .setSubtitle("Placez votre doigt sur le capteur")
                .setNegativeButtonText("Annuler")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
