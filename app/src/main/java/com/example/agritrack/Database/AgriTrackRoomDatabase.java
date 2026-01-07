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
import com.example.agritrack.Models.Transaction;

@Database(
        entities = {
                UserAccountEntity.class,
                TerrainEntity.class,
                EquipmentEntity.class,
        Transaction.class,
        PlantEntity.class,
        IrrigationEntity.class
        },
    version = 5,
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
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
