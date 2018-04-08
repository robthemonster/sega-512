package com.themonster.segaclient;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by The Monster on 3/7/2018.
 */

public class SendRequestToServerTask extends AsyncTask<Void, Void, Void> {

    private Serializable request;

    public SendRequestToServerTask(Serializable request) {
        this.request = request;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            InputStream segaStore = Constants.segaStore;
            Constants.segaStore.reset();
            trustStore.load(segaStore, "gottagofast".toCharArray());
            trustManagerFactory.init(trustStore);
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            SSLSocket socket = (SSLSocket) sslContext.getSocketFactory().createSocket(Constants.SEGA_SERVER_DNS, 6969);
            socket.setNeedClientAuth(false);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(request);
            outputStream.close();
            socket.close();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | KeyManagementException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
