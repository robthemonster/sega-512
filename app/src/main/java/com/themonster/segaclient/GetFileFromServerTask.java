package com.themonster.segaclient;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by The Monster on 3/25/2018.
 */

public class GetFileFromServerTask extends AsyncTask<String, Float, Void> {
    private GetFileFromServerCallBack callBack;
    private String downloadLocation;

    public GetFileFromServerTask(GetFileFromServerCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected Void doInBackground(String... strings) {
        String groupname = strings[0];
        String filename = strings[1];
        String filesDir = strings[2];
        FTPClient client = new FTPClient();
        try {
            File localFile = new File(filesDir + File.separator + "groups" + File.separator + groupname + File.separator + filename);
            if (!localFile.getParentFile().getParentFile().exists()) {
                localFile.getParentFile().getParentFile().mkdir();
            }
            if (!localFile.getParentFile().exists()) {
                localFile.getParentFile().mkdir();
            }
            if (localFile.exists()) {
                localFile.delete();
            }
            if (localFile.createNewFile()) {
                client.connect(Constants.SEGA_SERVER_DNS, 6921);
                client.login("anon", "");
                if (client.changeWorkingDirectory("groups/" + groupname)) {
                    client.setFileType(FTP.BINARY_FILE_TYPE);
                    client.enterLocalPassiveMode();
                    FileOutputStream outputStream = new FileOutputStream(localFile);
                    if (client.retrieveFile(filename, outputStream)) {
                        downloadLocation = localFile.getPath();
                    } else {
                        localFile.delete();
                    }
                    outputStream.close();
                } else {
                    localFile.delete();
                    Log.d("cwd", "error changing directory on remote server");
                }
                client.disconnect();
            } else {
                Log.d("create file", "error creating local file to contain download");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        callBack.downloadCompleted(downloadLocation);
    }

    public interface GetFileFromServerCallBack {
        void refreshFileList();

        void downloadCompleted(String location);
    }
}
