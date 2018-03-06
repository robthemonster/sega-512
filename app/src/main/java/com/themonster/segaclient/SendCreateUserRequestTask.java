package com.themonster.segaclient;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import SEGAMessages.CreateUserRequest;

/**
 * Created by The Monster on 3/5/2018.
 */

public class SendCreateUserRequestTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... strings) {
        String firebaseToken = strings[0];
        String username = strings[1];
        String password = strings[2];
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(username);
        createUserRequest.setPassword(password);
        createUserRequest.setFirebaseToken(firebaseToken);
        try {
            Socket socket = new Socket(ServerInfo.SEGA_SERVER_DNS, 6969);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(createUserRequest);
            outputStream.flush();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
