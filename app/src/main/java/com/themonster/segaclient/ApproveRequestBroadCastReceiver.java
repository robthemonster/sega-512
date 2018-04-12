package com.themonster.segaclient;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import SEGAMessages.GrantAuthorizationForGroupRequest;
import SEGAMessages.GrantAuthorizationForGroupResponse;

public class ApproveRequestBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Constants.init(context);
        } catch (IOException | CertificateException | JSchException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
        final GrantAuthorizationForGroupRequest request = new GrantAuthorizationForGroupRequest();
        request.setGroupName(intent.getStringExtra(Constants.GROUPNAME_EXTRA));
        request.setUsername(intent.getStringExtra(Constants.USERNAME_EXTRA));
        request.setFirebaseToken(Constants.getFirebaseToken(context));
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GrantAuthorizationForGroupResponse.TYPE);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GrantAuthorizationForGroupResponse response = (GrantAuthorizationForGroupResponse) intent.getSerializableExtra("response");
                Toast.makeText(context, response.isSucceded() ? "Acknowledged" : response.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        }, intentFilter);
        task.execute();
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
    }
}
