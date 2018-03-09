package com.example.administrator.imagelistproject.application;

import android.annotation.SuppressLint;
import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Edited by Administrator on 2018/3/2.
 */

public class ImageListApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Application imageListApplication = null;

    public static Application getApplication() {
        return imageListApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        imageListApplication = this;
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}
