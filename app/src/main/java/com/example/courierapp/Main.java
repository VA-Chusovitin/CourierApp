package com.example.courierapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.courierapp.database.AppDatabase;
import com.example.courierapp.database.DatabaseRequests;
import com.example.courierapp.database.Users;

import java.util.List;

public class Main extends AppCompatActivity implements View.OnClickListener {

    MyApplication application;
    public static AppDatabase database;
    DatabaseRequests databaseRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        application = (MyApplication) this.getApplication();
        database = application.getDatabase();
        databaseRequests = database.getDatabaseRequests();

        Button buttonSignIn = findViewById(R.id.button);
        buttonSignIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        new Thread(() -> {
            List<Users> user = databaseRequests.getAuthorizedUser();
            if (user.size() > 0) {
                user.get(0).authorized = false;
                databaseRequests.insertUser(user.get(0));
            }
        }).start();

        finish();
    }
}
