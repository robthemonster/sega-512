package com.themonster.segaclient;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by CJ Hernaez on 3/24/2018.
 */

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder> {

    Random random = new Random();
    private ArrayList<String> strings;
    private OnItemClickListener mListener;

    public GroupsAdapter(ArrayList<String> strs) {
        strings = strs;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
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
        //holder.mImageView.setImageResource(R.drawable.frame1);
        holder.cv.setBackgroundResource(chooseDrawable());
    }

    @Override
    public int getItemCount() {
        return strings.size();
    }

    int chooseDrawable() {
        int size = 7;
        int choice = random.nextInt(size);
        switch (1) {

            case 0:
                return R.drawable.grey_card;
            case 1:
                return R.drawable.blue_card;
            case 2:
                return R.drawable.teal_card;
            case 3:
                return R.drawable.yellow_card;
            case 4:
                return R.drawable.orange_card;
            case 5:
                return R.drawable.pink_card;
            case 6:
                return R.drawable.red_card;

        }

        return R.drawable.teal_card;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

    }

    public static class GroupsViewHolder extends RecyclerView.ViewHolder {

        public CardView cv;
        public TextView mTextView;
        public ImageView mImageView;

        public GroupsViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.cv_group_name);
            cv = itemView.findViewById(R.id.cv);
            mImageView = itemView.findViewById(R.id.cv_img);
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

        }
    }

}