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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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

import static android.app.Activity.RESULT_OK;

public class DirectoryBrowserFragment extends Fragment implements SendFileToServerTask.SendFileToServerCallBack, GetFileFromServerTask.GetFileFromServerCallBack {

    private static final String ARG_GROUPNAME = "groupname";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_TOKEN = "token";

    private String groupname;
    private String username;
    private String token;
    private int selectedIndex = -1;

    private ArrayList<FileAttributes> fileList = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

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
            refreshFileList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_directory_browser, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null) {
            final ListView listView = getView().findViewById(R.id.fileListViewBrowserFragment);
            listView.setAdapter(new ArrayAdapter<FileAttributes>(getContext(), R.layout.group_directory_file_list_item, fileList) {
                @NonNull
                @Override
                public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View listItem = convertView;
                    if (listItem == null) {
                        listItem = LayoutInflater.from(getContext()).inflate(R.layout.group_directory_file_list_item, parent, false);
                    }
                    final FileAttributes fileAttributes = fileList.get(position);
                    ((TextView) listItem.findViewById(R.id.fileNameListItem)).setText(
                            String.format("%.32s", fileAttributes.getFileName() + (fileAttributes.getFileName().length() > 32 ? "..." : ""))
                    );
                    ((TextView) listItem.findViewById(R.id.fileTypeListItem)).setText(String.format("%.10s", fileAttributes.getFileType()));
                    ((TextView) listItem.findViewById(R.id.fileSizeListItem)).setText(Formatter.formatShortFileSize(getContext(), fileAttributes.getFileSize()));
                    listItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
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
                                    request.setUsername(username);
                                    request.setFirebaseToken(Constants.getFirebaseToken(getContext()));
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
                    return listItem;
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
                    ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
                }
            }, intentFilter);
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

    public void getFiles() {

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
                    FileAttributes fileAttributes = (FileAttributes) ((ListView) getView().findViewById(R.id.fileListViewBrowserFragment)).getItemAtPosition(selectedIndex);
                    task.execute(groupname, token, fileAttributes.getFileName(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                    selectedIndex = -1;
                }
            }

        }
    }

    @Override
    public void downloadCompleted(String location) {
        Toast.makeText(getContext(), "File downloaded to " + location, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshFileList() {
        GetFilesForGroupRequest request = new GetFilesForGroupRequest();
        request.setGroupname(groupname);
        request.setUsername(username);
        request.setFirebaseToken(Constants.getFirebaseToken(getContext()));
        SendRequestToServerTask task = new SendRequestToServerTask(request);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void announceUploadCompleted() {
        Toast.makeText(getContext(), "Upload complete!", Toast.LENGTH_SHORT).show();
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}