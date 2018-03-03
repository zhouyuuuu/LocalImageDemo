package com.example.administrator.imagelistproject.model;

import android.content.Context;


/**
 * Edited by Administrator on 2018/2/27.
 */

public interface IModel {
    void loadLocalImageThumbnailId(Context context);
    void imageListScrollIDEL();
    void loadThumbnailBitmap(long id, Context context, ImageCache images, int position);
}
