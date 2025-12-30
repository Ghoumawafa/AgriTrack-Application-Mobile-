
package com.example.agritrack.Database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.agritrack.Models.Irrigation;
import com.example.agritrack.Models.Plant;
import com.example.agritrack.Models.PlantTreatment;

/**
 * Room Database for AgriTrack application
 * All database operations should be performed on background threads
 * Version 3: Added Plant and PlantTreatment entities with indexes
 */
@Database(
    entities = {
        Irrigation.class,
        Plant.class,
        PlantTreatment.class
    },
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract IrrigationDao irrigationDao();
    public abstract PlantDao plantDao();
    public abstract PlantTreatmentDao plantTreatmentDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "agritrack_db"
                    )
                    .fallbackToDestructiveMigration()
                    // Removed allowMainThreadQueries() - all DB operations must be on background threads
                    .build();
        }
        return INSTANCE;
    }
}