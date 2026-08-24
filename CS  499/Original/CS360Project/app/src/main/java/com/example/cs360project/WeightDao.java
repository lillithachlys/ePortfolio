package com.example.cs360project;

import java.util.List;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface WeightDao {
    @Query("SELECT * FROM weights WHERE uid = :uid ORDER BY wid")
    List<Weight> getWeightsForUser(int uid);

    @Insert
    void insert(Weight weight);
}