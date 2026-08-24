package com.example.cs499project;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface WeightDao {
    // find all weights for a specific user
    @Query("SELECT * FROM weights WHERE uid = :uid ORDER BY wid")
    List<Weight> getWeightsForUser(int uid);

    // gets the most recent weight
    @Query("SELECT * FROM weights WHERE uid = :uid ORDER BY wid DESC LIMIT 1")
    Weight getLatestWeight(int uid);

    // delete specific weight
    @Query("DELETE FROM weights WHERE wid = :wid")
    void deleteById(int wid);

    // delete all weight of a user
    @Query("DELETE FROM weights WHERE uid = :uid")
    void deleteUserData(int uid);

    // insert a weight
    @Insert
    void insert(Weight weight);
}