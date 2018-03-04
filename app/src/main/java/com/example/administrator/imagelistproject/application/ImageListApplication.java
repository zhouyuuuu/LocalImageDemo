package com.example.administrator.imagelistproject.application;

import android.app.Application;

/**
 * Edited by Administrator on 2018/3/2.
 */

public class ImageListApplication extends Application {
    private static Application imageListApplication = null;

    public static Application getApplication() {
        return imageListApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        imageListApplication = this;
    }
}
