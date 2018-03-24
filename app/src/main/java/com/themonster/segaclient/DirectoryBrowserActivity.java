package com.themonster.segaclient;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DirectoryBrowserActivity extends AppCompatActivity implements DirectoryBrowserFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_browser);
        Fragment fragment = DirectoryBrowserFragment.newInstance(getIntent().getStringExtra(Constants.GROUPNAME_EXTRA), getIntent().getStringExtra(Constants.USERNAME_EXTRA));
        getFragmentManager().beginTransaction().replace(R.id.directoryBrowserFragment, fragment).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //what
    }
}
