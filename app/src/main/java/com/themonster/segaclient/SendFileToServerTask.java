package com.themonster.segaclient;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by The Monster on 3/25/2018.
 */

public class SendFileToServerTask extends AsyncTask<String, Void, Void> {
    private SendFileToServerCallBack callBack;

    public SendFileToServerTask(SendFileToServerCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected Void doInBackground(String... strings) {
        String groupname = strings[0];
        File file = new File(strings[1]);
        if (!file.exists()) {
            Log.d("f", "upload file does not exist");
            return null;
        }
        try {
            FTPClient client = new FTPClient();
            client.connect(Constants.SEGA_SERVER_DNS, 6921);
            Log.d("ports", "local: " + client.getLocalPort() + " remote: " + client.getRemotePort());
            client.login("anon", "");
            Log.d("pwd", client.printWorkingDirectory());
            if (client.changeWorkingDirectory("groups/" + groupname)) {
                client.setFileType(FTP.BINARY_FILE_TYPE);
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                client.enterLocalPassiveMode();
                OutputStream outputStream = client.storeFileStream(file.getName());
                byte[] buffer = new byte[1024];
                long byteNum = 0;
                while (inputStream.read(buffer) != -1) {
                    outputStream.write(buffer);
                    byteNum++;
                    if (byteNum % (5 * file.length() / 1024 / 100) == 0) { //refresh every 5%
                        publishProgress();
                    }
                }
                outputStream.close();
                inputStream.close();
            }
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        callBack.refreshFileList();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        callBack.refreshFileList();
        callBack.announceUploadCompleted();
    }

    public interface SendFileToServerCallBack {
        void refreshFileList();

        void announceUploadCompleted();
    }
}
