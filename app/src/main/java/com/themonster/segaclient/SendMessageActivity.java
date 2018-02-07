package com.themonster.segaclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class SendMessageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        new Thread(new ListenForMessages()).start();
    }
    public void sendMessage(View view){
        if (findViewById(R.id.message_edittext) != null && findViewById(R.id.message_edittext) instanceof EditText){
            String message = ((EditText)findViewById(R.id.message_edittext)).getText().toString();
            SendMessageTask sendMessageTask = new SendMessageTask();
            sendMessageTask.execute(message);
        }
    }
}
