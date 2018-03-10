package com.themonster.segaclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import SEGAMessages.GetUsersForGroupRequest;
import SEGAMessages.GetUsersForGroupResponse;

public class GroupActivity extends AppCompatActivity {

    ArrayList<String> usersInGroup = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        final ListView listView = findViewById(R.id.userListGroupActivity);
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.group_users_list_item, usersInGroup));
        String groupName = getIntent().getStringExtra("group");
        String username = getIntent().getStringExtra("username");
        ((TextView) findViewById(R.id.groupNameGroupActivity)).setText(groupName);
        GetUsersForGroupRequest request = new GetUsersForGroupRequest();
        request.setGroupname(groupName);
        request.setUsername(username);
        request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        task.execute();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GetUsersForGroupResponse.TYPE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GetUsersForGroupResponse response = (GetUsersForGroupResponse) intent.getSerializableExtra("response");
                usersInGroup.clear();
                usersInGroup.addAll(response.getUsers());
                if (listView.getAdapter() instanceof ArrayAdapter) {
                    ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
                }

            }
        }, intentFilter);
    }
}
