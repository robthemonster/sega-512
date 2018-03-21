package com.themonster.segaclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
        final String username = getIntent().getStringExtra("username");
        if (username != null) {
            ((TextView) findViewById(R.id.usernameDashboard)).setText(username);
        }
        final GetGroupsForUserRequest request = new GetGroupsForUserRequest();
        request.setUsername(username);
        request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        task.execute();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GetGroupsForUserResponse.TYPE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GetGroupsForUserResponse response = (GetGroupsForUserResponse) intent.getSerializableExtra("response");
                groups.clear();
                groups.addAll(response.getGroups());
                ListView groupList = findViewById(R.id.groupListDashboard);
                if (groupList.getAdapter() instanceof ArrayAdapter) {
                    ArrayAdapter adapter = (ArrayAdapter) groupList.getAdapter();
                    adapter.notifyDataSetChanged();
                }
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String groupSelected = adapterView.getItemAtPosition(i).toString();
                Intent intent = new Intent(DashboardActivity.this, GroupActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("group", groupSelected);
                startActivity(intent);
            }
        });
        FloatingActionButton fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() { // This will send the program into an XML file that I will use for testing and
            // trying to figure out the database and new ROOM environment
            @Override
            public void onClick(View view) {
                Log.d("CreateGroup ", "onClick Pressed!");
                Intent intent = new Intent(DashboardActivity.this, CreateGroupActivity.class);
                intent.putExtra("username", getIntent().getStringExtra("username"));
                startActivity(intent);
            }
        });
        FloatingActionButton refresh = findViewById(R.id.refreshGroupsDashboardFab);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendRequestToServerTask task = new SendRequestToServerTask(request);
                task.execute();
            }
        });
    }
}
