package com.example.agritrack.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.UserAccountDao;
import com.example.agritrack.Database.UserAccountEntity;

public class StorageHelper {
    private static final String PREF_NAME = "AgriTrackPrefs";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private final UserAccountDao userAccountDao;

    public StorageHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        userAccountDao = AgriTrackRoomDatabase.getInstance(context).userAccountDao();
    }

    public void saveUserAccount(String email, String password, String name) {
        if (email == null) {
            return;
        }
        String normalizedEmail = email.toLowerCase();
        userAccountDao.upsert(new UserAccountEntity(normalizedEmail, password, name));
    }

    public boolean validateUserCredentials(String email, String password) {
        if (email == null) {
            return false;
        }
        String normalizedEmail = email.toLowerCase();
        String savedPassword = userAccountDao.getPasswordByEmail(normalizedEmail);
        if (savedPassword == null) {
            return false;
        }
        return password.equals(savedPassword);
    }

    public boolean isEmailRegistered(String email) {
        if (email == null) {
            return false;
        }
        String normalizedEmail = email.toLowerCase();
        return userAccountDao.countByEmail(normalizedEmail) > 0;
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
        if (email == null) {
            return "Agriculteur";
        }
        String normalizedEmail = email.toLowerCase();
        String name = userAccountDao.getNameByEmail(normalizedEmail);
        return name != null ? name : "Agriculteur";
    }

    public void clearUserData() {
        editor.clear();
        editor.apply();
    }
}
