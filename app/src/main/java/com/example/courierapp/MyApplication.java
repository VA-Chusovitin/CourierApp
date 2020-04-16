package com.example.courierapp;

import android.app.Application;

import androidx.room.Room;

import com.example.courierapp.database.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class MyApplication extends Application{
    private static AppDatabase database;
    private String urlApi = "https://chusovitindiplom.ru/api/";

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database").build();
    }

    public String getUrlApi() {
        return urlApi;
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public String getMoscowDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.dateFormat));
        dateFormat.setTimeZone(TimeZone.getTimeZone(getString(R.string.timeZoneMoscow)));
        return dateFormat.format(Calendar.getInstance().getTime());
    }
}
