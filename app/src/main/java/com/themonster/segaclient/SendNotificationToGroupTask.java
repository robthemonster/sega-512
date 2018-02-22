package com.themonster.segaclient;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import SEGAMessages.GroupNotification;

/**
 * Created by The Monster on 2/11/2018.
 */

public class SendNotificationToGroupTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... strings) {
        String message = strings[0];
        String topicName = strings[1];
        try {
            Socket socket = new Socket(ServerInfo.SEGA_SERVER_DNS, 6969);
            GroupNotification groupNotification = new GroupNotification();
            groupNotification.setMessage(message);
            groupNotification.setTopicName(topicName);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(groupNotification);
            outputStream.flush();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
