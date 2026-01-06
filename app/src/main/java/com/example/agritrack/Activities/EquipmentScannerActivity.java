package com.example.agritrack.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.EquipmentDao;
import com.example.agritrack.Database.EquipmentEntity;
import com.example.agritrack.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EquipmentScannerActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1001;
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private boolean processing = false;
    private EquipmentDao equipmentDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_scanner);

        previewView = findViewById(R.id.previewView);
        TextView txtHint = findViewById(R.id.txtHint);
        ImageButton btnClose = findViewById(R.id.btnCloseScan);

        equipmentDao = AgriTrackRoomDatabase.getInstance(this).equipmentDao();

        btnClose.setOnClickListener(v -> finish());

        barcodeScanner = BarcodeScanning.getClient();
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
                    @OptIn(markerClass = ExperimentalGetImage.class)
                    @Override
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        if (processing) {
                            imageProxy.close();
                            return;
                        }
                        processing = true;
                        try {
                            if (imageProxy.getImage() == null) {
                                processing = false;
                                imageProxy.close();
                                return;
                            }
                            InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
                            barcodeScanner.process(image)
                                    .addOnSuccessListener(barcodes -> {
                                        if (barcodes != null && !barcodes.isEmpty()) {
                                            handleBarcodes(barcodes);
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("Scanner", "barcode failed", e))
                                    .addOnCompleteListener(task -> {
                                        processing = false;
                                        imageProxy.close();
                                    });
                        } catch (Exception e) {
                            processing = false;
                            imageProxy.close();
                        }
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void handleBarcodes(List<Barcode> barcodes) {
        // take first value
        String val = null;
        for (Barcode b : barcodes) {
            if (b.getDisplayValue() != null) {
                val = b.getDisplayValue();
                break;
            }
        }
        if (val == null) return;
        final String scannedVal = val;

        runOnUiThread(() -> {
            // try to find by numeric id
            try {
                long id = Long.parseLong(scannedVal);
                EquipmentEntity found = equipmentDao.getById(id);
                if (found != null) {
                    Intent intent = new Intent(this, EquipmentEditActivity.class);
                    intent.putExtra(EquipmentListActivity.EXTRA_EQUIPMENT_ID, found.getId());
                    startActivity(intent);
                    finish();
                    return;
                }
            } catch (NumberFormatException ignored) {
            }

            // search by name/type contains
            List<EquipmentEntity> all = equipmentDao.getAll();
            for (EquipmentEntity e : all) {
                if (e.getName() != null && e.getName().equalsIgnoreCase(scannedVal)) {
                    Intent intent = new Intent(this, EquipmentEditActivity.class);
                    intent.putExtra(EquipmentListActivity.EXTRA_EQUIPMENT_ID, e.getId());
                    startActivity(intent);
                    finish();
                    return;
                }
            }

            // not found -> open edit to create new with scanned code in name
            Intent intent = new Intent(this, EquipmentEditActivity.class);
            intent.putExtra("scanned_code", scannedVal);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (barcodeScanner != null) barcodeScanner.close();
    }
}
