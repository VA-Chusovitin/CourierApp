package com.example.courierapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DatabaseRequests {

    //LoginDetails
    @Query("SELECT * FROM Users WHERE authorized = 1")
    List<Users> getAuthorizedUser();

    @Query("SELECT * FROM Users WHERE login = :login OR email = :login")
    Users findUser(String login);

    @Query("SELECT * FROM Users")
    List<Users> getAllUser();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(Users user);

    @Update
    void updateUser(Users user);

    @Delete()
    int deleteUser(Users users);

    @Query("DELETE FROM Users")
    int deleteAllUsers();
}
