package com.themonster.segaclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

public class SendMessageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        new Thread(new ListenForMessages()).start();
    }

    public void createGroup(View view) {
        String groupName = ((EditText) findViewById(R.id.groupName_edittext)).getText().toString();
        if (!groupName.toLowerCase().equals("Best of Friends".toLowerCase())) //TODO: Check database for existing names
        {
            FirebaseMessaging.getInstance().subscribeToTopic(groupName);
        } else {
            Toast.makeText(this, "THAT TAKEN", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendNotificationToGroup(View view) {
        String groupName = ((EditText) findViewById(R.id.groupName_edittext)).getText().toString();
        String message = ((EditText) findViewById(R.id.password_edittext)).getText().toString();
        SendNotificationToGroupTask task = new SendNotificationToGroupTask();
        task.execute(message, groupName);
    }
}
