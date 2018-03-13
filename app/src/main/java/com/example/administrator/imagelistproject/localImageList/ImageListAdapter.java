package com.example.administrator.imagelistproject.localImageList;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.imagelistproject.R;
import com.example.administrator.imagelistproject.image.ImageBean;
import com.example.administrator.imagelistproject.image.ImageCache;
import com.example.administrator.imagelistproject.presenter.LoadImagePresenter;
import com.example.administrator.imagelistproject.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Edited by Administrator on 2018/2/27.
 */

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageListViewHolder> implements IImageList {
    private static final String TAG = "com.example.administrator.imagelistproject.localImageList.ImageListAdapter";
    public static final int ITEM_TYPE_ITEM = 101;
    public static final int ITEM_TYPE_SUB_ITEM = 102;//这个是被展开出来的子项
    private LayoutInflater mLayoutInflater;
    private final ArrayList<ImageBean> mImageBeans;
    private ItemClickListener mItemClickListener;
    private LoadImagePresenter mLoadImagePresenter;
    private ImageCache mImageCache;
    private RecyclerView mRvToBind;
    private ConcurrentHashMap<ImageBean,Integer> mImageIdAndItsPositionInShowingImageList;

    ImageListAdapter(Context context, ArrayList<ImageBean> data, ImageCache images, ConcurrentHashMap<ImageBean,Integer> imageIdAndItsPositionInShowingImageList, RecyclerView recyclerView) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mImageBeans = data;
        mLoadImagePresenter = new LoadImagePresenter(this);
        mImageCache = images;
        mRvToBind = recyclerView;
        mImageIdAndItsPositionInShowingImageList = imageIdAndItsPositionInShowingImageList;
    }


    @NonNull
    @Override
    public ImageListAdapter.ImageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (viewType == ITEM_TYPE_ITEM){
            v = mLayoutInflater.inflate(R.layout.view_imagelist_item, parent, false);
            return new ImageListViewHolder(v);
        }else {
            v = mLayoutInflater.inflate(R.layout.view_imagelist_subitem, parent, false);
            v.setTag(TelescopicItemAnimator.ITEM_TYPE_SUB_ITEM);
            return new ImageListViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageListAdapter.ImageListViewHolder holder, int position) {
        //如果在mImages中不存在该图片，则先将ImageView设置为空，然后开线程去加载图片，待图片加载完成时会回调imageLoaded
        Bitmap image = mImageCache.getBitmap(mImageBeans.get(position));
        if (image != null) {
            holder.iv.setImageBitmap(image);
        } else {
            holder.iv.setImageResource(R.drawable.bg_gray_round);
            mLoadImagePresenter.loadImageThumbnail(mImageBeans.get(position), mImageCache);
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

    /**
     * 重写了这个方法，如果payloads不为空，就不重新绑定View了避免Change动画执行覆盖掉其他动画
     */
    @Override
    public void onBindViewHolder(@NonNull ImageListViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mImageBeans.get(position).getItemType() == ITEM_TYPE_SUB_ITEM) {
            return ITEM_TYPE_SUB_ITEM;
        } else {
            return ITEM_TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mImageBeans == null ? 0 : mImageBeans.size();
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
    public void imageThumbnailLoadedCallback(ImageBean imageBean) {
        final Integer position = mImageIdAndItsPositionInShowingImageList.get(imageBean);
        if (position != null) {
            mRvToBind.post(new Runnable() {
                @Override
                public void run() {
                    LogUtil.e(TAG, "imageThumbnailLoadedCallback : 加载成功，更新了Item" + position);
                    notifyItemChanged(position);
                }
            });
        }
    }

    @Override
    public void imageBeansLoadedCallback(@NonNull ArrayList<ArrayList<ImageBean>> localImageThumbnailIds) {
    }

    @Override
    public boolean isReadyToRefreshView() {
        return mRvToBind.getScrollState() == RecyclerView.SCROLL_STATE_IDLE ;
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

    void recyclerViewScrollStateIsIDEL(){
        mLoadImagePresenter.notifyIsReadyToRefreshView();
    }

    void cancelAllLoadTask(){
        mLoadImagePresenter.stopLoading();
    }
}
