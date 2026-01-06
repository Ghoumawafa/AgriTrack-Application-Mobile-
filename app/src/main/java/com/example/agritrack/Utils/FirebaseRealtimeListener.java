package com.example.agritrack.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebase Realtime Database listener utility
 * Provides lifecycle-safe listeners for irrigation zones
 */
public class FirebaseRealtimeListener {

    private final DatabaseReference databaseRef;
    private final Map<String, ValueEventListener> listeners = new HashMap<>();

    public FirebaseRealtimeListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true); // Enable offline persistence
        this.databaseRef = database.getReference();
    }

    /**
     * Listen to a specific zone's configuration
     * @param zoneKey The zone key (e.g., "zone1")
     * @param callback Callback for zone updates
     */
    public void listenToZone(String zoneKey, ZoneUpdateCallback callback) {
        String path = "irrigation/zones/" + sanitizeKey(zoneKey);
        DatabaseReference zoneRef = databaseRef.child(path);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ZoneData data = new ZoneData();
                    data.mode = snapshot.child("mode").getValue(String.class);
                    data.manualState = Boolean.TRUE.equals(snapshot.child("manualState").getValue(Boolean.class));
                    data.threshold = snapshot.child("threshold").getValue(Integer.class);
                    data.enabled = Boolean.TRUE.equals(snapshot.child("enabled").getValue(Boolean.class));
                    data.sensorValue = snapshot.child("sensorValue").getValue(Integer.class);

                    callback.onZoneUpdate(data);
                } else {
                    callback.onZoneUpdate(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };

        zoneRef.addValueEventListener(listener);
        listeners.put(zoneKey, listener);
    }

    /**
     * Stop listening to a specific zone
     * @param zoneKey The zone key
     */
    public void stopListening(String zoneKey) {
        ValueEventListener listener = listeners.remove(zoneKey);
        if (listener != null) {
            String path = "irrigation/zones/" + sanitizeKey(zoneKey);
            databaseRef.child(path).removeEventListener(listener);
        }
    }

    /**
     * Stop all listeners (call in onDestroy)
     */
    public void stopAllListeners() {
        for (Map.Entry<String, ValueEventListener> entry : listeners.entrySet()) {
            String path = "irrigation/zones/" + sanitizeKey(entry.getKey());
            databaseRef.child(path).removeEventListener(entry.getValue());
        }
        listeners.clear();
    }

    private String sanitizeKey(String rawKey) {
        String trimmed = rawKey == null ? "" : rawKey.trim();
        if (trimmed.isEmpty()) return "zone";
        return trimmed
                .replace('.', '_')
                .replace('#', '_')
                .replace('$', '_')
                .replace('[', '_')
                .replace(']', '_')
                .replace('/', '_');
    }

    /**
     * Zone data model
     */
    public static class ZoneData {
        public String mode;
        public boolean manualState;
        public Integer threshold;
        public boolean enabled;
        public Integer sensorValue;

        public boolean isManualMode() {
            return "manual".equalsIgnoreCase(mode);
        }

        public boolean isAutoMode() {
            return "auto".equalsIgnoreCase(mode);
        }
    }

    /**
     * Callback interface for zone updates
     */
    public interface ZoneUpdateCallback {
        void onZoneUpdate(@Nullable ZoneData data);
        void onError(String error);
    }
}

