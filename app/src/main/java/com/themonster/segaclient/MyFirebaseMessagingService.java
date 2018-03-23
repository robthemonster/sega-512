package com.themonster.segaclient;

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
                Intent intent = new Intent(remoteMessage.getNotification().getClickAction());
                intent.putExtra("groupname", remoteMessage.getData().get("groupname"));
                intent.putExtra("username", remoteMessage.getData().get("username"));
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= 26) {
                    NotificationChannel channel = new NotificationChannel("SEGA", "SEGA", NotificationManager.IMPORTANCE_DEFAULT);
                    manager.createNotificationChannel(channel);
                }
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
