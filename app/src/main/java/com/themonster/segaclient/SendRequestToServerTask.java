package com.themonster.segaclient;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.net.ssl.SSLSocket;

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
            SSLSocket socket = (SSLSocket) Constants.sslContext.getSocketFactory().createSocket(Constants.SEGA_SERVER_DNS, 6969);
            socket.setNeedClientAuth(false);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(request);
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
