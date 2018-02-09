package com.example.administrator.imagelistproject;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;

//Created by Administrator on 2018/2/5.

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageListViewHolder> {
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> mData;
    private ItemClickListener mItemClickListener;

    ImageListAdapter(Context context, ArrayList<String> data) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ImageListAdapter.ImageListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.view_imagelist_item,parent,false);
        return new ImageListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ImageListAdapter.ImageListViewHolder holder, int position) {
        final int pos = position;
        if (!mData.get(pos).equals("new")) {
            holder.iv.setImageResource(R.mipmap.ic_launcher);
        }else {
            holder.iv.setImageResource(R.mipmap.ic_launcher_round);
        }
        holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener!=null){
                    mItemClickListener.OnItemClick(pos,holder);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    static class ImageListViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        ImageListViewHolder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv_image);
        }
    }



    public void setItemClickListener(ItemClickListener itemClickListener){
        mItemClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void OnItemClick(int position,ImageListViewHolder holder);
    }
}
