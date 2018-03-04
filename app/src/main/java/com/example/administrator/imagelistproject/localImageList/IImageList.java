package com.example.administrator.imagelistproject.localImageList;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/2/27.
 */

public interface IImageList {
    void showProgressBar();

    void hideProgressBar();

    void imageThumbnailLoadedCallback(long imageId);

    void imageIdsLoadedCallback(ArrayList<ArrayList<Long>> localImageThumbnailIds);

    boolean isReadyToRefreshView();
}
