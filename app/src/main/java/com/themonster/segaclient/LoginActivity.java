package com.themonster.segaclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import SEGAMessages.UserLoginRequest;
import SEGAMessages.UserLoginResponse;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity";

    private static Toast loginFailedToast;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() { // This will send the program into an XML file that I will use for testing and
            // trying to figure out the database and new ROOM environment
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick Pressed!");
                startActivity(new Intent(LoginActivity.this, CreateUserActivity.class));
            }
        });


        TextInputEditText editText = findViewById(R.id.passwordLogin);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    EditText usernameEditText = findViewById(R.id.usernameLogin);
                    EditText passwordEditText = findViewById(R.id.passwordLogin);
                    usernameEditText.setEnabled(false);
                    passwordEditText.setEnabled(false);
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    }
                    UserLoginRequest request = new UserLoginRequest();
                    request.setUsername(usernameEditText.getText().toString());
                    request.setPassword(passwordEditText.getText().toString());
                    request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
                    SendRequestToServerTask task = new SendRequestToServerTask(request);
                    task.execute();
                    findViewById(R.id.spinnyDoodleLogin).setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }
        });
        loginFailedToast = Toast.makeText(getApplicationContext(), "login failed", Toast.LENGTH_SHORT);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UserLoginResponse.TYPE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                UserLoginResponse response = (UserLoginResponse) intent.getSerializableExtra("response");
                if (response != null) {
                    if (response.isSucceeded()) { //TODO: account for if login failed
                        launchDashBoard(response.getUsername());
                    } else {
                        loginFailedToast.show();
                        resetFields();
                    }
                }
            }
        }, intentFilter);
    }

    private void resetFields() {
        ((EditText) findViewById(R.id.passwordLogin)).getText().clear();
        findViewById(R.id.usernameLogin).setEnabled(true);
        findViewById(R.id.passwordLogin).setEnabled(true);
        findViewById(R.id.spinnyDoodleLogin).setVisibility(View.INVISIBLE);
    }

    private void launchDashBoard(String username) {
        findViewById(R.id.spinnyDoodleLogin).setVisibility(View.INVISIBLE);
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

}
