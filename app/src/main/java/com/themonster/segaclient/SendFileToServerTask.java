package com.themonster.segaclient;

import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by The Monster on 3/25/2018.
 */

public class SendFileToServerTask extends AsyncTask<String, Void, Boolean> {
    private SendFileToServerCallBack callBack;

    public SendFileToServerTask(SendFileToServerCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String groupname = strings[0];
        String token = strings[1];
        File file = new File(strings[2]);
        if (!file.exists()) {
            Log.d("f", "upload file does not exist");
            return null;
        }
        try {
            Session session = Constants.jSch.getSession(groupname, Constants.SEGA_SERVER_DNS, 6921);
            session.setPassword(token);
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftp = (ChannelSftp) channel;
            sftp.cd("groups/" + groupname);
            sftp.put(new FileInputStream(file), file.getName());
            sftp.exit();
            session.disconnect();
            return true;
        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        callBack.refreshFileList();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        callBack.refreshFileList();
        callBack.announceUploadResult(aBoolean);
    }

    public interface SendFileToServerCallBack {
        void refreshFileList();

        void announceUploadResult(Boolean aBoolean);
    }
}
