package com.themonster.segaclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import SEGAMessages.CreateUserResponse;

/**
 * Created by CJ Hernaez on 2/12/2018.
 */

public class CreateUserActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createuser);
        TextInputEditText et = findViewById(R.id.passwordCreateUser);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    EditText usernameEditText = findViewById(R.id.usernameCreateUser);
                    EditText passwordEditText = findViewById(R.id.passwordCreateUser);
                    usernameEditText.setEnabled(false);
                    passwordEditText.setEnabled(false);
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    }
                    String username = usernameEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    String firebaseToken = getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", "");
                    SendCreateUserRequestTask task = new SendCreateUserRequestTask();
                    task.execute(firebaseToken, username, password);
                    findViewById(R.id.spinnyDoodleCreateUser).setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("CreateUserResponse");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                CreateUserResponse response = (CreateUserResponse) intent.getSerializableExtra("response");
                if (response != null) {
                    if (response.isSucceeded()) {
                        returnToLogin();
                    } else {
                        //TODO: Account for if create user was unsuccessful
                    }
                }
            }
        }, intentFilter);
    }

    public void returnToLogin() {
        findViewById(R.id.spinnyDoodleCreateUser).setVisibility(View.INVISIBLE);
        onBackPressed();
    }
}
