package com.example.administrator.imagelistproject;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/2/27.
 */

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageListViewHolder> {
    static final String TAG = "new";//这个TAG是用于标记子项的View，在Animator中需要用到该TAG区分项和子项
    static final int TYPE_ITEM = 101;
    static final int TYPE_SUB_ITEM = 102;//这个是被展开出来的子项
    private LayoutInflater mLayoutInflater;
    private ArrayList<Long[]> mData;
    private ItemClickListener mItemClickListener;
    private Context mContext;

    ImageListAdapter(Context context, ArrayList<Long[]> data) {
        mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ImageListAdapter.ImageListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case TYPE_ITEM:
                v = mLayoutInflater.inflate(R.layout.view_imagelist_item, parent, false);
                return new ImageListViewHolder(v);
            case TYPE_SUB_ITEM:
                v = mLayoutInflater.inflate(R.layout.view_imagelist_subitem, parent, false);
                v.setTag(TAG);
                return new ImageListViewHolder(v);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final ImageListAdapter.ImageListViewHolder holder, int position) {
        holder.iv.setImageBitmap(ImageLoader.getThumbnailBitmap(mData.get(position)[0], mContext));
        holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.OnItemClick(holder.getAdapterPosition(), holder);
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position)[1] == TYPE_SUB_ITEM) {
            return TYPE_SUB_ITEM;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    void setItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }


    public interface ItemClickListener {
        void OnItemClick(int position, ImageListViewHolder holder);
    }

    static class ImageListViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        ImageView iv;

        ImageListViewHolder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv_image);
            tv = itemView.findViewById(R.id.tv_image);
        }
    }
}
