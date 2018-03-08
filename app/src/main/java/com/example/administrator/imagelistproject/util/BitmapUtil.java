package com.example.administrator.imagelistproject.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.example.administrator.imagelistproject.application.ImageListApplication;

/**
 * Edited by Administrator on 2018/3/8.
 */

public class BitmapUtil {
    private static final int RESIZE_WIDTH_DEFAULT = 70;
    public static Bitmap loadBitmapThumbnail(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int width = options.outWidth;
        options.inSampleSize = (int) (width / dip2px(RESIZE_WIDTH_DEFAULT));
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }

    private static float dip2px(float dpValue) {
        float scale = ImageListApplication.getApplication().getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }
}
