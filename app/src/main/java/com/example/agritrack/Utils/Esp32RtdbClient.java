package com.example.agritrack.Utils;

import androidx.annotation.Nullable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public final class Esp32RtdbClient {

    // Must match ESP32 sketch (esp32code/esp32_IOT.ino)
    private static final String DB_BASE = "https://agritrack-48076-default-rtdb.firebaseio.com";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    private Esp32RtdbClient() {
    }

    /**
     * Upserts one irrigation zone config at /irrigation/zones/<zoneKey>.json
     * Uses Firebase REST PATCH so we can update partial fields.
     */
    public static void upsertZoneConfig(
            String zoneKey,
            String mode,
            boolean manualState,
            int threshold,
            @Nullable Callback callback
    ) {
        String safeMode = mode == null ? "auto" : mode;

        // Keep pins consistent with ESP32 sketch defaults
        int pumpPin = 26;
        int sensorPin = 34;

        String bodyJson = "{" +
                "\"mode\":" + quote(safeMode) + "," +
                "\"manualState\":" + manualState + "," +
                "\"threshold\":" + threshold + "," +
                "\"hardware\":{" +
                "\"pumpMotor\":{\"pin\":" + pumpPin + ",\"enabled\":true}," +
                "\"soilSensor\":{\"pin\":" + sensorPin + ",\"enabled\":true}" +
                "}" +
                "}";

        Request request = new Request.Builder()
                .url(buildZoneUrl(zoneKey))
                .patch(RequestBody.create(bodyJson, JSON))
                .build();

        Call call = client.newCall(request);
        if (callback != null) {
            call.enqueue(callback);
        } else {
            // fire-and-forget
            call.enqueue(new Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    // ignored
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                    response.close();
                }
            });
        }
    }

    /** Convenience: turns manualState off for a given zone. */
    public static void setManualOff(String zoneKey, @Nullable Callback callback) {
        String bodyJson = "{\"manualState\":false}";

        Request request = new Request.Builder()
                .url(buildZoneUrl(zoneKey))
                .patch(RequestBody.create(bodyJson, JSON))
                .build();

        Call call = client.newCall(request);
        if (callback != null) {
            call.enqueue(callback);
        } else {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                    response.close();
                }
            });
        }
    }

    private static String buildZoneUrl(String zoneKey) {
        String safeKey = sanitizeFirebaseKey(zoneKey);

        HttpUrl base = HttpUrl.parse(DB_BASE);
        if (base == null) {
            // Fallback; should never happen with a constant base.
            return DB_BASE + "/irrigation/zones/" + safeKey + ".json";
        }

        // Note: Firebase REST API expects ".json" on the final path segment.
        return base.newBuilder()
                .addPathSegments("irrigation/zones")
                .addPathSegment(safeKey + ".json")
                .build()
                .toString();
    }

    /**
     * Firebase RTDB keys cannot contain: '.', '#', '$', '[', ']', or '/'.
     * We replace these with '_' so terrain names don't break writes.
     */
    private static String sanitizeFirebaseKey(String rawKey) {
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

    private static String quote(String value) {
        if (value == null) return "\"\"";
        String escaped = value.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }
}
