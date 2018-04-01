package com.themonster.segaclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import SEGAMessages.AddUserToGroupRequest;
import SEGAMessages.AddUserToGroupResponse;
import SEGAMessages.GetGroupsForUserRequest;
import SEGAMessages.GetUsersForGroupRequest;
import SEGAMessages.GetUsersForGroupResponse;
import SEGAMessages.RequestAuthorizationFromGroupRequest;
import SEGAMessages.RequestAuthorizationFromGroupResponse;

public class GroupActivity2 extends AppCompatActivity {


    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private GroupMembersAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<String> usersInGroup = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_2);
        String groupName = getIntent().getStringExtra(Constants.GROUPNAME_EXTRA);
        String username = Constants.getUsername(getApplicationContext());

        setTitle(getIntent().getStringExtra(Constants.GROUPNAME_EXTRA) + "'s members");
        //nameTV.setText(getSharedPreferences("userCredentials", MODE_PRIVATE).getString(Constants.USERNAME_EXTRA, "") + "'s groups");
        mRecyclerView = findViewById(R.id.group_members_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new GroupMembersAdapter(usersInGroup);
        mRecyclerView.setAdapter(mAdapter);
       /* mAdapter.setOnItemClickListener(new GroupsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.d("Position : " + position + " clicked.", "ya");
                String groupSelected = usersInGroup.get(position);
                Intent intent = new Intent(GroupActivity2.this, GroupActivity.class);
                intent.putExtra(Constants.USERNAME_EXTRA, getSharedPreferences("userCredentials", MODE_PRIVATE).getString(Constants.USERNAME_EXTRA, ""));
                intent.putExtra(Constants.GROUPNAME_EXTRA, groupSelected);
                startActivity(intent);
            }
        });*/

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.group_members_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refresh();
            }
        });



        /*----------------------------------------------------------------------------*/

    /*
        GetUsersForGroupRequest request = new GetUsersForGroupRequest();
        request.setGroupname(groupName);
        request.setUsername(username);
        request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        task.execute();
        */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GetUsersForGroupResponse.TYPE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GetUsersForGroupResponse response = (GetUsersForGroupResponse) intent.getSerializableExtra("response");
                if (response.getUsers() == null){
                    Log.d("users", "users was null");
                }
                usersInGroup.clear();
                usersInGroup.addAll(response.getUsers());
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);

            }
        }, intentFilter);



        /*----------------------------------------------------------------------------*/
    }

    protected void onResume() {
        super.onResume();
        refresh();
    }

    void refresh()
    {
        String username = getSharedPreferences("userCredentials", MODE_PRIVATE).getString(Constants.USERNAME_EXTRA, "");
        String groupName = getIntent().getStringExtra(Constants.GROUPNAME_EXTRA);

        // ((TextView) findViewById(R.id.usernameDashboard)).setText(getSharedPreferences("userCredentials", MODE_PRIVATE).getString(Constants.USERNAME_EXTRA, ""));
        //Log.d("DashBoardActivity2", "onresumecalled "+ getSharedPreferences("userCredentials", MODE_PRIVATE).getString(Constants.USERNAME_EXTRA, ""));
        GetUsersForGroupRequest request = new GetUsersForGroupRequest();
        request.setGroupname(groupName);
        request.setUsername(username);
        request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        task.execute();
    }



    public void RequestAccess(View view) {
        final RequestAuthorizationFromGroupRequest request = new RequestAuthorizationFromGroupRequest();
        request.setGroupName(getIntent().getStringExtra(Constants.GROUPNAME_EXTRA));
        request.setUsername(getIntent().getStringExtra(Constants.USERNAME_EXTRA));
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
                    Intent launchBrowser = new Intent(getIntent());
                    Log.d("test", launchBrowser.getExtras().getString(Constants.GROUPNAME_EXTRA) == getIntent().getExtras().getString(Constants.GROUPNAME_EXTRA) ? "yes" : "no");

                    launchBrowser.setClass(getApplicationContext(), DirectoryBrowserActivity.class);
                    startActivity(launchBrowser);
                } else {
                    Toast.makeText(getApplicationContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, intentFilter);
        task.execute();
    }

    public void AddUserToGroup(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText userToAdd = new EditText(this);
        builder.setTitle("Username:");
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(AddUserToGroupResponse.TYPE);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        AddUserToGroupResponse response = (AddUserToGroupResponse) intent.getSerializableExtra("response");
                        Toast.makeText(getApplicationContext(), response.isSucceeded() ? "User added sucessfully" : response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        if (response.isSucceeded()) {
                            GroupActivity2.super.recreate(); // is this ok
                        }
                    }
                }, intentFilter);
                AddUserToGroupRequest request = new AddUserToGroupRequest();
                request.setUserToAdd(userToAdd.getText().toString());
                request.setRequestor(getIntent().getStringExtra(Constants.USERNAME_EXTRA));
                request.setGroupname(getIntent().getStringExtra(Constants.GROUPNAME_EXTRA));
                request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
                SendRequestToServerTask task = new SendRequestToServerTask(request);
                task.execute();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setView(userToAdd);
        builder.show();
    }
}
