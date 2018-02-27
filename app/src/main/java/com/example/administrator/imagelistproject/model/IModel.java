package com.example.administrator.imagelistproject.model;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/2/27.
 */

public interface IModel {
    ArrayList<ArrayList<Long>> loadLocalImageThumbnailId(Context context);
    Bitmap getThumbnailBitmap(long id, Context context);
}
