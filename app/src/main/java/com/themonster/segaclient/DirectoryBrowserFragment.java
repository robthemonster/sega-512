package com.themonster.segaclient;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.codekidlabs.storagechooser.StorageChooser;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import SEGAMessages.FileAttributes;
import SEGAMessages.GetFilesForGroupRequest;
import SEGAMessages.GetFilesForGroupResponse;
import SEGAMessages.UploadFileToGroupRequest;
import SEGAMessages.UploadFileToGroupResponse;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DirectoryBrowserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DirectoryBrowserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DirectoryBrowserFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String ARG_GROUPNAME = "groupname";
    private static final String ARG_USERNAME = "username";

    // TODO: Rename and change types of parameters
    private String groupname;
    private String username;

    private ArrayList<FileAttributes> fileList = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public DirectoryBrowserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param groupname Parameter 1.
     * @param username  Parameter 2.
     * @return A new instance of fragment DirectoryBrowserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DirectoryBrowserFragment newInstance(String groupname, String username) {
        DirectoryBrowserFragment fragment = new DirectoryBrowserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUPNAME, groupname);
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupname = getArguments().getString(ARG_GROUPNAME);
            username = getArguments().getString(ARG_USERNAME);
            GetFilesForGroupRequest request = new GetFilesForGroupRequest();
            request.setGroupname(groupname);
            request.setUsername(username);
            request.setFirebaseToken(getActivity().getSharedPreferences("firebaseToken", Context.MODE_PRIVATE).getString("token", ""));
            SendRequestToServerTask task = new SendRequestToServerTask(request);
            task.execute();
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
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View listItem = convertView;
                    if (listItem == null) {
                        listItem = LayoutInflater.from(getContext()).inflate(R.layout.group_directory_file_list_item, parent, false);
                    }
                    FileAttributes fileAttributes = fileList.get(position);
                    ((TextView) listItem.findViewById(R.id.fileNameListItem)).setText(fileAttributes.getFileName());
                    ((TextView) listItem.findViewById(R.id.fileSizeListItem)).setText(fileAttributes.getFileSize() + "");
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
                StorageChooser storageChooser = new StorageChooser.Builder()
                        .withActivity(getActivity())
                        .withFragmentManager(getFragmentManager())
                        .disableMultiSelect()
                        .allowCustomPath(true)
                        .setType(StorageChooser.FILE_PICKER)
                        .showFoldersInGrid(true)
                        .build();
                storageChooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
                    @Override
                    public void onSelect(String s) {
                        Log.d("File selected", s);
                        final UploadFileToGroupRequest request = new UploadFileToGroupRequest();
                        File file = new File(s);
                        if (file.exists()) {
                            try {
                                IntentFilter intentFilter = new IntentFilter();
                                intentFilter.addAction(UploadFileToGroupResponse.TYPE);
                                LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        UploadFileToGroupResponse response = (UploadFileToGroupResponse) intent.getSerializableExtra("response");
                                        Toast.makeText(getActivity(), response.isSucceeded() ? "Upload succeeded" : response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                                        if (response.isSucceeded()) {
                                            GetFilesForGroupRequest request = new GetFilesForGroupRequest();
                                            request.setGroupname(groupname);
                                            request.setUsername(username);
                                            request.setFirebaseToken(getActivity().getSharedPreferences("firebaseToken", Context.MODE_PRIVATE).getString("token", ""));
                                            SendRequestToServerTask task = new SendRequestToServerTask(request);
                                            task.execute();
                                        }
                                    }
                                }, intentFilter);
                                request.setFile(FileUtils.readFileToByteArray(file));
                                request.setFilename(file.getName());
                                request.setGroupname(groupname);
                                request.setUsername(username);
                                request.setFirebaseToken(getActivity().getSharedPreferences("firebaseToken", Context.MODE_PRIVATE).getString("token", ""));
                                SendRequestToServerTask task = new SendRequestToServerTask(request);
                                task.execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                storageChooser.show();
            }
        }
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
