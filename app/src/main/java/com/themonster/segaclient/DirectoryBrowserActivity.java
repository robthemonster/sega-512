package com.themonster.segaclient;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class DirectoryBrowserActivity extends AppCompatActivity implements DirectoryBrowserFragment.OnFragmentInteractionListener {
    DirectoryBrowserFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_browser);
        fragment = DirectoryBrowserFragment.newInstance(
                getIntent().getStringExtra(Constants.GROUPNAME_EXTRA),
                getIntent().getStringExtra(Constants.USERNAME_EXTRA),
                getIntent().getStringExtra("token")
        );
        getFragmentManager().beginTransaction().replace(R.id.directoryBrowserFragment, fragment).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //what
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getFragmentManager().beginTransaction().remove(fragment).commit();
    }

    public void UploadFile(View view) {
        fragment.uploadFile();
    }
}
