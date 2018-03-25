package com.themonster.segaclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import SEGAMessages.GetGroupsForUserRequest;
import SEGAMessages.GetGroupsForUserResponse;

public class DashboardActivity2 extends AppCompatActivity {

    private ArrayList<String> groups = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard2);

        mRecyclerView = (RecyclerView) findViewById(R.id.AD_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new GroupsAdapter(groups);
        mRecyclerView.setAdapter(mAdapter);



        final GetGroupsForUserRequest request = new GetGroupsForUserRequest();
        request.setUsername(getSharedPreferences("userCredentials", MODE_PRIVATE).getString(Constants.USERNAME_EXTRA, ""));
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
                    mAdapter.notifyDataSetChanged();

            }
        }, intentFilter);






    }
}
