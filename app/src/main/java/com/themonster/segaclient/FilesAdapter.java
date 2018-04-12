package com.themonster.segaclient;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import SEGAMessages.FileAttributes;

/**
 * Created by CJ Hernaez on 3/24/2018.
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> {

    Random random = new Random();
    private ArrayList<FileAttributes> files;
    private OnItemClickListener mListener;
    private OnItemLongClickListener mLCListener;

    public FilesAdapter(ArrayList<FileAttributes> files) {
        this.files = files;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLCListener = listener;
    }


    @Override
    public FilesAdapter.FilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_files, parent, false);
        FilesViewHolder evh = new FilesViewHolder(v, mListener, mLCListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(FilesAdapter.FilesViewHolder holder, int position) {
        FileAttributes currItem = files.get(position);
        holder.mTextView.setText(currItem.getFileName());
        holder.cv.setBackgroundResource(R.drawable.red_card);

    }

    @Override
    public int getItemCount() {
        return files.size();

    }


    public interface OnItemClickListener {
        void onItemClick(int position);

    }

    public interface OnItemLongClickListener {
        boolean onLongClick(int position);
    }

    public static class FilesViewHolder extends RecyclerView.ViewHolder {

        public CardView cv;
        public TextView mTextView;
        //public ImageView mImageView;

        public FilesViewHolder(View itemView, final OnItemClickListener listener, final OnItemLongClickListener LListener) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.cv_file_name);
            cv = itemView.findViewById(R.id.cv_files);
            // mImageView = itemView.findViewById(R.id.cv_img);
            itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {

                            // cv.setCardBackgroundColor(Color.RED); //actually works if you want to include it
                            listener.onItemClick(position);
                        }
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {

                            // cv.setCardBackgroundColor(Color.RED); //actually works if you want to include it
                            LListener.onLongClick(position);
                        }
                    }
                    return true;
                }
            });
        }
    }

}