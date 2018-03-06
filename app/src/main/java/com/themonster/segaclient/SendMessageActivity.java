package com.themonster.segaclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.messaging.FirebaseMessaging;

public class SendMessageActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity";

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() { // This will send the program into an XML file that I will use for testing and
            // trying to figure out the database and new ROOM environment
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick Pressed!");
                startActivity(new Intent(SendMessageActivity.this, CreateUserActivity.class));
            }
        });
    }

    public void createGroup(View view) {
        String groupName = ((EditText) findViewById(R.id.groupName_edittext)).getText().toString();
        FirebaseMessaging.getInstance().subscribeToTopic(groupName);
    }

    public void sendNotificationToGroup(View view) {
        String groupName = ((EditText) findViewById(R.id.groupName_edittext)).getText().toString();
        String message = ((EditText) findViewById(R.id.password_edittext)).getText().toString();
        SendNotificationToGroupTask task = new SendNotificationToGroupTask();
        task.execute(message, groupName);
    }
}
