package com.example.agritrack.Utils;

import android.os.AsyncTask;
import android.util.Log;

import com.example.agritrack.BuildConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeminiService {
    // Use API key from BuildConfig (set via app/build.gradle.kts)
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY == null ? "" : BuildConfig.GEMINI_API_KEY;
    // Endpoints de fallback (diagnostic). Ajustez les noms de modèle si nécessaire.
    private static final String[] ENDPOINTS = new String[] {
            // Prioritise a model that exists in your project (from your /v1/models output)
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY,
            // Fallbacks (chat-style / text) using same model name
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateMessage?key=" + API_KEY,
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateText?key=" + API_KEY,
            "https://generativelanguage.googleapis.com/v1beta2/models/gemini-2.5-flash:generateMessage?key=" + API_KEY
    };
    // Optionnel: si vous utilisez OAuth Bearer token
    private static final boolean USE_BEARER = false;
    private static final String BEARER_TOKEN = "";
    // Conservé pour compatibilité si besoin
    private static final String ENDPOINT = ENDPOINTS[0];
    public interface GeminiCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public void askGemini(String prompt, GeminiCallback callback) {
        new GeminiTask(callback).execute(prompt);
    }

    private static class GeminiTask extends AsyncTask<String, Void, String> {
        private final GeminiCallback callback;

        GeminiTask(GeminiCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String prompt = params[0];
                Log.d("Gemini", "🔄 PROMPT: " + prompt);

                // Format chat-style attendu par many versions of the Generative Language API
                String safePrompt = prompt.replace("\"", "\\\"");
                // We'll choose payload per-endpoint below (some endpoints expect `prompt`, others `messages`)
                Log.d("Gemini", "🔄 prepared prompt (escaped): " + safePrompt);

                String lastResponse = null;
                int lastCode = -1;
                String triedEndpoint = null;

                // Use primary endpoint only to avoid multiple calls per request
                String ep = ENDPOINT;
                triedEndpoint = ep;
                Log.d("Gemini", "➡️ USING ENDPOINT: " + ep);
                // For generateContent (primary) use contents/parts format
                String jsonToSend = "{\"contents\":[{\"parts\":[{\"text\":\"" + safePrompt + "\"}]}]}";
                Log.d("Gemini", "📤 REQUEST: " + jsonToSend);

                URL url = new URL(ep);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    if (USE_BEARER && BEARER_TOKEN.length() > 0) {
                        conn.setRequestProperty("Authorization", "Bearer " + BEARER_TOKEN);
                    }
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(120000);

                    // Écriture
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonToSend.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int code = conn.getResponseCode();
                    lastCode = code;
                    String responseMessage = conn.getResponseMessage();
                    Log.d("Gemini", "📥 CODE: " + code + " message:" + responseMessage);

                    // Lire headers pour diagnostic
                    Log.d("Gemini", "📤 Headers: " + conn.getHeaderFields().toString());

                    // Lecture réponse (gère null streams)
                    StringBuilder response = new StringBuilder();
                    java.io.InputStream is = null;
                    try {
                        is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
                        if (is != null) {
                            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                                String line;
                                while ((line = br.readLine()) != null) {
                                    response.append(line);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Log.e("Gemini", "Error reading stream: " + ex.getMessage());
                    }

                    Log.d("Gemini", "📥 BODY: " + response);
                    conn.disconnect();

                    lastResponse = response.toString();

                    if (code == 200) {
                        try {
                            JsonElement root = JsonParser.parseString(lastResponse);

                            // 1) Try the common generateContent shape: candidates[0].content.parts[0].text
                            if (root.isJsonObject()) {
                                JsonObject obj = root.getAsJsonObject();
                                if (obj.has("candidates")) {
                                    JsonArray candidates = obj.getAsJsonArray("candidates");
                                    if (candidates.size() > 0) {
                                        JsonObject first = candidates.get(0).getAsJsonObject();
                                        if (first.has("content")) {
                                            JsonObject content = first.getAsJsonObject("content");
                                            if (content.has("parts")) {
                                                JsonArray parts = content.getAsJsonArray("parts");
                                                if (parts.size() > 0) {
                                                    JsonObject p0 = parts.get(0).getAsJsonObject();
                                                    if (p0.has("text")) {
                                                        String answer = p0.get("text").getAsString();
                                                        Log.d("Gemini", "✅ ANSWER: " + answer);
                                                        return "✅ " + answer;
                                                    }
                                                }
                                            }
                                            // fallback: content.text
                                            if (content.has("text")) {
                                                String answer = content.get("text").getAsString();
                                                Log.d("Gemini", "✅ ANSWER: " + answer);
                                                return "✅ " + answer;
                                            }
                                        }
                                    }
                                }
                            }

                            // 2) generic recursive search for first "text" field
                            String found = findFirstText(root);
                            if (found != null) {
                                Log.d("Gemini", "✅ ANSWER(recursive): " + found);
                                return "✅ " + found;
                            }

                        } catch (Exception ex) {
                            Log.e("Gemini", "JSON parse error: " + ex.getMessage());
                        }
                        return "✅ " + lastResponse;
                    }
                // single endpoint flow finished
                if (lastCode != 200) {
                    return "❌ HTTP " + lastCode + " (tried " + triedEndpoint + "): " + lastResponse;
                }

            } catch (Exception e) {
                Log.e("Gemini", "💥 ERROR: " + e.getMessage());
                return "❌ " + e.getMessage();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.startsWith("✅")) {
                callback.onResponse(result.substring(2));
            } else {
                callback.onError(result);
            }
        }
    }

    // Recursive helper to find first occurrence of a "text" field in JSON
    private static String findFirstText(JsonElement el) {
        if (el == null || el.isJsonNull()) return null;
        try {
            if (el.isJsonObject()) {
                JsonObject obj = el.getAsJsonObject();
                for (String key : obj.keySet()) {
                    JsonElement v = obj.get(key);
                    if ("text".equalsIgnoreCase(key) && v != null && v.isJsonPrimitive()) {
                        return v.getAsString();
                    }
                    String nested = findFirstText(v);
                    if (nested != null) return nested;
                }
            } else if (el.isJsonArray()) {
                JsonArray arr = el.getAsJsonArray();
                for (JsonElement item : arr) {
                    String nested = findFirstText(item);
                    if (nested != null) return nested;
                }
            }
        } catch (Exception ex) {
            Log.e("Gemini", "findFirstText error: " + ex.getMessage());
        }
        return null;
    }
}
