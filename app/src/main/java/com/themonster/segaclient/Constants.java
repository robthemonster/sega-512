package com.themonster.segaclient;

import android.content.Context;

import java.io.InputStream;
/**
 * Created by The Monster on 2/21/2018.
 */

public class Constants {
    public static InputStream segaStore;
    public static final String SEGA_SERVER_DNS = /*"192.168.1.2";*/ "ec2-18-220-223-143.us-east-2.compute.amazonaws.com";
    public static final String USERNAME_EXTRA = "username";
    public static final String GROUPNAME_EXTRA = "groupname";

    public static String getFirebaseToken(Context context) {
        if (context.getSharedPreferences("firebaseToken", Context.MODE_PRIVATE) != null) {
            return context.getSharedPreferences("firebaseToken", Context.MODE_PRIVATE).getString("token", "");
        } else {
            return "";
        }
    }

    public static void setStore(Context context) {
        if (segaStore == null) {
            segaStore = context.getResources().openRawResource(R.raw.segastore);
        }
    }

    public static String getUsername(Context context) {
        if (context.getSharedPreferences("userCredentials", Context.MODE_PRIVATE) != null) {
            return context.getSharedPreferences("userCredentials", Context.MODE_PRIVATE).getString(USERNAME_EXTRA, "");
        } else {
            return "";
        }
    }
}
