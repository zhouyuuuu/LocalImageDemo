package com.example.administrator.imagelistproject.image;

import android.content.Context;


/**
 * Edited by Administrator on 2018/2/27.
 */

public interface IImageLoader {
    void loadLocalImageIds(Context context);

    void ImageListIsReadyToRefreshViewCallback();

    void loadImageThumbnail(long id, Context context, ImageCache images);
}
