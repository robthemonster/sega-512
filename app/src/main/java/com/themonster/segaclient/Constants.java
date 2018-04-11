package com.themonster.segaclient;

import android.content.Context;
import android.util.Log;

import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by The Monster on 2/21/2018.
 */

public class Constants {
    public static final String SEGA_SERVER_DNS = /*"192.168.1.2";*/ "ec2-18-220-223-143.us-east-2.compute.amazonaws.com";
    public static final String USERNAME_EXTRA = "username";
    public static final String GROUPNAME_EXTRA = "groupname";
    public static SSLContext sslContext;
    public static JSch jSch;

    public static String getFirebaseToken(Context context) {
        if (context.getSharedPreferences("firebaseToken", Context.MODE_PRIVATE) != null) {
            return context.getSharedPreferences("firebaseToken", Context.MODE_PRIVATE).getString("token", "");
        } else {
            return "";
        }
    }

    public static String getUsername(Context context) {
        if (context.getSharedPreferences("userCredentials", Context.MODE_PRIVATE) != null) {
            return context.getSharedPreferences("userCredentials", Context.MODE_PRIVATE).getString(USERNAME_EXTRA, "");
        } else {
            return "";
        }
    }

    public static void init(Context context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, JSchException {
        KeyStore trustStore = KeyStore.getInstance("BKS");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustStore.load(context.getResources().openRawResource(R.raw.segastore), "gottagofast".toCharArray());
        trustManagerFactory.init(trustStore);
        sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        jSch = new JSch();
        jSch.setKnownHosts(context.getResources().openRawResource(R.raw.known_hosts));
        HostKeyRepository hostKeyRepository = jSch.getHostKeyRepository();
        Log.d("size", "" + hostKeyRepository.getHostKey().length);
    }
}
