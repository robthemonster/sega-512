package com.themonster.segaclient;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import SEGAMessages.Response;

/**
 * Created by The Monster on 2/7/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData() != null && remoteMessage.getData().get("serializedMessage") != null) {
            try {
                byte[] serializedMessage = Base64.decode(remoteMessage.getData().get("serializedMessage"), Base64.DEFAULT);
                ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(serializedMessage));
                Object message = objectInputStream.readObject();
                if (message instanceof Response) {
                    Response response = (Response) message;
                    broadcastResponseLocally(response);
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        if (remoteMessage.getNotification() != null) {
            Log.d("Remote message", "" + remoteMessage.getNotification().getBody());
            super.onMessageReceived(remoteMessage);
            if (remoteMessage.getData() != null && remoteMessage.getData().containsKey("groupname") && remoteMessage.getData().containsKey("username")) {
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= 26) {
                    Intent intent = new Intent(this, ApproveRequestBroadCastReceiver.class);
                    intent.putExtra(Constants.GROUPNAME_EXTRA, remoteMessage.getData().get("groupname"));
                    intent.putExtra(Constants.USERNAME_EXTRA, remoteMessage.getData().get("username"));
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    NotificationChannel channel = new NotificationChannel("SEGA", "SEGA", NotificationManager.IMPORTANCE_HIGH);
                    manager.createNotificationChannel(channel);
                    NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_check_green_24dp, "Approve", pendingIntent).build();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "SEGA")
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setSmallIcon(R.drawable.segalogo)
                            .addAction(action)
                            .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0))
                            .setContentTitle(remoteMessage.getNotification().getTitle())
                            .setContentText(remoteMessage.getNotification().getBody())
                            .setAutoCancel(true);
                    manager.notify(0, builder.build());
                    return;
                }
                Intent intent = new Intent(remoteMessage.getNotification().getClickAction());
                intent.putExtra(Constants.GROUPNAME_EXTRA, remoteMessage.getData().get("groupname"));
                intent.putExtra(Constants.USERNAME_EXTRA, remoteMessage.getData().get("username"));
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setSmallIcon(R.drawable.segalogo)
                        .setContentIntent(pendingIntent)
                        .setChannelId("SEGA")
                        .setAutoCancel(true);
                manager.notify(0, builder.build());
            }
        }
    }

    private void broadcastResponseLocally(Response response) {
        Intent intent = new Intent();
        intent.putExtra("response", response);
        intent.setAction(response.type());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

}
