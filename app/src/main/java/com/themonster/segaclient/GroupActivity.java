package com.themonster.segaclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import SEGAMessages.GetUsersForGroupRequest;
import SEGAMessages.GetUsersForGroupResponse;
import SEGAMessages.RequestAuthorizationFromGroupRequest;
import SEGAMessages.RequestAuthorizationFromGroupResponse;

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

    public void RequestAccess(View view) {
        final RequestAuthorizationFromGroupRequest request = new RequestAuthorizationFromGroupRequest();
        request.setGroupName(getIntent().getStringExtra("group"));
        request.setUsername(getIntent().getStringExtra("username"));
        request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RequestAuthorizationFromGroupResponse.TYPE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                RequestAuthorizationFromGroupResponse response = (RequestAuthorizationFromGroupResponse) intent.getSerializableExtra("response");
                if (response.isSucceeded()) {
                    Toast.makeText(getApplicationContext(), request.getGroupName() + " authorized your request!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, intentFilter);
        task.execute();
    }
}
