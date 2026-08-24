package com.example.cs499project;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    // find for logging in
    @Query("SELECT * FROM users WHERE username = :username " + "AND password = :password LIMIT 1")
    User findByLogin(String username, String password);

    // find for registration
    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    User findByUsername(String username);

    // delete specific user
    @Query("DELETE FROM users WHERE uid = :uid")
    void deleteById(int uid);

    // insert user credentials
    @Insert
    void insertAll(User... users);
}
