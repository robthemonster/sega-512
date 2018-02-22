package com.themonster.segaclient;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import SEGAMessages.ClientInfo;

/**
 * Created by The Monster on 2/7/2018.
 */

public class SendMessageTask extends AsyncTask<String, Boolean, Void> {
    @Override
    protected Void doInBackground(String... strings) {
        String message = strings[0];
        String firebaseToken = strings[1];
        try {
            Socket socket = new Socket(ServerInfo.SEGA_SERVER_DNS, 6969);
            ClientInfo clientInfo = new ClientInfo();
            clientInfo.setFirebaseToken(firebaseToken);
            clientInfo.setMessage(message);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(clientInfo);
            outputStream.flush();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
