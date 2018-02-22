package com.themonster.segaclient;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by The Monster on 2/7/2018.
 */

public class ListenForMessages extends Thread {
    public static final String SERVER_ADDRESS = "ec2-18-219-109-42.us-east-2.compute.amazonaws.com"; //Google Cloud IP Address. Test Commit3?

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(6969);
            while (true) {
                Socket socket = serverSocket.accept();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.d("ListenForMessages", bufferedReader.readLine());
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
