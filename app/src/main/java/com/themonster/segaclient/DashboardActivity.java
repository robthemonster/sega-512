package com.themonster.segaclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import SEGAMessages.GetGroupsForUserRequest;
import SEGAMessages.GetGroupsForUserResponse;

public class DashboardActivity extends AppCompatActivity {

    private ArrayList<String> groups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            ((TextView) findViewById(R.id.usernameDashboard)).setText(username);
        }
        final GetGroupsForUserRequest request = new GetGroupsForUserRequest();
        request.setUsername(username);
        request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        task.execute();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("GetGroupsForUserResponse");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GetGroupsForUserResponse response = (GetGroupsForUserResponse) intent.getSerializableExtra("response");
                groups.clear();
                groups.addAll(response.getGroups());
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) ((ListView) findViewById(R.id.groupListDashboard)).getAdapter();
                adapter.notifyDataSetChanged();
            }
        }, intentFilter);

        ListView listView = findViewById(R.id.groupListDashboard);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.group_list_item, groups) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                String item = getItem(position);
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_list_item, parent, false);
                }
                TextView groupNameTextView = convertView.findViewById(R.id.groupNameListItem);
                groupNameTextView.setText(item);
                return convertView;
            }
        });
    }
}
