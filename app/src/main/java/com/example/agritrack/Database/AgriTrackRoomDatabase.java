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
                EquipmentEntity.class,
                AnimalEntity.class,
                AnimalFoodPlanEntity.class,
                AnimalFeedingScheduleEntity.class,
                AnimalFeedingRecordEntity.class
        },
        version = 10,
        exportSchema = false
)
public abstract class AgriTrackRoomDatabase extends RoomDatabase {

    public abstract UserAccountDao userAccountDao();
    public abstract TerrainDao terrainDao();
    public abstract EquipmentDao equipmentDao();
    public abstract AnimalDao animalDao();
    public abstract AnimalFoodPlanDao animalFoodPlanDao();
    public abstract AnimalFeedingScheduleDao animalFeedingScheduleDao();
    public abstract AnimalFeedingRecordDao animalFeedingRecordDao();



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

    // ⚠️ CORRECTION : Migration 3_4 avec les BONS CHAMPS
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

            // ⚠️ CORRECTION : Les champs doivent correspondre à AnimalFoodEntity
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `animal_foods` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`animalId` INTEGER NOT NULL, " +
                            "`foodType` TEXT NOT NULL, " +
                            "`quantity` REAL NOT NULL DEFAULT 0.0, " +
                            "`feedingTime` TEXT NOT NULL, " +
                            "`feedingDate` TEXT NOT NULL, " +
                            "`notes` TEXT, " +
                            "FOREIGN KEY(`animalId`) REFERENCES `animals`(`id`) ON DELETE CASCADE" +
                            ")"
            );
        }
    };

    // ⚠️ AJOUTER : Nouvelle migration 4_5 pour les corrections
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Vérifier si la table animal_foods existe avec les anciens champs
            Cursor cursor = database.query(
                    "PRAGMA table_info(`animal_foods`)"
            );

            boolean hasCorrectFields = false;
            boolean hasAnimalId = false;

            try {
                while (cursor.moveToNext()) {
                    String columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    if ("animalId".equals(columnName)) {
                        hasAnimalId = true;
                    }
                }
                hasCorrectFields = hasAnimalId;
            } finally {
                cursor.close();
            }

            // Si la table n'a pas les bons champs, la recréer
            if (!hasCorrectFields) {
                // Supprimer l'ancienne table
                database.execSQL("DROP TABLE IF EXISTS `animal_foods`");

                // Recréer avec les bons champs
                database.execSQL(
                        "CREATE TABLE `animal_foods` (" +
                                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "`animalId` INTEGER NOT NULL, " +
                                "`foodType` TEXT NOT NULL, " +
                                "`quantity` REAL NOT NULL DEFAULT 0.0, " +
                                "`feedingTime` TEXT NOT NULL, " +
                                "`feedingDate` TEXT NOT NULL, " +
                                "`notes` TEXT, " +
                                "FOREIGN KEY(`animalId`) REFERENCES `animals`(`id`) ON DELETE CASCADE" +
                                ")"
                );
            }
        }
    };


    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 1. Supprimer et recréer animal_foods avec tous les champs
            database.execSQL("DROP TABLE IF EXISTS animal_foods");

            database.execSQL(
                    "CREATE TABLE animal_foods (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "animal_id INTEGER NOT NULL, " +
                            "plan_id INTEGER, " +
                            "meal_number INTEGER NOT NULL DEFAULT 1, " +
                            "total_meals INTEGER NOT NULL DEFAULT 1, " +
                            "hay_quantity REAL NOT NULL DEFAULT 0, " +
                            "grains_quantity REAL NOT NULL DEFAULT 0, " +
                            "supplements_quantity REAL NOT NULL DEFAULT 0, " +
                            "water_quantity REAL NOT NULL DEFAULT 0, " +
                            "food_type TEXT, " +
                            "ration_type TEXT, " +
                            "animal_species TEXT, " +
                            "animal_age_category TEXT, " +
                            "feeding_date TEXT, " +
                            "scheduled_time TEXT, " +
                            "actual_time TEXT, " +
                            "feeding_time TEXT, " +
                            "is_fed INTEGER NOT NULL DEFAULT 0, " +
                            "fed_by TEXT, " +
                            "is_skipped INTEGER NOT NULL DEFAULT 0, " +
                            "skip_reason TEXT, " +
                            "estimated_cost REAL NOT NULL DEFAULT 0, " +
                            "leftovers REAL NOT NULL DEFAULT 0, " +
                            "nutritional_value REAL NOT NULL DEFAULT 0, " +
                            "notes TEXT, " +
                            "quantity REAL NOT NULL DEFAULT 0, " +
                            "FOREIGN KEY(animal_id) REFERENCES animals(id) ON DELETE CASCADE" +
                            ")"
            );
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_foods_animal_id ON animal_foods(animal_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_foods_plan_id ON animal_foods(plan_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_foods_meal_number ON animal_foods(meal_number)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_foods_feeding_date_scheduled_time ON animal_foods(feeding_date, scheduled_time)");

            // 2. Créer animal_food_plans avec TOUS les champs
            database.execSQL(
                    "CREATE TABLE animal_food_plans (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "species TEXT NOT NULL, " +
                            "category TEXT NOT NULL, " +
                            "age_category TEXT NOT NULL, " +
                            "min_weight REAL NOT NULL, " +
                            "max_weight REAL NOT NULL, " +
                            "total_daily_food REAL NOT NULL, " +
                            "hay_percentage REAL NOT NULL DEFAULT 0, " +
                            "grains_percentage REAL NOT NULL DEFAULT 0, " +
                            "supplements_percentage REAL NOT NULL DEFAULT 0, " +
                            "water_liters REAL NOT NULL DEFAULT 0, " +
                            "feeding_times TEXT NOT NULL, " +
                            "meals_per_day INTEGER NOT NULL DEFAULT 1, " +
                            "recommendations TEXT, " +
                            "health_conditions TEXT, " +
                            "estimated_cost_per_day REAL NOT NULL DEFAULT 0" +
                            ")"
            );

            // 3. Insérer des plans par défaut COMPLETS
            insertDefaultPlans(database);
        }

        private void insertDefaultPlans(SupportSQLiteDatabase database) {
            // Plan pour vache laitière
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Vache', 'Laitière', 'Adulte', 500, 600, 12.5, 60, 35, 5, 50, " +
                            "'[\"06:00\", \"12:00\", \"18:00\"]', 3, " +
                            "'Augmenter grains pendant lactation', 8.5)"
            );

            // Plan pour vache à viande
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Vache', 'Viande', 'Adulte', 400, 500, 10.0, 70, 25, 5, 40, " +
                            "'[\"07:00\", \"17:00\"]', 2, " +
                            "'Régime riche en foin', 6.5)"
            );

            // Plan pour mouton
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Mouton', 'Viande', 'Adulte', 50, 80, 2.5, 80, 15, 5, 10, " +
                            "'[\"08:00\", \"16:00\"]', 2, " +
                            "'Pâturage recommandé', 1.5)"
            );

            // Plan pour chèvre
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Chèvre', 'Laitière', 'Adulte', 40, 60, 3.0, 50, 40, 10, 15, " +
                            "'[\"07:00\", \"13:00\", \"19:00\"]', 3, " +
                            "'Riche en protéines', 2.5)"
            );

            // Plan pour poule
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Poule', 'Pondeuse', 'Adulte', 1.5, 2.5, 0.12, 0, 80, 20, 0.5, " +
                            "'[\"06:00\", \"15:00\"]', 2, " +
                            "'Riche en calcium', 0.15)"
            );
        }
    };
    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE IF EXISTS animal_foods");
            database.execSQL("DROP TABLE IF EXISTS animal_food_plans");

            database.execSQL(
                    "CREATE TABLE animal_foods (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "animal_id INTEGER NOT NULL, " +
                            "plan_id INTEGER, " +
                            "meal_number INTEGER NOT NULL DEFAULT 1, " +
                            "total_meals INTEGER NOT NULL DEFAULT 1, " +
                            "hay_quantity REAL NOT NULL DEFAULT 0, " +
                            "grains_quantity REAL NOT NULL DEFAULT 0, " +
                            "supplements_quantity REAL NOT NULL DEFAULT 0, " +
                            "water_quantity REAL NOT NULL DEFAULT 0, " +
                            "food_type TEXT, " +
                            "ration_type TEXT, " +
                            "animal_species TEXT, " +
                            "animal_age_category TEXT, " +
                            "feeding_date TEXT, " +
                            "scheduled_time TEXT, " +
                            "actual_time TEXT, " +
                            "feeding_time TEXT, " +
                            "is_fed INTEGER NOT NULL DEFAULT 0, " +
                            "fed_by TEXT, " +
                            "is_skipped INTEGER NOT NULL DEFAULT 0, " +
                            "skip_reason TEXT, " +
                            "estimated_cost REAL NOT NULL DEFAULT 0, " +
                            "leftovers REAL NOT NULL DEFAULT 0, " +
                            "nutritional_value REAL NOT NULL DEFAULT 0, " +
                            "notes TEXT, " +
                            "quantity REAL NOT NULL DEFAULT 0, " +
                            "FOREIGN KEY(animal_id) REFERENCES animals(id) ON DELETE CASCADE" +
                            ")"
            );
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_foods_animal_id ON animal_foods(animal_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_foods_plan_id ON animal_foods(plan_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_foods_meal_number ON animal_foods(meal_number)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_foods_feeding_date_scheduled_time ON animal_foods(feeding_date, scheduled_time)");

            database.execSQL(
                    "CREATE TABLE animal_food_plans (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "species TEXT NOT NULL, " +
                            "category TEXT NOT NULL, " +
                            "age_category TEXT NOT NULL, " +
                            "min_weight REAL NOT NULL, " +
                            "max_weight REAL NOT NULL, " +
                            "total_daily_food REAL NOT NULL, " +
                            "hay_percentage REAL NOT NULL DEFAULT 0, " +
                            "grains_percentage REAL NOT NULL DEFAULT 0, " +
                            "supplements_percentage REAL NOT NULL DEFAULT 0, " +
                            "water_liters REAL NOT NULL DEFAULT 0, " +
                            "feeding_times TEXT NOT NULL, " +
                            "meals_per_day INTEGER NOT NULL DEFAULT 1, " +
                            "recommendations TEXT, " +
                            "health_conditions TEXT, " +
                            "estimated_cost_per_day REAL NOT NULL DEFAULT 0" +
                            ")"
            );

            // Réinsérer les plans par défaut
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Vache', 'Laitière', 'Adulte', 500, 600, 12.5, 60, 35, 5, 50, " +
                            "'[\"06:00\", \"12:00\", \"18:00\"]', 3, 'Augmenter grains pendant lactation', 8.5)"
            );
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Vache', 'Viande', 'Adulte', 400, 500, 10.0, 70, 25, 5, 40, " +
                            "'[\"07:00\", \"17:00\"]', 2, 'Régime riche en foin', 6.5)"
            );
        }
    };
    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 1. Supprimer les anciennes tables si elles existent
            database.execSQL("DROP TABLE IF EXISTS animal_foods");
            database.execSQL("DROP TABLE IF EXISTS animal_food_plans");

            // 2. Créer la table animal_food_plans
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS animal_food_plans (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "species TEXT NOT NULL, " +
                            "category TEXT NOT NULL, " +
                            "age_category TEXT NOT NULL, " +
                            "min_weight REAL NOT NULL, " +
                            "max_weight REAL NOT NULL, " +
                            "total_daily_food REAL NOT NULL, " +
                            "hay_percentage REAL NOT NULL DEFAULT 0, " +
                            "grains_percentage REAL NOT NULL DEFAULT 0, " +
                            "supplements_percentage REAL NOT NULL DEFAULT 0, " +
                            "water_liters REAL NOT NULL DEFAULT 0, " +
                            "feeding_times TEXT NOT NULL, " +
                            "meals_per_day INTEGER NOT NULL DEFAULT 1, " +
                            "recommendations TEXT, " +
                            "estimated_cost_per_day REAL NOT NULL DEFAULT 0" +
                            ")"
            );

            // 3. Créer la table animal_feeding_schedules
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS animal_feeding_schedules (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "animal_id INTEGER NOT NULL, " +
                            "plan_id INTEGER, " +
                            "meal_number INTEGER NOT NULL DEFAULT 1, " +
                            "total_meals INTEGER NOT NULL DEFAULT 1, " +
                            "feeding_date TEXT NOT NULL, " +
                            "scheduled_time TEXT NOT NULL, " +
                            "hay_quantity REAL NOT NULL DEFAULT 0, " +
                            "grains_quantity REAL NOT NULL DEFAULT 0, " +
                            "supplements_quantity REAL NOT NULL DEFAULT 0, " +
                            "water_quantity REAL NOT NULL DEFAULT 0, " +
                            "is_fed INTEGER NOT NULL DEFAULT 0, " +
                            "actual_time TEXT, " +
                            "fed_by TEXT, " +
                            "notes TEXT, " +
                            "is_skipped INTEGER NOT NULL DEFAULT 0, " +
                            "skip_reason TEXT, " +
                            "FOREIGN KEY(animal_id) REFERENCES animals(id) ON DELETE CASCADE, " +
                            "FOREIGN KEY(plan_id) REFERENCES animal_food_plans(id) ON DELETE SET NULL" +
                            ")"
            );

            // 4. Créer les index
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_feeding_schedules_animal_id ON animal_feeding_schedules(animal_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_feeding_schedules_plan_id ON animal_feeding_schedules(plan_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_feeding_schedules_feeding_date_scheduled_time ON animal_feeding_schedules(feeding_date, scheduled_time)");

            // 5. Insérer les plans par défaut
            insertDefaultPlansV8(database);
        }

        private void insertDefaultPlansV8(SupportSQLiteDatabase database) {
            // Vache Laitière Adulte
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Vache', 'Laitière', 'Adulte', 500, 700, 15.0, 60, 35, 5, 80, " +
                            "'[\"06:00\", \"12:00\", \"18:00\"]', 3, 'Augmenter grains pendant lactation', 12.0)"
            );

            // Vache Viande Adulte
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Vache', 'Viande', 'Adulte', 400, 600, 12.0, 70, 25, 5, 60, " +
                            "'[\"07:00\", \"17:00\"]', 2, 'Régime riche en foin', 8.0)"
            );

            // Vache Jeune
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Vache', 'Laitière', 'Jeune', 200, 400, 8.0, 65, 30, 5, 40, " +
                            "'[\"06:00\", \"12:00\", \"18:00\"]', 3, 'Croissance rapide', 6.5)"
            );

            // Mouton Adulte
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Mouton', 'Viande', 'Adulte', 50, 90, 2.5, 80, 15, 5, 8, " +
                            "'[\"08:00\", \"16:00\"]', 2, 'Pâturage recommandé', 1.8)"
            );

            // Chèvre Laitière Adulte
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Chèvre', 'Laitière', 'Adulte', 40, 70, 3.5, 50, 40, 10, 12, " +
                            "'[\"07:00\", \"13:00\", \"19:00\"]', 3, 'Riche en protéines pour lactation', 3.2)"
            );

            // Poule Pondeuse
            database.execSQL(
                    "INSERT INTO animal_food_plans (species, category, age_category, min_weight, max_weight, " +
                            "total_daily_food, hay_percentage, grains_percentage, supplements_percentage, water_liters, " +
                            "feeding_times, meals_per_day, recommendations, estimated_cost_per_day) VALUES (" +
                            "'Poule', 'Pondeuse', 'Adulte', 1.5, 2.8, 0.12, 0, 75, 25, 0.3, " +
                            "'[\"07:00\", \"16:00\"]', 2, 'Riche en calcium pour coquilles', 0.18)"
            );
        }
    };
    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS animal_feeding_records (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "animal_id INTEGER NOT NULL, " +
                            "schedule_id INTEGER, " +
                            "record_date TEXT NOT NULL, " +
                            "record_time TEXT NOT NULL, " +
                            "status TEXT NOT NULL, " +
                            "quantity_given REAL NOT NULL DEFAULT 0, " +
                            "leftovers REAL NOT NULL DEFAULT 0, " +
                            "fed_by TEXT, " +
                            "notes TEXT, " +
                            "FOREIGN KEY(animal_id) REFERENCES animals(id) ON DELETE CASCADE, " +
                            "FOREIGN KEY(schedule_id) REFERENCES animal_feeding_schedules(id) ON DELETE CASCADE" +
                            ")"
            );
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_feeding_records_animal_id ON animal_feeding_records(animal_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_feeding_records_schedule_id ON animal_feeding_records(schedule_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_animal_feeding_records_record_date_record_time ON animal_feeding_records(record_date, record_time)");
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
                            .allowMainThreadQueries()
                            // ⚠️ AJOUTER toutes les migrations
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                            // ⚠️ DÉCOMMENTER cette ligne
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
