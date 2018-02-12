package com.example.administrator.imagelistproject;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

//Created by Administrator on 2018/2/5.

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageListViewHolder> {
    static final String TAG = "new";
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> mData;
    private ItemClickListener mItemClickListener;
    private static final int TYPE_ITEM = 101;
    private static final int TYPE_SUB_ITEM = 102;//这个是被展开出来的子项

    ImageListAdapter(Context context, ArrayList<String> data) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ImageListAdapter.ImageListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType){
            case TYPE_ITEM:
                v = mLayoutInflater.inflate(R.layout.view_imagelist_item,parent,false);
                return new ImageListViewHolder(v);
            case TYPE_SUB_ITEM:
                v = mLayoutInflater.inflate(R.layout.view_imagelist_subitem,parent,false);
                return new ImageListViewHolder(v);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final ImageListAdapter.ImageListViewHolder holder,int position) {
        if (!mData.get(position).equals("new")) {
            holder.iv.setImageResource(R.mipmap.ic_launcher);
            holder.tv.setText(String.valueOf(position));
        }else {
            holder.itemView.setTag(TAG);
            holder.iv.setImageResource(R.mipmap.ic_launcher_round);
            holder.tv.setText(String.valueOf(position));
        }
        holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener!=null){
                    mItemClickListener.OnItemClick(holder.getAdapterPosition(),holder);
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).equals("new")){
            return TYPE_SUB_ITEM;
        }else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
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



    void setItemClickListener(ItemClickListener itemClickListener){
        mItemClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void OnItemClick(int position,ImageListViewHolder holder);
    }
}
