package com.example.agritrack.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface AnimalFoodPlanDao {

    @Query("SELECT * FROM animal_food_plans WHERE species = :species AND category = :category AND age_category = :ageCategory AND :weight BETWEEN min_weight AND max_weight LIMIT 1")
    AnimalFoodPlanEntity findPlanForAnimal(String species, String category, String ageCategory, double weight);

    @Query("SELECT * FROM animal_food_plans WHERE species = :species")
    List<AnimalFoodPlanEntity> getPlansBySpecies(String species);

    @Query("SELECT * FROM animal_food_plans")
    List<AnimalFoodPlanEntity> getAllPlans();

    @Insert
    long insert(AnimalFoodPlanEntity plan);

    @Update
    int update(AnimalFoodPlanEntity plan);
}