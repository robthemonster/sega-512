package com.themonster.segaclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.List;

import SEGAMessages.GetGroupsForUserRequest;

public class DashboardActivity extends AppCompatActivity {

    private List<String> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            ((TextView) findViewById(R.id.usernameDashboard)).setText(username);
        }
        GetGroupsForUserRequest request = new GetGroupsForUserRequest();
        request.setUsername(username);
        request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        task.execute();
    }
}
