package com.example.administrator.imagelistproject.view;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/2/27.
 */

public interface IView {
    void showProgressBar();
    void hideProgressBar();
    void imageLoaded(int position);
    void imageThumbnailLoaded(ArrayList<ArrayList<Long>> localImageThumbnailIds);
    boolean isReadyToRefresh();
}
