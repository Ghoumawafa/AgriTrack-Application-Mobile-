package com.example.agritrack.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.agritrack.Models.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getTransactionById(int id);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    // ✅ NOUVEAU : pour filtrer par utilisateur
    @Query("SELECT * FROM transactions WHERE userEmail = :email ORDER BY date DESC")
    List<Transaction> getTransactionsForUser(String email);

    // ✅ NOUVEAU : totaux par utilisateur
    @Query("SELECT SUM(amount) FROM transactions WHERE userEmail = :email AND category = 'Revenu'")
    Double getTotalRevenusForUser(String email);

    @Query("SELECT SUM(amount) FROM transactions WHERE userEmail = :email AND category = 'Dépense'")
    Double getTotalDepensesForUser(String email);
}
