package com.example.agritrack.Database;

import android.content.Context;
import android.database.Cursor;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.agritrack.Database.UserAccountDao;
import com.example.agritrack.Database.TerrainDao;
import com.example.agritrack.Database.EquipmentDao;
import com.example.agritrack.Dao.TransactionDao;
import com.example.agritrack.Database.UserAccountEntity;
import com.example.agritrack.Database.TerrainEntity;
import com.example.agritrack.Database.EquipmentEntity;
import com.example.agritrack.Database.IrrigationDao;
import com.example.agritrack.Database.IrrigationEntity;
import com.example.agritrack.Database.PlantDao;
import com.example.agritrack.Database.PlantEntity;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.Database.AnimalFoodPlanDao;
import com.example.agritrack.Database.AnimalFoodPlanEntity;
import com.example.agritrack.Database.AnimalFeedingScheduleDao;
import com.example.agritrack.Database.AnimalFeedingScheduleEntity;
import com.example.agritrack.Database.AnimalFeedingRecordDao;
import com.example.agritrack.Database.AnimalFeedingRecordEntity;
import com.example.agritrack.Models.Transaction;

@Database(
        entities = {
                UserAccountEntity.class,
                TerrainEntity.class,
                EquipmentEntity.class,
                Transaction.class,
                PlantEntity.class,
                IrrigationEntity.class,
                AnimalEntity.class,
                AnimalFoodPlanEntity.class,
                AnimalFeedingScheduleEntity.class,
                AnimalFeedingRecordEntity.class
        },
        version = 7,
        exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class AgriTrackRoomDatabase extends RoomDatabase {

    public abstract UserAccountDao userAccountDao();
    public abstract TerrainDao terrainDao();
    public abstract EquipmentDao equipmentDao();
    public abstract TransactionDao transactionDao();
    public abstract PlantDao plantDao();
    public abstract IrrigationDao irrigationDao();
    public abstract AnimalDao animalDao();
    public abstract AnimalFoodPlanDao animalFoodPlanDao();
    public abstract AnimalFeedingScheduleDao animalFeedingScheduleDao();
    public abstract AnimalFeedingRecordDao animalFeedingRecordDao();

    private static volatile AgriTrackRoomDatabase INSTANCE;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `terrains` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`location` TEXT NOT NULL, " +
                    "`area` REAL NOT NULL DEFAULT 0.0, " +
                    "`soil_type` TEXT NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `equipments` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`type` TEXT NOT NULL, " +
                    "`purchase_date` TEXT NOT NULL, " +
                    "`cost` REAL NOT NULL DEFAULT 0.0, " +
                    "`status` TEXT NOT NULL, " +
                    "`usage_hours` INTEGER NOT NULL DEFAULT 0)");
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
                    if ("latitude".equals(colName)) hasLatitude = true;
                    if ("longitude".equals(colName)) hasLongitude = true;
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

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`amount` REAL NOT NULL, " +
                    "`date` TEXT NOT NULL, " +
                    "`description` TEXT, " +
                    "`category` TEXT, " +
                    "`type` TEXT NOT NULL)");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `plants` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT, " +
                    "`type` TEXT, " +
                    "`growthStage` TEXT, " +
                    "`quantity` INTEGER NOT NULL DEFAULT 0, " +
                    "`location` TEXT, " +
                    "`plantingDate` TEXT)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `irrigations` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`terrainName` TEXT, " +
                    "`irrigationDate` INTEGER, " +
                    "`waterQuantity` REAL NOT NULL DEFAULT 0.0, " +
                    "`method` TEXT, " +
                    "`status` TEXT, " +
                    "`notes` TEXT)");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add new columns to irrigations table for IoT features
            database.execSQL("ALTER TABLE `irrigations` ADD COLUMN `sensorPin` INTEGER NOT NULL DEFAULT 4");
            database.execSQL("ALTER TABLE `irrigations` ADD COLUMN `actuatorPin` INTEGER NOT NULL DEFAULT 2");
            database.execSQL("ALTER TABLE `irrigations` ADD COLUMN `sensorValue` INTEGER NOT NULL DEFAULT 2000");
            database.execSQL("ALTER TABLE `irrigations` ADD COLUMN `waterUsed` REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE `irrigations` ADD COLUMN `durationMinutes` INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create animals table
            database.execSQL("CREATE TABLE IF NOT EXISTS `animals` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`species` TEXT NOT NULL, " +
                    "`breed` TEXT NOT NULL, " +
                    "`birth_date` TEXT NOT NULL, " +
                    "`weight` REAL NOT NULL DEFAULT 0.0, " +
                    "`gender` TEXT NOT NULL, " +
                    "`health_status` TEXT NOT NULL)");

            // Create animal_food_plans table
            database.execSQL("CREATE TABLE IF NOT EXISTS `animal_food_plans` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`species` TEXT NOT NULL, " +
                    "`category` TEXT NOT NULL, " +
                    "`age_category` TEXT NOT NULL, " +
                    "`min_weight` REAL NOT NULL DEFAULT 0.0, " +
                    "`max_weight` REAL NOT NULL DEFAULT 0.0, " +
                    "`total_daily_food` REAL NOT NULL DEFAULT 0.0, " +
                    "`hay_percentage` REAL NOT NULL DEFAULT 0.0, " +
                    "`grains_percentage` REAL NOT NULL DEFAULT 0.0, " +
                    "`supplements_percentage` REAL NOT NULL DEFAULT 0.0, " +
                    "`water_liters` REAL NOT NULL DEFAULT 0.0, " +
                    "`feeding_times` TEXT NOT NULL, " +
                    "`meals_per_day` INTEGER NOT NULL DEFAULT 0, " +
                    "`recommendations` TEXT, " +
                    "`estimated_cost_per_day` REAL NOT NULL DEFAULT 0.0)");

            // Create animal_feeding_schedules table
            database.execSQL("CREATE TABLE IF NOT EXISTS `animal_feeding_schedules` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`animal_id` INTEGER NOT NULL, " +
                    "`plan_id` INTEGER, " +
                    "`meal_number` INTEGER NOT NULL, " +
                    "`total_meals` INTEGER NOT NULL, " +
                    "`feeding_date` TEXT NOT NULL, " +
                    "`scheduled_time` TEXT NOT NULL, " +
                    "`hay_quantity` REAL NOT NULL DEFAULT 0.0, " +
                    "`grains_quantity` REAL NOT NULL DEFAULT 0.0, " +
                    "`supplements_quantity` REAL NOT NULL DEFAULT 0.0, " +
                    "`water_quantity` REAL NOT NULL DEFAULT 0.0, " +
                    "`is_fed` INTEGER NOT NULL DEFAULT 0, " +
                    "`is_skipped` INTEGER NOT NULL DEFAULT 0, " +
                    "`fed_time` TEXT, " +
                    "`fed_by` TEXT, " +
                    "FOREIGN KEY(`animal_id`) REFERENCES `animals`(`id`) ON DELETE CASCADE, " +
                    "FOREIGN KEY(`plan_id`) REFERENCES `animal_food_plans`(`id`) ON DELETE SET NULL)");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_animal_feeding_schedules_animal_id` ON `animal_feeding_schedules` (`animal_id`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_animal_feeding_schedules_plan_id` ON `animal_feeding_schedules` (`plan_id`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_animal_feeding_schedules_feeding_date_scheduled_time` ON `animal_feeding_schedules` (`feeding_date`, `scheduled_time`)");

            // Create animal_feeding_records table
            database.execSQL("CREATE TABLE IF NOT EXISTS `animal_feeding_records` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`animal_id` INTEGER NOT NULL, " +
                    "`schedule_id` INTEGER, " +
                    "`record_date` TEXT NOT NULL, " +
                    "`record_time` TEXT NOT NULL, " +
                    "`status` TEXT NOT NULL, " +
                    "`quantity_given` REAL NOT NULL DEFAULT 0.0, " +
                    "`leftovers` REAL NOT NULL DEFAULT 0.0, " +
                    "`fed_by` TEXT, " +
                    "`notes` TEXT, " +
                    "FOREIGN KEY(`animal_id`) REFERENCES `animals`(`id`) ON DELETE CASCADE, " +
                    "FOREIGN KEY(`schedule_id`) REFERENCES `animal_feeding_schedules`(`id`) ON DELETE CASCADE)");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_animal_feeding_records_animal_id` ON `animal_feeding_records` (`animal_id`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_animal_feeding_records_schedule_id` ON `animal_feeding_records` (`schedule_id`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_animal_feeding_records_record_date_record_time` ON `animal_feeding_records` (`record_date`, `record_time`)");
        }
    };

    public static AgriTrackRoomDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AgriTrackRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AgriTrackRoomDatabase.class,
                                    "agritrack.db"
                            )
                            .allowMainThreadQueries()
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
