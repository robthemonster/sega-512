package com.themonster.segaclient;

import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.File;
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
        String token = strings[1];
        String filename = strings[2];
        String filesDir = strings[3];

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
                Session session = Constants.jSch.getSession(groupname, Constants.SEGA_SERVER_DNS, 6921);
                session.setPassword(token);
                session.connect();
                Channel channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftp = (ChannelSftp) channel;
                sftp.cd("groups/" + groupname);
                sftp.get(filename, localFile.getAbsolutePath());
                downloadLocation = localFile.getPath();
            } else {
                Log.d("create file", "error creating local file to contain download");
            }
        } catch (IOException | JSchException | SftpException e) {
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
