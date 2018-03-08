package com.example.administrator.imagelistproject.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.example.administrator.imagelistproject.application.ImageListApplication;
import com.example.administrator.imagelistproject.image.IImageLoader;
import com.example.administrator.imagelistproject.image.ImageBean;
import com.example.administrator.imagelistproject.image.ImageCache;
import com.example.administrator.imagelistproject.image.ImageLoader;
import com.example.administrator.imagelistproject.localImageList.IImageList;

import java.util.ArrayList;


/**
 * Edited by Administrator on 2018/2/27.
 */

public class LoadImagePresenter {
    private IImageList iImageList;
    private IImageLoader imageLoader;
    private Context mContext;

    public LoadImagePresenter(@NonNull IImageList iImageList) {
        this.iImageList = iImageList;
        this.imageLoader = new ImageLoader(this);
        this.mContext = ImageListApplication.getApplication();
    }

    /**
     * 加载图片ID
     */
    public void loadLocalImageThumbnailId() {
        iImageList.showProgressBar();
        imageLoader.loadLocalImageBeans(mContext);
    }

    /**
     * 加载图片缩略图
     *
     * @param imageBean 图片信息
     * @param images 图片缓存集合，加载完成的图片会添加到该集合中
     */
    public void loadImageThumbnail(ImageBean imageBean, @NonNull ImageCache images) {
        imageLoader.loadImageThumbnail(imageBean, mContext, images);
    }

    /**
     * 通知View图片已经加载完毕
     *
     * @param imageBean 图片
     */
    public void refreshView(ImageBean imageBean) {
        iImageList.imageThumbnailLoadedCallback(imageBean);
    }

    /**
     * 通知View缩略图ID已经加载完毕
     *
     * @param localImageThumbnailIds 加载完成的缩略图ID集合
     */
    public void notifyImageThumbnailLoaded(@NonNull ArrayList<ArrayList<ImageBean>> localImageThumbnailIds) {
        iImageList.imageBeansLoadedCallback(localImageThumbnailIds);
        iImageList.hideProgressBar();
    }

    /**
     * 通知imageLoader可以开始将数据更新到View上
     */
    public void notifyIsReadyToRefreshView() {
        imageLoader.ImageListIsReadyToRefreshViewCallback();
    }

    /**
     * 检查是否View是可以刷新的状态
     * @return 是否可以刷新
     */
    public boolean checkViewReadyToRefresh() {
        return iImageList.isReadyToRefreshView();
    }
}
