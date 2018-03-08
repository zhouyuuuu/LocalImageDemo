package com.example.administrator.imagelistproject.image;

import android.content.Context;
import android.support.annotation.NonNull;


/**
 * Edited by Administrator on 2018/2/27.
 */

public interface IImageLoader {
    void loadLocalImageBeans(@NonNull Context context);

    void ImageListIsReadyToRefreshViewCallback();

    void loadImageThumbnail(ImageBean imageBean, @NonNull Context context, @NonNull ImageCache images);
}
