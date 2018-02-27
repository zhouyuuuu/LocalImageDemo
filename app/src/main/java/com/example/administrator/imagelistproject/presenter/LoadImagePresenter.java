package com.example.administrator.imagelistproject.presenter;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.administrator.imagelistproject.model.IModel;
import com.example.administrator.imagelistproject.model.ImageLoader;
import com.example.administrator.imagelistproject.view.IView;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/2/27.
 */

public class LoadImagePresenter {
    private IView activity;
    private IModel imageLoader;

    public LoadImagePresenter(IView activity) {
        this.activity = activity;
        this.imageLoader = new ImageLoader();
    }

    public ArrayList<ArrayList<Long>> loadLocalImageThumbnailId(Context context){
        activity.showProgressBar();
        ArrayList<ArrayList<Long>> result = imageLoader.loadLocalImageThumbnailId(context);
        activity.hideProgressBar();
        return result;
    }

    public Bitmap getThumbnailBitmap(long id, Context context){
        return imageLoader.getThumbnailBitmap(id,context);
    }
}
