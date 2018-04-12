package com.themonster.segaclient;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import SEGAMessages.CreateUserRequest;
import SEGAMessages.CreateUserResponse;

/**
 * Created by CJ Hernaez on 2/12/2018.
 */

public class CreateUserActivity extends AppCompatActivity {

    private static Toast createUserFailedToast;
    boolean passwordsmatch = true;
    ProgressDialog dialog;
    Random random;
    int size;
    BroadcastReceiver broadcastReceiver;
    CountDownTimer countDownTimer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        dialog = new ProgressDialog(CreateUserActivity.this);
        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.experimentalblue));
        createUserFailedToast = Toast.makeText(getApplicationContext(), "Username taken!", Toast.LENGTH_SHORT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createuser);
        random = new Random();
        size = getResources().getStringArray(R.array.loading_memes).length;
        Button fab = findViewById(R.id.create_user_phantom_button);
        fab.setOnClickListener(new View.OnClickListener() { // This will send the program into an XML file that I will use for testing and
            // trying to figure out the database and new ROOM environment
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreateUserActivity.this, SplashScreenActivity.class));
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                CreateUserResponse response = (CreateUserResponse) intent.getSerializableExtra("response");
                if (response != null) {
                    if (response.isSucceeded()) {
                        returnToLogin();
                    } else {
                        //TODO: Account for if create user was unsuccessful
                        createUserFailedToast.show();
                        resetFields();
                    }
                }
            }
        };


        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CreateUserResponse.TYPE);


        TextInputEditText et = findViewById(R.id.passwordConfirmCreateUser);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                EditText usernameEditText = findViewById(R.id.usernameCreateUser);
                EditText passwordEditText = findViewById(R.id.passwordCreateUser);
                EditText passwordConfirmEditText = findViewById(R.id.passwordConfirmCreateUser);
                /*if (!passwordsmatch && id == EditorInfo.IME_ACTION_DONE)
                {
                    Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
                else*/
                if (/*passwordsmatch && */id == EditorInfo.IME_ACTION_DONE) {
                    LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);
                    passwordConfirmEditText.setEnabled(false);
                    usernameEditText.setEnabled(false);
                    passwordEditText.setEnabled(false);

                    if (validateUserName() < 0) //
                    {
                        passwordConfirmEditText.setEnabled(true);
                        usernameEditText.setEnabled(true);
                        passwordEditText.setEnabled(true);
                    } else if (passwordConfirmEditText.getText().toString().equals(passwordEditText.getText().toString())) {

                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null) {
                            inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                        }
                        CreateUserRequest request = new CreateUserRequest();
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
                        //findViewById(R.id.spinnyDoodleCreateUser).setVisibility(View.VISIBLE);
                        countDownTimer = new CountDownTimer(20000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                // mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                            }

                            public void onFinish() {
                                Toast.makeText(getApplicationContext(), "Request Timed Out. Server Down?", Toast.LENGTH_SHORT).show();
                                resetFields();
                            }
                        }.start();
                        return true;
                    } else // the passwords do not match
                    {
                        countDownTimer.cancel();
                        passwordConfirmEditText.setHint("Passwords do not match");
                        Toast.makeText(CreateUserActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                        passwordConfirmEditText.setEnabled(true);
                        usernameEditText.setEnabled(true);
                        passwordEditText.setEnabled(true);

                    }
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
        ((EditText) findViewById(R.id.usernameCreateUser)).getText().clear();
        ((EditText) findViewById(R.id.passwordConfirmCreateUser)).getText().clear();
        findViewById(R.id.passwordCreateUser).setEnabled(true);
        findViewById(R.id.usernameCreateUser).setEnabled(true);
        findViewById(R.id.passwordConfirmCreateUser).setEnabled(true);
        findViewById(R.id.spinnyDoodleCreateUser).setVisibility(View.INVISIBLE);
        dialog.dismiss();
    }

    public void returnToLogin() {
        countDownTimer.cancel();
        findViewById(R.id.spinnyDoodleCreateUser).setVisibility(View.INVISIBLE);
        dialog.dismiss();
        finish();
    }

    public boolean validatePassword() {
        return findViewById(R.id.passwordConfirmCreateUser).toString().length() > 5;
    }

    public int validateUserName() {

        String et = ((EditText) findViewById(R.id.usernameCreateUser)).getText().toString();

        Log.d("ValidateUser", "String Length =" + et.length());

        if (et.length() < 3) {
            Toast.makeText(CreateUserActivity.this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return getResources().getInteger(R.integer.short_username_error);
            //Completed(TOD0: dont forget about this cj)

        }
        if (et.contains("  ")) {
            Toast.makeText(CreateUserActivity.this, "Username cannot contain consecutive spaces", Toast.LENGTH_SHORT).show();
            return getResources().getInteger(R.integer.double_space_error);
        }
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(et);
        if (m.find())//https://stackoverflow.com/questions/1795402/java-check-a-string-if-there-is-a-special-character-in-it
        {
            Toast.makeText(CreateUserActivity.this, "Username cannot contain special characters", Toast.LENGTH_SHORT).show();
            return getResources().getInteger(R.integer.special_character_error);
        }
        return 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }

}
