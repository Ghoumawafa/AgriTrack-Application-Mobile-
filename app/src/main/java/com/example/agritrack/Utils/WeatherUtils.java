package com.example.agritrack.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class WeatherUtils {

    // VOTRE CLÉ API - REMPLACEZ AVEC LA VÔTRE
    public static final String DEFAULT_API_KEY = "4249ad1cd9e98c07ca57865343e48bb2";

    public static void saveApiKey(Context context, String apiKey) {
        SharedPreferences prefs = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("api_key", apiKey).apply();
    }

    public static String getApiKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE);
        // Utilisez votre clé API ici
        return prefs.getString("api_key", DEFAULT_API_KEY);
    }

    // Conseils agricoles selon la météo
    public static String getAgriculturalAdvice(String weatherMain, double temp, int humidity) {
        StringBuilder advice = new StringBuilder();

        if (weatherMain == null) {
            return "✅ Conditions normales pour l'agriculture.";
        }

        // Titre avec température
        advice.append("🌡️ Température: ").append(String.format("%.1f°C", temp)).append("\n\n");

        // Conseils selon la température
        if (temp > 30) {
            advice.append("⚠️ CHALEUR EXTRÊME\n");
            advice.append("• Arroser avant 7h ou après 19h\n");
            advice.append("• Protéger les animaux du soleil\n");
            advice.append("• Éviter les traitements chimiques\n");
        } else if (temp < 5) {
            advice.append("❄️ RISQUE DE GEL\n");
            advice.append("• Couvrir les cultures sensibles\n");
            advice.append("• Rentrer le bétail la nuit\n");
            advice.append("• Protéger les systèmes d'irrigation\n");
        } else if (temp >= 15 && temp <= 25) {
            advice.append("✅ TEMPS IDÉAL\n");
            advice.append("• Conditions parfaites pour les semis\n");
            advice.append("• Bonne croissance végétative\n");
            advice.append("• Travaux agricoles recommandés\n");
        }

        // Conseils selon le temps
        switch (weatherMain.toLowerCase()) {
            case "rain":
                advice.append("\n🌧️ PLUIE PRÉVUE\n");
                advice.append("• Économisez l'irrigation\n");
                advice.append("• Reportez les épandages\n");
                advice.append("• Vérifiez le drainage\n");
                break;

            case "clear":
                advice.append("\n☀️ CIEL DÉGAGÉ\n");
                advice.append("• Idéal pour les récoltes\n");
                advice.append("• Séchage rapide du fourrage\n");
                advice.append("• Photosynthèse optimale\n");
                break;

            case "clouds":
                advice.append("\n☁️ NUAGEUX\n");
                advice.append("• Luminosité douce pour croissance\n");
                advice.append("• Évaporation réduite\n");
                advice.append("• Bon pour transplantations\n");
                break;

            case "snow":
                advice.append("\n❄️ NEIGE\n");
                advice.append("• Protégez les arbres fruitiers\n");
                advice.append("• Isolez les serres\n");
                advice.append("• Nourriture énergétique bétail\n");
                break;

            case "thunderstorm":
                advice.append("\n⚡ ORAGE\n");
                advice.append("• RENTREZ LE BÉTAIL\n");
                advice.append("• Débranchez les équipements\n");
                advice.append("• Évitez les champs ouverts\n");
                break;

            case "mist":
            case "fog":
            case "haze":
                advice.append("\n🌫️ BROUILLARD\n");
                advice.append("• Surveillance maladies fongiques\n");
                advice.append("• Aération des bâtiments\n");
                advice.append("• Évitez les traitements foliaires\n");
                break;
        }

        // Conseils sur l'humidité
        advice.append("\n💧 Humidité: ").append(humidity).append("%\n");
        if (humidity > 80) {
            advice.append("• Risque élevé de mildiou/oïdium\n");
            advice.append("• Surveillez les cultures sensibles\n");
        } else if (humidity < 40) {
            advice.append("• Augmentez la fréquence d'arrosage\n");
            advice.append("• Surveillez le stress hydrique\n");
        } else {
            advice.append("• Niveau d'humidité optimal\n");
        }

        return advice.toString();
    }
}