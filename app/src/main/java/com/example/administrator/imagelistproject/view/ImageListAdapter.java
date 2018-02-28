package com.example.administrator.imagelistproject.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.imagelistproject.R;
import com.example.administrator.imagelistproject.model.ImageCache;
import com.example.administrator.imagelistproject.presenter.LoadImagePresenter;

import java.util.ArrayList;


/**
 * Edited by Administrator on 2018/2/27.
 */

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageListViewHolder> implements IView {
    static final String TAG = "new";//这个TAG是用于标记子项的View，在Animator中需要用到该TAG区分项和子项
    static final int TYPE_ITEM = 101;
    static final int TYPE_SUB_ITEM = 102;//这个是被展开出来的子项
    private LayoutInflater mLayoutInflater;
    private ArrayList<Long[]> mData;
    private ItemClickListener mItemClickListener;
    private Context mContext;
    private LoadImagePresenter mLoadImagePresenter;
    private ImageCache mImages;
    private RecyclerView mRecyclerView;

    ImageListAdapter(Context context, ArrayList<Long[]> data, ImageCache images, RecyclerView recyclerView) {
        mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mData = data;
        mLoadImagePresenter = new LoadImagePresenter(this);
        mImages = images;
        mRecyclerView = recyclerView;
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
        //如果在mImages中不存在该图片，则先将ImageView设置为空，然后开线程去加载图片，待图片加载完成时会回调imageLoaded
        Bitmap image = mImages.getBitmap(mData.get(position)[0]);
        if (image != null) {
            holder.iv.setImageBitmap(image);
        } else {
            holder.iv.setImageBitmap(null);
            mLoadImagePresenter.loadThumbnailBitmap(mData.get(position)[0], mContext, mImages, position);
        }
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

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void imageLoaded(final int position) {
        //等待RecyclerView停止测量和停止滑动后通知Item进行更新，若不等待停止测量和滑动就通知刷新则会抛异常
        while (true) {
            if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && !mRecyclerView.isComputingLayout())
                break;
        }
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: 调用notifyItemChange" + position);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public void imageThumbnailLoaded(ArrayList<ArrayList<Long>> localImageThumbnailIds) {
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
