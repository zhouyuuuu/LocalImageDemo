package com.example.administrator.imagelistproject.presenter;

import android.content.Context;

import com.example.administrator.imagelistproject.model.IModel;
import com.example.administrator.imagelistproject.model.ImageCache;
import com.example.administrator.imagelistproject.model.ImageLoader;
import com.example.administrator.imagelistproject.view.IView;

import java.util.ArrayList;


/**
 * Edited by Administrator on 2018/2/27.
 */

public class LoadImagePresenter {
    private IView iView;
    private IModel imageLoader;

    public LoadImagePresenter(IView iView) {
        this.iView = iView;
        this.imageLoader = new ImageLoader(this);
    }

    /**
     * 加载图片缩略图ID
     *
     * @param context 上下文
     */
    public void loadLocalImageThumbnailId(Context context) {
        iView.showProgressBar();
        imageLoader.loadLocalImageThumbnailId(context);
    }

    /**
     * 加载图片缩略图
     *
     * @param id       缩略图ID
     * @param context  上下文
     * @param images   图片缓存集合，加载完成的图片会添加到该集合中
     * @param position 图片对应于RecyclerView中的位置
     */
    public void loadThumbnailBitmap(long id, Context context, ImageCache images, int position) {
        imageLoader.loadThumbnailBitmap(id, context, images, position);
    }

    /**
     * 通知View图片已经加载完毕
     *
     * @param position 图片对应于RecyclerView中的位置
     */
    public void notifyImageLoaded(int position) {
        iView.imageLoaded(position);
    }

    /**
     * 通知View缩略图ID已经加载完毕
     *
     * @param localImageThumbnailIds 加载完成的缩略图ID集合
     */
    public void notifyImageThumbnailLoaded(ArrayList<ArrayList<Long>> localImageThumbnailIds) {
        iView.imageThumbnailLoaded(localImageThumbnailIds);
        iView.hideProgressBar();
    }
}
