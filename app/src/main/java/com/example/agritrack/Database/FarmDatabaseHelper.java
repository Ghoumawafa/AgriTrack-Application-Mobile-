package com.example.agritrack.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FarmDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "agritrack.db";
    private static final int DATABASE_VERSION = 1;

    // Tables communes
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_ROLE = "role";

    public FarmDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table utilisateurs
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL,"
                + COLUMN_USER_NAME + " TEXT NOT NULL,"
                + COLUMN_USER_ROLE + " TEXT DEFAULT 'user'"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Insérer un utilisateur admin de base
        db.execSQL("INSERT INTO " + TABLE_USERS + " (email, name, role) " +
                "VALUES ('admin@agritrack.com', 'Administrateur', 'admin')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}