package com.example.agritrack.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.database.Cursor;

@Database(
        entities = {
        UserAccountEntity.class,
        TerrainEntity.class,
        EquipmentEntity.class, AnimalEntity.class
        },
    version = 4,
        exportSchema = false
)
public abstract class AgriTrackRoomDatabase extends RoomDatabase {

    public abstract UserAccountDao userAccountDao();

    public abstract TerrainDao terrainDao();

    public abstract EquipmentDao equipmentDao();
    public abstract AnimalDao animalDao();

    private static volatile AgriTrackRoomDatabase INSTANCE;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `terrains` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`location` TEXT NOT NULL, " +
                            "`area` REAL NOT NULL DEFAULT 0.0, " +
                            "`soil_type` TEXT NOT NULL" +
                            ")"
            );

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `equipments` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`purchase_date` TEXT NOT NULL, " +
                            "`cost` REAL NOT NULL DEFAULT 0.0, " +
                            "`status` TEXT NOT NULL, " +
                            "`usage_hours` INTEGER NOT NULL DEFAULT 0" +
                            ")"
            );
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            boolean hasLatitude = false;
            boolean hasLongitude = false;

            Cursor cursor = database.query("PRAGMA table_info(`terrains`)");
            try {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    String colName = cursor.getString(nameIndex);
                    if ("latitude".equals(colName)) {
                        hasLatitude = true;
                    } else if ("longitude".equals(colName)) {
                        hasLongitude = true;
                    }
                }
            } finally {
                cursor.close();
            }

            if (!hasLatitude) {
                database.execSQL("ALTER TABLE `terrains` ADD COLUMN `latitude` REAL");
            }
            if (!hasLongitude) {
                database.execSQL("ALTER TABLE `terrains` ADD COLUMN `longitude` REAL");
            }
        }
    };

    // Nouvelle migration pour la version 4
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Créer la table animals
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `animals` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`species` TEXT NOT NULL, " +
                            "`breed` TEXT NOT NULL, " +
                            "`birth_date` TEXT NOT NULL, " +
                            "`weight` REAL NOT NULL DEFAULT 0.0, " +
                            "`gender` TEXT NOT NULL, " +
                            "`health_status` TEXT NOT NULL" +
                            ")"
            );

            // Créer la table animal_foods
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `animal_foods` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`quantity` REAL NOT NULL DEFAULT 0.0, " +
                            "`unit` TEXT NOT NULL, " +
                            "`cost` REAL NOT NULL DEFAULT 0.0, " +
                            "`alert_quantity` REAL NOT NULL DEFAULT 0.0" +
                            ")"
            );
        }
    };

    public static AgriTrackRoomDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AgriTrackRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AgriTrackRoomDatabase.class,
                                    "agritrack_room.db"
                            )
                            // Learning project: keeps API synchronous (matches existing StorageHelper).
                            // For production, prefer background threads / coroutines.
                            .allowMainThreadQueries()
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
