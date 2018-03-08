package com.example.administrator.imagelistproject.application;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Edited by Administrator on 2018/3/2.
 */

public class ImageListApplication extends Application {
    private static Application imageListApplication = null;
    private static RefWatcher mRefWatcher = null;

    public static Application getApplication() {
        return imageListApplication;
    }

    public static RefWatcher getRefWatcher(){
        return mRefWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        imageListApplication = this;
        mRefWatcher = LeakCanary.install(this);
    }
}
