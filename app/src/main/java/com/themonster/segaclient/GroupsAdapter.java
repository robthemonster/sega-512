package com.themonster.segaclient;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by CJ Hernaez on 3/24/2018.
 */

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder> {

    private ArrayList<String> strings;

    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mListener = listener;
    }


    public static class GroupsViewHolder extends RecyclerView.ViewHolder {

        public CardView cv;
        public TextView mTextView;
        public GroupsViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            mTextView= itemView.findViewById(R.id.cv_group_name);
            cv = itemView.findViewById(R.id.cv);
            itemView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v)
                {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                        {

                            // cv.setCardBackgroundColor(Color.RED); //actually works if you want to include it
                            listener.onItemClick(position);
                        }
                    }
                }
            });

        }
    }

    public GroupsAdapter(ArrayList<String> strs){
        strings = strs;
    }

    @Override
    public GroupsAdapter.GroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_groups, parent, false);
        GroupsViewHolder evh = new GroupsViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(GroupsAdapter.GroupsViewHolder holder, int position) {
        String currItem = strings.get(position);
        holder.mTextView.setText(currItem);
    }

    @Override
    public int getItemCount() {
        return strings.size();
    }


    /*
    private interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListenmer(OnItemClickListener listener)
    {
        mListener = listener;
    }
    // Provide a suitable constructor (depends on the kind of dataset)
    public GroupsAdapter(ArrayList<String> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GroupsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_groups, parent, false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LOGDAT", "D");
            }
        });
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        CardView cv;

        public ViewHolder(View v) {
            super(v);
            cv = itemView.findViewById(R.id.cv);
            mTextView = itemView.findViewById(R.id.cv_group_name);
        }
    }
    */
}