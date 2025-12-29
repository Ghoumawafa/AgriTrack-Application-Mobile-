// java
package com.example.agritrack.Utils;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.agritrack.Models.Irrigation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class FirebaseIrrigationManager {
    private static final String TAG = "FirebaseIrrManager";
    private final DatabaseReference ref;

    public interface RemoteListener {
        void onRemoteAdded(Irrigation irrigation, String key);
        void onRemoteChanged(Irrigation irrigation, String key);
        void onRemoteRemoved(String key);
        void onError(String message);
    }

    private ValueEventListener valueListener;

    public FirebaseIrrigationManager() {
        // root path in DB
        ref = FirebaseDatabase.getInstance().getReference("irrigations");
    }

    public void pushIrrigation(Irrigation i, String keyIfUpdate) {
        Map<String, Object> map = new HashMap<>();
        map.put("terrainName", i.getTerrainName());
        map.put("irrigationDate", i.getIrrigationDate() != null ? i.getIrrigationDate().getTime() : System.currentTimeMillis());
        map.put("waterQuantity", i.getWaterQuantity());
        map.put("method", i.getMethod());

        if (keyIfUpdate == null) {
            // create
            ref.push().setValue(map)
                    .addOnFailureListener(e -> Log.e(TAG, "push failed", e));
        } else {
            ref.child(keyIfUpdate).updateChildren(map)
                    .addOnFailureListener(e -> Log.e(TAG, "update failed", e));
        }
    }

    public void deleteIrrigation(String key) {
        if (key == null) return;
        ref.child(key).removeValue()
                .addOnFailureListener(e -> Log.e(TAG, "delete failed", e));
    }

    public void startListening(final RemoteListener listener) {
        if (valueListener != null) return;
        valueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // iterate children and notify as changed/added — simple approach: notify changed for all
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        String key = child.getKey();
                        String terrain = child.child("terrainName").getValue(String.class);
                        Long ts = child.child("irrigationDate").getValue(Long.class);
                        Double qty = child.child("waterQuantity").getValue(Double.class);
                        String method = child.child("method").getValue(String.class);
                        Irrigation i = new Irrigation(
                                terrain != null ? terrain : "",
                                ts != null ? new java.util.Date(ts) : new java.util.Date(),
                                qty != null ? qty : 0.0,
                                method != null ? method : "N/A"
                        );
                        // notify changed/added; client can deduplicate by key
                        listener.onRemoteChanged(i, key);
                    } catch (Exception e) {
                        listener.onError(e.getMessage());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        };
        ref.addValueEventListener(valueListener);
    }

    public void stopListening() {
        if (valueListener != null) {
            ref.removeEventListener(valueListener);
            valueListener = null;
        }
    }
}
