package com.example.agritrack.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserAccountEntity user);

    @Query("SELECT COUNT(*) FROM user_accounts WHERE email = :email")
    int countByEmail(String email);

    @Query("SELECT password FROM user_accounts WHERE email = :email LIMIT 1")
    String getPasswordByEmail(String email);

    @Query("SELECT name FROM user_accounts WHERE email = :email LIMIT 1")
    String getNameByEmail(String email);

    @Query("DELETE FROM user_accounts")
    void deleteAll();
}
