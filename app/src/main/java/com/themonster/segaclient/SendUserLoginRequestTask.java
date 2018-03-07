package com.themonster.segaclient;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import SEGAMessages.UserLoginRequest;

/**
 * Created by The Monster on 3/7/2018.
 */

public class SendUserLoginRequestTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... strings) {
        String firebaseToken = strings[0];
        String username = strings[1];
        String password = strings[2];
        UserLoginRequest request = new UserLoginRequest();
        request.setFirebaseToken(firebaseToken);
        request.setUsername(username);
        request.setPassword(password);
        try {
            Socket socket = new Socket(ServerInfo.SEGA_SERVER_DNS, 6969);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(request);
            outputStream.flush();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
