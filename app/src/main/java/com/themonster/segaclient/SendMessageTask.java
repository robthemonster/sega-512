package com.themonster.segaclient;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by The Monster on 2/7/2018.
 */

public class SendMessageTask extends AsyncTask<String, Boolean, Void> {
    @Override
    protected Void doInBackground(String... strings) {
        String message = strings[0];
        try {
            Socket socket = new Socket(ListenForMessages.SERVER_ADDRESS, 6969);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.print(message);
            printWriter.flush();
            printWriter.close();
            socket.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
