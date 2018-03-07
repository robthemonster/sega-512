package com.themonster.segaclient;

import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import SEGAMessages.CreateUserResponse;
import SEGAMessages.GetGroupsForUserResponse;
import SEGAMessages.UserLoginResponse;

/**
 * Created by The Monster on 2/7/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            byte[] serializedMessage = Base64.decode(remoteMessage.getData().get("serializedMessage"), Base64.DEFAULT);
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(serializedMessage));
            Object message = objectInputStream.readObject();
            if (message instanceof CreateUserResponse) {
                CreateUserResponse response = (CreateUserResponse) message;
                Intent intent = new Intent();
                intent.putExtra("response", response);
                intent.setAction("CreateUserResponse");
                Log.d("REPONSE", " U GOT REPONSE");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                return;
            }
            if (message instanceof UserLoginResponse) {
                UserLoginResponse response = (UserLoginResponse) message;
                Intent intent = new Intent();
                intent.putExtra("response", response);
                intent.setAction("UserLoginResponse");
                Log.d("REPONSE", "U GOT USER LOGGING REPOSE");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                return;
            }
            if (message instanceof GetGroupsForUserResponse) {
                GetGroupsForUserResponse response = (GetGroupsForUserResponse) message;
                Intent intent = new Intent();
                intent.putExtra("response", response);
                intent.setAction("GetGroupsForUserResponse");
                Log.d("RPOOEENSE", "GET GROUPS REPONSE");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                return;
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        if (remoteMessage.getNotification() != null) {
            Log.d("Remote message", "" + remoteMessage.getNotification().getBody());
            super.onMessageReceived(remoteMessage);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody());
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }
    }
}
