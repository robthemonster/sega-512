package com.themonster.segaclient;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by The Monster on 2/7/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("Remote message", "" + remoteMessage.getNotification().getBody());
        super.onMessageReceived(remoteMessage);
    }
}
