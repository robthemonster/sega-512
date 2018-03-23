package com.themonster.segaclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import SEGAMessages.GrantAuthorizationForGroupRequest;
import SEGAMessages.GrantAuthorizationForGroupResponse;

public class GrantAuthorizationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grant_authorization);
        TextView groupName = findViewById(R.id.groupNameGrantAuthActivity);
        groupName.setText(getIntent().getStringExtra("groupname"));
    }

    public void GrantAccess(View view) {
        GrantAuthorizationForGroupRequest request = new GrantAuthorizationForGroupRequest();
        request.setGroupName(getIntent().getStringExtra(Constants.GROUPNAME_EXTRA));
        request.setUsername(getIntent().getStringExtra(Constants.USERNAME_EXTRA));
        request.setFirebaseToken(getSharedPreferences("firebaseToken", MODE_PRIVATE).getString("token", ""));
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        task.execute();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GrantAuthorizationForGroupResponse.TYPE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GrantAuthorizationForGroupResponse response = (GrantAuthorizationForGroupResponse) intent.getSerializableExtra("response");
                if (response.isSucceded()) {
                    Toast.makeText(getApplicationContext(), "Acknowledged", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, intentFilter);
    }

    public void DenyAccess(View view) {
        finish();
    }
}
