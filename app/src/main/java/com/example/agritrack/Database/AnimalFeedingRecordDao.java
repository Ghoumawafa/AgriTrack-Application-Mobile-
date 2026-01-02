package com.example.agritrack.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AnimalFeedingRecordDao {
    @Insert
    long insert(AnimalFeedingRecordEntity record);

    @Query("SELECT * FROM animal_feeding_records ORDER BY record_date DESC, record_time DESC")
    List<AnimalFeedingRecordEntity> getAll();

    @Query("SELECT * FROM animal_feeding_records WHERE record_date = :date ORDER BY record_time DESC")
    List<AnimalFeedingRecordEntity> getByDate(String date);

    @Query("SELECT * FROM animal_feeding_records WHERE animal_id = :animalId ORDER BY record_date DESC, record_time DESC")
    List<AnimalFeedingRecordEntity> getByAnimal(long animalId);

    @Query("SELECT COUNT(*) FROM animal_feeding_records WHERE record_date = :date AND status = 'FED'")
    int getFedCountByDate(String date);

    @Query("SELECT COUNT(*) FROM animal_feeding_records WHERE record_date = :date AND status = 'SKIPPED'")
    int getSkippedCountByDate(String date);
}
