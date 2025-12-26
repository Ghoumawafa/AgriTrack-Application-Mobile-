package com.example.agritrack.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class StorageHelper {
    private static final String PREF_NAME = "AgriTrackPrefs";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public StorageHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUserAccount(String email, String password, String name) {
        String userKey = "user_" + email.toLowerCase().hashCode();
        editor.putString(userKey + "_email", email.toLowerCase());
        editor.putString(userKey + "_password", password);
        editor.putString(userKey + "_name", name);
        editor.apply();
    }

    public boolean validateUserCredentials(String email, String password) {
        String userKey = "user_" + email.toLowerCase().hashCode();
        String savedEmail = sharedPreferences.getString(userKey + "_email", "");
        String savedPassword = sharedPreferences.getString(userKey + "_password", "");
        return email.equalsIgnoreCase(savedEmail) && password.equals(savedPassword);
    }

    public boolean isEmailRegistered(String email) {
        String userKey = "user_" + email.toLowerCase().hashCode();
        return sharedPreferences.contains(userKey + "_email");
    }

    public void setUserLoggedIn(boolean isLoggedIn) {
        editor.putBoolean("is_logged_in", isLoggedIn);
        editor.apply();
    }

    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("is_logged_in", false);
    }

    public void saveUserPreferences(String email, String name, String farmName) {
        editor.putString("user_email", email);
        editor.putString("user_name", name);
        editor.putString("farm_name", farmName);
        editor.apply();
    }

    public String getUserName() {
        return sharedPreferences.getString("user_name", "Agriculteur");
    }

    public String getFarmName() {
        return sharedPreferences.getString("farm_name", "Ma Ferme");
    }

    public String getUserNameByEmail(String email) {
        String userKey = "user_" + email.toLowerCase().hashCode();
        return sharedPreferences.getString(userKey + "_name", "Agriculteur");
    }

    public void clearUserData() {
        editor.clear();
        editor.apply();
    }
}
