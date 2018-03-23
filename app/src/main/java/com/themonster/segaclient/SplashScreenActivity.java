package com.themonster.segaclient;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.splash4)); //TODO change back to 1. Currently testing
        ProgressBar pb = findViewById(R.id.splash_screen_phantom_spinner);
        pb.setVisibility(View.INVISIBLE);
        /*https://stackoverflow.com/questions/2819778/custom-drawable-for-progressbar-progressdialog*/

        Button button = findViewById(R.id.splash_screen_login_button);
        button.setOnClickListener(new View.OnClickListener() { // This will send the program into an XML file that I will use for testing and
            // trying to figure out the database and new ROOM environment
            @Override
            public void onClick(View view) {
                // Toast.makeText(SplashScreenActivity.this, "Button Pressed", Toast.LENGTH_SHORT).show();
                ProgressBar pb = findViewById(R.id.splash_screen_phantom_spinner);
                ProgressDialog dialog = new ProgressDialog(SplashScreenActivity.this);
                dialog.setIndeterminate(true);
                dialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.phantom_spinner));

                dialog.setMessage("Now how to make this a progress bar?");
                if (pb.getVisibility() == View.VISIBLE) {
                    dialog.setMessage("Oh like that. Turning it off.");
                }
                dialog.show();

                if (pb.getVisibility() == View.INVISIBLE) {
                    pb.setVisibility(View.VISIBLE);
                } else {
                    pb.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

}
