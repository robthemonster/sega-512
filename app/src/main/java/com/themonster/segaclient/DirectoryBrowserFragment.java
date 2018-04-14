package com.themonster.segaclient;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.NormalFilePickActivity;
import com.vincent.filepicker.filter.entity.NormalFile;

import java.util.ArrayList;

import SEGAMessages.DeleteFileFromGroupRequest;
import SEGAMessages.DeleteFileFromGroupResponse;
import SEGAMessages.FileAttributes;
import SEGAMessages.GetFilesForGroupRequest;
import SEGAMessages.GetFilesForGroupResponse;
import SEGAMessages.RequestAuthorizationFromGroupRequest;
import SEGAMessages.RequestAuthorizationFromGroupResponse;
import SEGAMessages.ValidateTokenRequest;
import SEGAMessages.ValidateTokenResponse;

import static android.app.Activity.RESULT_OK;

public class DirectoryBrowserFragment extends Fragment implements SendFileToServerTask.SendFileToServerCallBack, GetFileFromServerTask.GetFileFromServerCallBack {

    private static final String ARG_GROUPNAME = "groupname";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_TOKEN = "token";
    android.support.v7.app.AlertDialog.Builder builder;
    SwipeRefreshLayout mSwipeRefreshLayout;
    boolean authorized = false;
    private String groupname;
    private String username;
    private String token;
    private boolean alive = true;
    private int selectedIndex = -1;
    private RecyclerView mRecyclerView;
    private FilesAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<FileAttributes> fileList = new ArrayList<>();
    private OnFragmentInteractionListener mListener;
    private CountDownTimer accessTimer;
    private BroadcastReceiver authReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RequestAuthorizationFromGroupResponse response = (RequestAuthorizationFromGroupResponse) intent.getSerializableExtra("response");
            if (response.isSucceeded()) {
                token = response.getToken();
                enterElevatedAccess();
                refreshFileList();
            } else {
                Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public DirectoryBrowserFragment() {
        // Required empty public constructor
    }

    public static DirectoryBrowserFragment newInstance(String groupname, String username, String token) {
        DirectoryBrowserFragment fragment = new DirectoryBrowserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUPNAME, groupname);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupname = getArguments().getString(ARG_GROUPNAME);
            username = getArguments().getString(ARG_USERNAME);
            token = getArguments().getString(ARG_TOKEN);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_directory_browser, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null) {
            super.onActivityCreated(savedInstanceState);
            mRecyclerView = getView().findViewById(R.id.files_recycler_view);
            mRecyclerView.setHasFixedSize(false);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mAdapter = new FilesAdapter(fileList);
            mRecyclerView.setAdapter(mAdapter);

            mSwipeRefreshLayout = getView().findViewById(R.id.files_swipe_container);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshFileList();

                }
            });


            FloatingActionButton uploadFab = getView().findViewById(R.id.upload_file_button_browser_fragment);
            uploadFab.setEnabled(false);
            uploadFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (authorized) {
                        uploadFile();
                    } else {
                        Toast.makeText(getContext(), "Authorization Required.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            FloatingActionButton requestAccessFab = getView().findViewById(R.id.request_access_button_browser_fragment);
            requestAccessFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RequestAccess();
                }
            });
            mAdapter.setOnItemClickListener(new FilesAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(final int position) {
                    if (!authorized) {
                        Toast.makeText(getContext(), "Authorization Required. Click on the Lock", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final FileAttributes fileAttributes = fileList.get(position);


                    DialogInterface.OnClickListener download = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            selectedIndex = position;
                            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permissions, 201);
                        }
                    };
                    DialogInterface.OnClickListener delete = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction(DeleteFileFromGroupResponse.TYPE);
                            LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    DeleteFileFromGroupResponse response = (DeleteFileFromGroupResponse) intent.getSerializableExtra("response");
                                    Toast.makeText(getContext(), response.isSucceeded() ? "File deleted" : response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                                    if (response.isSucceeded()) {
                                        refreshFileList();
                                    }
                                }
                            }, intentFilter);
                            DeleteFileFromGroupRequest request = new DeleteFileFromGroupRequest();
                            request.setFilename(fileAttributes.getFileName());
                            request.setGroupname(groupname);
                            request.setToken(token);
                            request.setUsername(username);
                            request.setFirebaseToken(Constants.getFirebaseToken(getContext().getApplicationContext()));
                            SendRequestToServerTask task = new SendRequestToServerTask(request);
                            task.execute();
                        }
                    };
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Download", download);
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Delete", delete);
                    alertDialog.show();
                }

            });
            mAdapter.setOnItemLongClickListener(new FilesAdapter.OnItemLongClickListener() {
                @Override
                public boolean onLongClick(int position) {
                    if (!authorized) {
                        return true;
                    }
                    Toast.makeText(DirectoryBrowserFragment.super.getActivity().getApplicationContext(), "Will Implement Soon TM", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(GetFilesForGroupResponse.TYPE);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    GetFilesForGroupResponse response = (GetFilesForGroupResponse) intent.getSerializableExtra("response");
                    if (response.getErrorMessage() != null) {
                        Log.d("getFiles", response.getErrorMessage());
                    }
                    fileList.clear();
                    fileList.addAll(response.getFiles());
                    //((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
                    mAdapter.notifyDataSetChanged();
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            }, intentFilter);
            refreshFileList();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void uploadFile() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(permissions, 200);
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permsRequestCode == 200) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(getActivity(), NormalFilePickActivity.class);
                intent.putExtra(Constant.MAX_NUMBER, 3);
                intent.putExtra(NormalFilePickActivity.SUFFIX, new String[]{"pdf", "jpg"});
                startActivityForResult(intent, Constant.REQUEST_CODE_PICK_FILE);
            }
        }
        if (permsRequestCode == 201) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                GetFileFromServerTask task = new GetFileFromServerTask(DirectoryBrowserFragment.this);
                if (selectedIndex != -1) {
                    FileAttributes fileAttributes = fileList.get(selectedIndex);
                    task.execute(groupname, token, fileAttributes.getFileName(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                    selectedIndex = -1;
                }
            }

        }
    }

    @Override
    public void downloadCompleted(String location) {
        Toast.makeText(getContext(), location == null ? "Access denied" : "File downloaded to " + location, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void refreshFileList() {
        if (getContext() != null) {
            GetFilesForGroupRequest request = new GetFilesForGroupRequest();
            request.setGroupname(getArguments().getString(ARG_GROUPNAME));
            request.setUsername(getArguments().getString(ARG_USERNAME));
            request.setFirebaseToken(Constants.getFirebaseToken(getContext().getApplicationContext()));
            SendRequestToServerTask task = new SendRequestToServerTask(request);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void announceUploadResult(Boolean successful) {
        Toast.makeText(getContext(), successful ? "Upload complete!" : "Upload failed. Access denied.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_PICK_FILE) {
            if (resultCode == RESULT_OK) {
                ArrayList<NormalFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);
                for (NormalFile normalFile : list) {
                    SendFileToServerTask task = new SendFileToServerTask(this);
                    task.execute(groupname, token, normalFile.getPath());
                }
            }
        }
    }

    public void RequestAccess() {
        RequestAuthorizationFromGroupRequest request = new RequestAuthorizationFromGroupRequest();
        request.setGroupName(groupname);
        request.setUsername(username);
        request.setFirebaseToken(Constants.getFirebaseToken(getContext().getApplicationContext()));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RequestAuthorizationFromGroupResponse.TYPE);
        LocalBroadcastManager.getInstance(getContext().getApplicationContext()).registerReceiver(authReceiver, intentFilter);
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        task.execute();
    }

    private void enterElevatedAccess() {
        FloatingActionButton uploadFab = getView().findViewById(R.id.upload_file_button_browser_fragment);
        uploadFab.setEnabled(true);
        FloatingActionButton requestAccessFab = getView().findViewById(R.id.request_access_button_browser_fragment);
        authorized = true;
        uploadFab.setColorFilter(Color.BLUE);
        requestAccessFab.setColorFilter(Color.BLUE);
        requestAccessFab.setEnabled(false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ValidateTokenResponse.TYPE);
        ((FilesAdapter) mRecyclerView.getAdapter()).setItemsLocked(false);
        accessTimer = new CountDownTimer(70000, 5000) {
            @Override
            public void onTick(long timeRemaining) {
                if (token != null) {
                    ValidateTokenRequest request = new ValidateTokenRequest();
                    request.setGroupname(groupname);
                    request.setToken(token);
                    request.setFirebaseToken(Constants.getFirebaseToken(getContext().getApplicationContext()));
                    SendRequestToServerTask task = new SendRequestToServerTask(request);
                    task.execute();
                }
            }

            @Override
            public void onFinish() {
                token = null;
                exitElevatedAccess();
                refreshFileList();
            }
        };
        LocalBroadcastManager.getInstance(getContext().getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ValidateTokenResponse response = (ValidateTokenResponse) intent.getSerializableExtra("response");
                if (!response.isTokenIsValid()) {
                    token = null;
                    accessTimer.cancel();
                    accessTimer.onFinish();
                }
            }
        }, intentFilter);
        accessTimer.start();
        Toast.makeText(getContext(), "Authorization granted.", Toast.LENGTH_SHORT).show();
    }

    private void exitElevatedAccess() {
        if (getView() != null) {
            FloatingActionButton uploadFab = getView().findViewById(R.id.upload_file_button_browser_fragment);
            uploadFab.setEnabled(false);
            ((FilesAdapter) mRecyclerView.getAdapter()).setItemsLocked(true);

            FloatingActionButton requestAccessFab = getView().findViewById(R.id.request_access_button_browser_fragment);
            requestAccessFab.setColorFilter(Color.GRAY);
            requestAccessFab.setEnabled(true);
            uploadFab.setColorFilter(Color.GRAY);
            Toast.makeText(getContext(), "Authorization expired.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        refreshFileList();
        super.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onDestroy() {
        authorized = false;
        LocalBroadcastManager.getInstance(getContext().getApplicationContext()).unregisterReceiver(authReceiver);
        if (accessTimer != null) {
            accessTimer.cancel();
        }
        alive = false;
        super.onDestroy();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}