package com.themonster.segaclient;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Random;

import SEGAMessages.UserLoginRequest;
import SEGAMessages.UserLoginResponse;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity";

    private static Toast loginFailedToast;
    FloatingActionButton fab;
    ProgressDialog dialog;
    Random random = new Random();
    int size;
    BroadcastReceiver broadcastReceiver;
    CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Constants.init(this);
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException | JSchException e) {
            e.printStackTrace();
            // nothing will work from here
        }
        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.experimentalorange));
        dialog = new ProgressDialog(LoginActivity.this);
        size = getResources().getStringArray(R.array.loading_memes).length;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick Pressed!");
                startActivity(new Intent(LoginActivity.this, CreateUserActivity.class));
            }
        });
        final IntentFilter intentFilter = new IntentFilter();
        loginFailedToast = Toast.makeText(getApplicationContext(), "login failed", Toast.LENGTH_SHORT);
        intentFilter.addAction(UserLoginResponse.TYPE);


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                UserLoginResponse response = (UserLoginResponse) intent.getSerializableExtra("response");
                if (response != null) {
                    if (response.isSucceeded()) { //TODO: account for if login failed
                        SharedPreferences sharedPreferences = getSharedPreferences("userCredentials", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString(Constants.USERNAME_EXTRA, response.getUsername()).apply();
                        launchDashBoard(response.getUsername());
                    } else {
                        Toast.makeText(getApplicationContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        resetFields();
                    }
                }
            }
        };


        TextInputEditText editText = findViewById(R.id.passwordLogin);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);
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
                    dialog.setIndeterminate(true);
                    dialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.phantom_spinner));
                    dialog.setTitle("Please Wait");
                    dialog.setMessage(getResources().getStringArray(R.array.loading_memes)[random.nextInt(size)] + "...");
                    dialog.setCancelable(false);
                    dialog.show();
                    countDownTimer = new CountDownTimer(20000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            // mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            Toast.makeText(getApplicationContext(), "Request Timed Out", Toast.LENGTH_SHORT).show();
                            resetFields();
                        }
                    }.start();
                    // findViewById(R.id.spinnyDoodleLogin).setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }
        });


    }

    private void resetFields() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
        ((EditText) findViewById(R.id.passwordLogin)).getText().clear();
        dialog.dismiss();
        findViewById(R.id.usernameLogin).setEnabled(true);
        findViewById(R.id.passwordLogin).setEnabled(true);
        //findViewById(R.id.spinnyDoodleLogin).setVisibility(View.INVISIBLE);
    }

    private void launchDashBoard(String username) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        findViewById(R.id.spinnyDoodleLogin).setVisibility(View.INVISIBLE);
        dialog.dismiss();
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra(Constants.USERNAME_EXTRA, username);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }
}
