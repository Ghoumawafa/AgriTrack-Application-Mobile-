
package com.example.agritrack.Database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.agritrack.Models.Irrigation;

/**
 * Room Database for AgriTrack application
 * All database operations should be performed on background threads
 * Version 2: Added indexes for performance optimization
 */
@Database(entities = {Irrigation.class}, version = 2, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract IrrigationDao irrigationDao();

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