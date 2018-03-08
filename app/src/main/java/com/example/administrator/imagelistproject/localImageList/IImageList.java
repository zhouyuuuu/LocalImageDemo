package com.example.administrator.imagelistproject.localImageList;

import android.support.annotation.NonNull;

import com.example.administrator.imagelistproject.image.ImageBean;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/2/27.
 */

public interface IImageList {
    void showProgressBar();

    void hideProgressBar();

    void imageThumbnailLoadedCallback(ImageBean imageBean);

    void imageBeansLoadedCallback(@NonNull ArrayList<ArrayList<ImageBean>> localImageThumbnailIds);

    boolean isReadyToRefreshView();
}
