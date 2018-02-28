package com.example.administrator.imagelistproject.model;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Edited by Administrator on 2018/2/28.
 * 图片缓存类
 */

public class ImageCache {
    //定义LruCache，指定其key和保存数据的类型
    private LruCache<Long, Bitmap> mImageCache;

    public ImageCache() {
        //获取当前进程可以使用的内存大小，单位换算为KB
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        //取总内存的1/4作为缓存
        final int cacheSize = maxMemory / 4;

        //初始化LruCache
        mImageCache = new LruCache<Long, Bitmap>(cacheSize) {
            //定义每一个存储对象的大小
            @Override
            protected int sizeOf(Long key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }

    //获取数据
    public Bitmap getBitmap(Long url) {
        return mImageCache.get(url);
    }

    //存储数据
    void putBitmap(Long url, Bitmap bitmap) {
        mImageCache.put(url, bitmap);
    }
}
