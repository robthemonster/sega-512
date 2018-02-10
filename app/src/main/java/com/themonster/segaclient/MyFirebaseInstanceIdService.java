package com.themonster.segaclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by The Monster on 2/7/2018.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String newToken = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences sharedPreferences = getSharedPreferences("firebaseToken", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("token", newToken).apply();
        Log.d("Firebase Token", newToken);
    }
}
