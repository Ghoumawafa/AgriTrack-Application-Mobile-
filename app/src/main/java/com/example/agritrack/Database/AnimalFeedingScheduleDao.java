package com.example.agritrack.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface AnimalFeedingScheduleDao {

    @Query("SELECT * FROM animal_feeding_schedules WHERE feeding_date = :date ORDER BY scheduled_time")
    List<AnimalFeedingScheduleEntity> getSchedulesForDate(String date);

    @Query("SELECT * FROM animal_feeding_schedules WHERE animal_id = :animalId AND feeding_date = :date ORDER BY scheduled_time")
    List<AnimalFeedingScheduleEntity> getSchedulesForAnimalAndDate(long animalId, String date);

    @Query("SELECT * FROM animal_feeding_schedules WHERE feeding_date = :date AND is_fed = 0 AND is_skipped = 0 ORDER BY scheduled_time")
    List<AnimalFeedingScheduleEntity> getPendingFeedingsForDate(String date);

    @Query("SELECT * FROM animal_feeding_schedules WHERE feeding_date = :date AND is_fed = 1 ORDER BY scheduled_time")
    List<AnimalFeedingScheduleEntity> getCompletedFeedingsForDate(String date);

    @Query("SELECT * FROM animal_feeding_schedules WHERE feeding_date = :date AND is_skipped = 1 ORDER BY scheduled_time")
    List<AnimalFeedingScheduleEntity> getSkippedFeedingsForDate(String date);


    @Query("SELECT * FROM animal_feeding_schedules ORDER BY feeding_date, scheduled_time")
    List<AnimalFeedingScheduleEntity> getAll();

    @Insert
    long insert(AnimalFeedingScheduleEntity schedule);

    @Insert
    void insertAll(List<AnimalFeedingScheduleEntity> schedules);

    @Update
    int update(AnimalFeedingScheduleEntity schedule);

    @Query("DELETE FROM animal_feeding_schedules WHERE feeding_date < :date")
    int deleteOldSchedules(String date);

    @Query("SELECT COUNT(*) FROM animal_feeding_schedules WHERE animal_id IN (SELECT id FROM animals WHERE species = :species) AND feeding_date = :date")
    int getMealCountForSpecies(String species, String date);
}