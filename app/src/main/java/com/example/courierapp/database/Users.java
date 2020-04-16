package com.example.courierapp.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class Users {


    @NotNull
    public long idUser;

    @PrimaryKey
    @NotNull
    public String login;

    @NotNull
    public String email;

    @NotNull
    public String password;

    @NotNull
    public String authorizationKey;

    @NotNull
    public String lastConnection;

    @NotNull
    public String timestamp;

    @NotNull
    public boolean  authorized;
}
