package com.example.agritrack.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DiseaseDetector {

    private static final String TAG = "DiseaseDetector";

    // TODO: move key to backend / secure storage (don’t ship real keys in APK)
    private static final String API_KEY = "ktsKZoiyoy3m6JpvOBswOOFtyu7m8XXYLksRdgqAlsDcWwAEas";
    private static final String ENDPOINT = "https://plant.id/api/v3/health_assessment";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final ExecutorService executor;

    public static class DetectionResult {
        public String diseaseName;
        public float confidence;
        public String severity;
        public String recommendation;

        public DetectionResult(String diseaseName, float confidence, String severity, String recommendation) {
            this.diseaseName = diseaseName;
            this.confidence = confidence;
            this.severity = severity;
            this.recommendation = recommendation;
        }
    }

    public interface Callback {
        void onResult(@NonNull DetectionResult result);
    }

    public DiseaseDetector(Context context) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        this.executor = Executors.newSingleThreadExecutor();
    }

    /** Use this from UI (Activity/Fragment) */
    public void detectDiseaseAsync(Bitmap bitmap, @NonNull Callback callback) {
        executor.execute(() -> {
            DetectionResult result = detectDiseaseBlocking(bitmap);
            callback.onResult(result);
        });
    }

    /** Blocking call: NEVER call on main thread */
    private DetectionResult detectDiseaseBlocking(Bitmap bitmap) {
        if (bitmap == null) {
            return new DetectionResult("Erreur", 0.0f, "N/A", "Aucune image capturée");
        }

        try {
            String base64Image = bitmapToBase64(bitmap);

            JSONArray imagesArray = new JSONArray().put(base64Image);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("images", imagesArray);
            jsonBody.put("health", "all");

            String responseString = postRequest(jsonBody.toString());
            Log.d(TAG, "API Response: " + responseString);

            return parseResponse(responseString);

        } catch (SocketTimeoutException e) {
            Log.e(TAG, "Timeout", e);
            return new DetectionResult("Erreur", 0.0f, "N/A",
                    "Timeout réseau. Vérifiez la connexion et réessayez.");

        } catch (IOException e) {
            Log.e(TAG, "HTTP/Network error", e);
            return new DetectionResult("Erreur", 0.0f, "N/A",
                    "Erreur réseau: " + e.getClass().getSimpleName() + " - " + safeMsg(e));

        } catch (JSONException e) {
            Log.e(TAG, "JSON parse error", e);
            return new DetectionResult("Erreur", 0.0f, "N/A",
                    "Réponse API inattendue (JSON): " + safeMsg(e));

        } catch (Exception e) {
            Log.e(TAG, "Analysis failed", e);
            return new DetectionResult("Erreur", 0.0f, "N/A",
                    "Échec: " + e.getClass().getSimpleName() + " - " + safeMsg(e));
        }
    }

    private String safeMsg(Exception e) {
        String m = e.getMessage();
        return (m == null || m.trim().isEmpty()) ? "(no message)" : m;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    private String postRequest(String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(ENDPOINT)
                .post(body)
                .addHeader("Api-Key", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = (response.body() != null) ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + " - " + response.message()
                        + " | Body: " + responseBody);
            }
            return responseBody;
        }
    }

    private DetectionResult parseResponse(String jsonResponse) throws JSONException {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            return new DetectionResult("Erreur API", 0.0f, "N/A", "Réponse vide de l’API.");
        }

        JSONObject json = new JSONObject(jsonResponse);

        // Some APIs return error/message fields
        if (json.has("error") || json.has("message")) {
            String err = json.optString("error", json.optString("message", "Erreur inconnue"));
            return new DetectionResult("Erreur API", 0.0f, "N/A", err);
        }

        JSONObject result = json.optJSONObject("result");
        if (result == null) {
            return new DetectionResult("Erreur API", 0.0f, "N/A",
                    "Champ 'result' manquant. Réponse: " + jsonResponse);
        }

        // Healthy block (defensive)
        JSONObject isHealthyObj = result.optJSONObject("is_healthy");
        boolean isHealthy = false;
        float healthyProb = 0f;
        if (isHealthyObj != null) {
            isHealthy = isHealthyObj.optBoolean("binary", false);
            healthyProb = (float) isHealthyObj.optDouble("probability", 0.0);
        }

        JSONObject diseaseObj = result.optJSONObject("disease");
        JSONArray suggestions = (diseaseObj != null) ? diseaseObj.optJSONArray("suggestions") : null;

        if (isHealthy && healthyProb > 0.8f) {
            return new DetectionResult(
                    "Aucune maladie détectée",
                    healthyProb,
                    "Faible",
                    "Votre plante semble en bonne santé. Continuez les bons soins."
            );
        }

        if (suggestions == null || suggestions.length() == 0) {
            return new DetectionResult(
                    "Inconnu",
                    Math.max(healthyProb, 0f),
                    "N/A",
                    "Aucune suggestion reçue. Essayez une photo plus nette et rapprochée de la zone touchée."
            );
        }

        JSONObject top = suggestions.getJSONObject(0);
        String diseaseName = top.optString("name", "Inconnu");
        float probability = (float) top.optDouble("probability", 0.0);

        String severity = probability >= 0.8f ? "Sévère"
                : probability >= 0.5f ? "Modérée"
                : "Faible";

        String recommendation = buildRecommendation(top);

        return new DetectionResult(diseaseName, probability, severity, recommendation);
    }

    private String buildRecommendation(JSONObject topSuggestion) {
        StringBuilder sb = new StringBuilder();

        JSONObject details = topSuggestion.optJSONObject("details");
        if (details != null) {
            String description = details.optString("description", "").trim();
            Object treatmentObj = details.opt("treatment");

            String treatmentText = "";
            if (treatmentObj instanceof JSONObject) {
                JSONObject t = (JSONObject) treatmentObj;
                String prevention = t.optString("prevention", "").trim();
                String biological = t.optString("biological", "").trim();
                String chemical = t.optString("chemical", "").trim();

                StringBuilder tSb = new StringBuilder();
                if (!prevention.isEmpty()) tSb.append("- Prévention: ").append(prevention).append("\n");
                if (!biological.isEmpty()) tSb.append("- Biologique: ").append(biological).append("\n");
                if (!chemical.isEmpty()) tSb.append("- Chimique: ").append(chemical).append("\n");
                treatmentText = tSb.toString().trim();
            } else if (treatmentObj instanceof String) {
                treatmentText = ((String) treatmentObj).trim();
            }

            if (!description.isEmpty()) sb.append(description).append("\n\n");
            if (!treatmentText.isEmpty()) sb.append("Traitement recommandé:\n").append(treatmentText);
        }

        if (sb.length() == 0) {
            sb.append("Prenez une photo plus proche (bonne lumière, focus net) et réessayez.");
        }

        return sb.toString().trim();
    }

    public void close() {
        executor.shutdown();
    }
}