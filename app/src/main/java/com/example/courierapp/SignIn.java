package com.example.courierapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.courierapp.database.AppDatabase;
import com.example.courierapp.database.DatabaseRequests;
import com.example.courierapp.database.Users;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class SignIn extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private MyApplication application;
    public static AppDatabase database;
    private DatabaseRequests databaseRequests;

    private TextInputLayout loginLayout;
    private TextInputLayout passLayout;
    private TextInputEditText login;
    private TextInputEditText password;
    private Button buttonSignIn;
    private CheckBox rememberMe;
    private ProgressBar progressBar;

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!login.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
                buttonSignIn.setEnabled(true);
            } else {
                buttonSignIn.setEnabled(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        application = (MyApplication) this.getApplication();
        database = application.getDatabase();
        databaseRequests = database.getDatabaseRequests();

        progressBar = findViewById(R.id.progressBar);
        checkAuthorization();

        loginLayout = findViewById(R.id.loginLayout);
        passLayout = findViewById(R.id.passLayout);
        login = findViewById(R.id.login);
        password = findViewById(R.id.pass);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        rememberMe = findViewById(R.id.rememberMe);

        login.setOnFocusChangeListener(this);
        login.addTextChangedListener(textWatcher);
        password.setOnFocusChangeListener(this);
        password.addTextChangedListener(textWatcher);
        password.setOnKeyListener((v, keyCode, event) -> {
            boolean consumed = false;
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                password.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(password.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                consumed = true;
            }
            return consumed;
        });
        buttonSignIn.setOnClickListener(this);
    }

    /*@Override
    public void onBackPressed() {
        finish();
    }*/

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSignIn:
                signInAccount(login.getText().toString(), password.getText().toString());
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == login) {
            if (!hasFocus && login.getText().toString().isEmpty()) {
                loginLayout.setError(getString(R.string.SignIn_loginEmpty));
            } else {
                loginLayout.setError(null);
            }
        }

        if (v == password) {
            if (!hasFocus && password.getText().toString().isEmpty()) {
                passLayout.setError(getString(R.string.SignIn_passwordEmpty));
            } else {
                passLayout.setError(null);
            }
        }
    }

    private void checkAuthorization() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        findViewById(R.id.mainSignIn).setVisibility(View.INVISIBLE);
        new Thread(() -> {
            List<Users> user = databaseRequests.getAuthorizedUser();
            if (user.size() > 0 && hasConnection()) {

                JSONObject jsonData = new JSONObject();
                JSONObject jsonRequest = new JSONObject();
                try {
                    jsonData.put(getString(R.string.authorizationKey), user.get(0).authorizationKey);
                    jsonRequest.put(getString(R.string.authorization), jsonData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestQueue requestQueue = Volley.newRequestQueue(this);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        application.getUrlApi(),
                        jsonRequest,
                        response -> new Thread(() -> {
                            if (response.has(getString(R.string.success))) {
                                try {
                                    JSONObject success = response.getJSONObject(getString(R.string.success));

                                    user.get(0).lastConnection = success.get(getString(R.string.lastConnection)).toString();
                                    user.get(0).timestamp = success.get(getString(R.string.timestamp)).toString();
                                    databaseRequests.insertUser(user.get(0));

                                    goMainActivity();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                user.get(0).authorized = false;
                                databaseRequests.insertUser(user.get(0));
                            }
                        }).start(),
                        error -> Log.e("API ERROR", error.toString())
                );

                requestQueue.add(jsonObjectRequest);
            } else if (user.size() > 0) {
                goMainActivity();
            }
            runOnUiThread(() -> {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                findViewById(R.id.mainSignIn).setVisibility(View.VISIBLE);
            });
        }).start();
    }

    private void signInAccount(String login, String password) {
        if (hasConnection()) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            this.login.setEnabled(false);
            this.password.setEnabled(false);
            rememberMe.setEnabled(false);
            buttonSignIn.setEnabled(false);

            JSONObject jsonData = new JSONObject();
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonData.put(getString(R.string.login), login);
                jsonData.put(getString(R.string.password), password);
                jsonRequest.put(getString(R.string.authorization), jsonData);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestQueue requestQueue = Volley.newRequestQueue(this);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    application.getUrlApi(),
                    jsonRequest,
                    response -> new Thread(() -> {
                        try {
                            if (response.has(getString(R.string.success))) {
                                JSONObject success = response.getJSONObject(getString(R.string.success));

                                Users user = new Users();
                                user.idUser = Long.parseLong(success.get(getString(R.string.idUser)).toString());
                                user.login = success.get(getString(R.string.login)).toString();
                                user.email = success.get(getString(R.string.email)).toString();
                                user.password = success.get(getString(R.string.password)).toString();
                                user.authorizationKey = success.get(getString(R.string.authorizationKey)).toString();
                                user.lastConnection = success.get(getString(R.string.lastConnection)).toString();
                                user.timestamp = success.get(getString(R.string.timestamp)).toString();
                                user.authorized = rememberMe.isChecked();

                                databaseRequests.insertUser(user);

                                goMainActivity();
                            } else if (response.has(getString(R.string.wrong))) {
                                JSONObject wrong = response.getJSONObject(getString(R.string.wrong));

                                if (wrong.has(getString(R.string.login))) {
                                    runOnUiThread(() -> {
                                        try {
                                            loginLayout.setError(wrong.get(getString(R.string.login)).toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                } else if (wrong.has(getString(R.string.password))) {
                                    runOnUiThread(() -> {
                                        try {
                                            passLayout.setError(wrong.get(getString(R.string.password)).toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                } else {
                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                            getString(R.string.SignIn_error), Toast.LENGTH_LONG).show());
                                    Log.e("API ERROR", getString(R.string.unknownError));
                                }
                            }

                            runOnUiThread(() -> {
                                this.login.setEnabled(true);
                                this.password.setEnabled(true);
                                this.rememberMe.setEnabled(true);
                                this.buttonSignIn.setEnabled(true);
                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }).start(),
                    error -> Log.e("API ERROR", error.toString())
            );

            requestQueue.add(jsonObjectRequest);
        } else {
            signInOffline();
        }
    }

    public boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void signInOffline() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignIn.this);

        builder.setTitle(getString(R.string.SignIn_dialogTitle))
                .setMessage(getString(R.string.SignIn_dialogMessage))
                .setPositiveButton(getString(R.string.SignIn_dialogPositive), (dialog, which) -> new Thread(() -> {
                    Users user = databaseRequests.findUser(login.getText().toString());
                    if (user != null && user.password.equals(password.getText().toString())) {
                        goMainActivity();
                    } else {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                    getString(R.string.SignIn_userNotFound), Toast.LENGTH_LONG).show());
                    }
                }).start())
                .setNegativeButton(getString(R.string.SignIn_dialogNegative), (dialog, which) -> {
                    dialog.cancel();
                    password.setText(getString(R.string.emptyString));
                })
                .setCancelable(false)
                .show();
    }

    private void goMainActivity() {
        runOnUiThread(() -> {
            login.setText(R.string.emptyString);
            password.setText(R.string.emptyString);
            rememberMe.setChecked(false);
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        });
        Intent intent;
        intent = new Intent(this, Main.class);
        startActivity(intent);
    }
}