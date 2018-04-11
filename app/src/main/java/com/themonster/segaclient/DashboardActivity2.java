package com.themonster.segaclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import SEGAMessages.GetGroupsForUserRequest;
import SEGAMessages.GetGroupsForUserResponse;

public class DashboardActivity2 extends AppCompatActivity {

    TextView nameTV;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Toast toast;
    private AlertDialog mDialog;
    private ArrayList<String> groups = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private GroupsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard2);
        //nameTV = (TextView)findViewById(R.id.cv_username);
        setTitle(getSharedPreferences("userCredentials", MODE_PRIVATE).getString(Constants.USERNAME_EXTRA, "") + "'s groups");
        //nameTV.setText(getSharedPreferences("userCredentials", MODE_PRIVATE).getString(Constants.USERNAME_EXTRA, "") + "'s groups");
        mRecyclerView = findViewById(R.id.AD_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new GroupsAdapter(groups);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new GroupsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.d("Position : " + position + " clicked.", "ya");
                String groupSelected = groups.get(position);
                Intent intent = new Intent(DashboardActivity2.this, GroupActivity2.class);
                intent.putExtra(Constants.USERNAME_EXTRA, getSharedPreferences("userCredentials", MODE_PRIVATE).getString(Constants.USERNAME_EXTRA, ""));
                intent.putExtra(Constants.GROUPNAME_EXTRA, groupSelected);
                startActivity(intent);
            }
        });

        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refresh();
            }
        });

        FloatingActionButton fab2 = findViewById(R.id.fab2_0);
        fab2.setOnClickListener(new View.OnClickListener() { // This will send the program into an XML file that I will use for testing and
            // trying to figure out the database and new ROOM environment
            @Override
            public void onClick(View view) {
                Log.d("CreateGroup ", "onClick Pressed!");
                Intent intent = new Intent(DashboardActivity2.this, CreateGroupActivity.class);
                intent.putExtra(Constants.USERNAME_EXTRA, getIntent().getStringExtra(Constants.USERNAME_EXTRA));
                startActivity(intent);
            }
        });
        /*
        final GetGroupsForUserRequest request = new GetGroupsForUserRequest();
        request.setUsername(getSharedPreferences("userCredentials", MODE_PRIVATE).getString(Constants.USERNAME_EXTRA, ""));
        request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        task.execute();
        */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GetGroupsForUserResponse.TYPE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GetGroupsForUserResponse response = (GetGroupsForUserResponse) intent.getSerializableExtra("response");
                groups.clear();
                groups.addAll(response.getGroups());
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, intentFilter);
    }

    protected void onResume() {
        super.onResume();
        // ((TextView) findViewById(R.id.usernameDashboard)).setText(getSharedPreferences("userCredentials", MODE_PRIVATE).getString(Constants.USERNAME_EXTRA, ""));
        Log.d("DashBoardActivity2", "onresumecalled " + getSharedPreferences("userCredentials", MODE_PRIVATE).getString(Constants.USERNAME_EXTRA, ""));
        refresh();
    }

    void refresh() {
        final GetGroupsForUserRequest request = new GetGroupsForUserRequest();
        request.setUsername(getSharedPreferences("userCredentials", MODE_PRIVATE).getString("username", ""));
        request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        task.execute();
    }

    public void onBackPressed() {

        if (mDialog == null) // https://stackoverflow.com/questions/14910602/how-to-use-alertdialog
        {
            mDialog = new AlertDialog.Builder(this)
                    .setTitle("Confirm Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("YES",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i("MyTag", "Click YES");
                                    //TODO rob here is where to clear out stuffs in the DB
                                    Intent i = new Intent(DashboardActivity2.this, LoginActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);

                                }
                            })

                    .setNegativeButton("NO",
                            new android.content.DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i("MyTag", "Click NO");

                                }
                            }).create();
        }
        // TODO Auto-generated method stub
        // moveTaskToBack(true);
        // super.onBackPressed();
        mDialog.show();
    }
}
