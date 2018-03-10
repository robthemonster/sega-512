package com.themonster.segaclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import SEGAMessages.CreateGroupRequest;
import SEGAMessages.CreateGroupResponse;
import SEGAMessages.CreateUserRequest;
import SEGAMessages.CreateUserResponse;

/**
 * Created by CJ Hernaez on 3/9/2018.
 */

public class CreateGroupActivity extends AppCompatActivity{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final String username = getIntent().getStringExtra("username");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creategroup);



        final TextInputEditText et = findViewById(R.id.create_group_groupname);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                EditText groupName = findViewById(R.id.create_group_groupname);
                if (id == EditorInfo.IME_ACTION_DONE) {
                    groupName.setEnabled(false);
                    if (validateGroup() < 0) //
                    {
                        groupName.setEnabled(true);

                    } else {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null) {
                            inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                        }
                        CreateGroupRequest request = new CreateGroupRequest();
                        request.setCreator(username);
                        request.setGroupName(groupName.getText().toString());
                        request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
                        SendRequestToServerTask task = new SendRequestToServerTask(request);
                        task.execute();
                        findViewById(R.id.spinnyDoodleCreateUser).setVisibility(View.VISIBLE);
                        return true;
                    }
                }
                return false;
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("CreateGroupResponse");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                CreateGroupResponse response = (CreateGroupResponse) intent.getSerializableExtra("response");
                if (response != null) {
                    if (response.isSucceeded()) {
                        Toast.makeText(CreateGroupActivity.this, "Group"+ et.getText().toString()+ "created.", Toast.LENGTH_SHORT).show();
                        findViewById(R.id.create_group_groupname).setEnabled(true);
                    } else {
                        //TODO: Account for if create user was unsuccessful
                       // createUserFailedToast.show();
                        Toast.makeText(CreateGroupActivity.this, "Group already Exists.", Toast.LENGTH_SHORT).show();
                        findViewById(R.id.create_group_groupname).setEnabled(true);
                        //resetFields();
                    }
                    findViewById(R.id.spinnyDoodleCreateUser).setVisibility(View.INVISIBLE);
                }
            }
        }, intentFilter);
    }

    public boolean validatePassword() {
        return findViewById(R.id.passwordConfirmCreateUser).toString().length() > 5;
    }
    public int validateGroup()
    {

        String et = ((EditText)findViewById(R.id.usernameCreateUser)).getText().toString();

        Log.d("ValidateGroup", "String Length =" + et.length());

        if (et.length() < 3)
        {
            Toast.makeText(CreateGroupActivity.this, "Group Name must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return R.integer.short_username_error;
        }
        if (et.contains("  "))
        {
            Toast.makeText(CreateGroupActivity.this, "Group Name cannot contain consecutive spaces", Toast.LENGTH_SHORT).show();
            return R.integer.double_space_error;
        }
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(et);
        if (m.find())//https://stackoverflow.com/questions/1795402/java-check-a-string-if-there-is-a-special-character-in-it
        {
            Toast.makeText(CreateGroupActivity.this, "Group Name cannot contain special characters", Toast.LENGTH_SHORT).show();
            return R.integer.special_character_error;
        }
        return 0;
    }
}
