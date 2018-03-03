package com.example.administrator.imagelistproject.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.example.administrator.imagelistproject.presenter.LoadImagePresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.provider.MediaStore.Images.Thumbnails.MICRO_KIND;

/**
 * Edited by Administrator on 2018/2/27.
 */

public class ImageLoader implements IModel {

    private LoadImagePresenter mLoadImagePresenter;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private final ImageLoader mImageLoader;

    public ImageLoader(LoadImagePresenter mLoadImagePresenter) {
        this.mLoadImagePresenter = mLoadImagePresenter;
        //使用线程池来管理加载图片和询问数据库的耗时操作
        mThreadPoolExecutor = new ThreadPoolExecutor(6, 10, 10, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
        mImageLoader = this;
    }

    /**
     * @param context 上下文
     */
    @Override
    public void loadLocalImageThumbnailId(Context context) {
        mThreadPoolExecutor.execute(new LoadImageThumbnailIdRunnable(1, context));
    }

    @Override
    public void imageListScrollIDEL() {
        new Thread(new NotifyImageListScrollIDELRunnable(1,null)).start();
    }

    /**
     * @param id      图片缩略图ID
     * @param context 上下文
     */
    @Override
    public void loadThumbnailBitmap(final long id, final Context context, final ImageCache images, final int position) {
        //再确认一次是否Cache中没有该图片
        if (images.getBitmap(id) == null) {
            mThreadPoolExecutor.execute(new LoadImageRunnable(1, id, context, images, position));
        }
    }

    public abstract class BaseRunnable implements Runnable,Comparable<BaseRunnable>{
        private int priority;
        private Context context;

        BaseRunnable(int priority, final Context context) {
            if (priority < 0)
                throw new IllegalArgumentException();
            this.priority = priority;
            this.context = context;
        }

        @Override
        public int compareTo(@NonNull BaseRunnable another) {
            int my = this.getPriority();
            int other = another.getPriority();
            return my < other ? 1 : my > other ? -1 : 0;
        }

        int getPriority() {
            return priority;
        }

        @Override
        public void run() {
            doSth(context);
        }

        abstract void doSth(Context context);
    }

    public class NotifyImageListScrollIDELRunnable extends BaseRunnable{
        NotifyImageListScrollIDELRunnable(int priority, Context context) {
            super(priority, context);
        }

        @Override
        void doSth(Context context) {
            synchronized (mImageLoader){
                mImageLoader.notifyAll();
            }
        }
    }

    /**
     * 加载图片缩略图ID的Runnable
     */
    public class LoadImageThumbnailIdRunnable extends BaseRunnable {

        LoadImageThumbnailIdRunnable(int priority, Context context) {
            super(priority, context);
        }

        /*
         * 通过Cursor拿到所有图片的路径，每拿到一张图片，对其路径提取出文件名，如果folderNames中存在该文件名，就通过文件名找到对应分组在分组列表中的位置后将图片添加进去，如果不存在则创建一个新分组，
         * folderNames中记录该分组名以及对应于分组列表中的位置，然后添加进分组列表。
         */
        @Override
        void doSth(Context context) {
            ArrayList<ArrayList<Long>> localImageThumbnailIds = new ArrayList<>();
            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            HashMap<String, Integer> folderNames = new HashMap<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //获取图片的id和文件夹名称
                    Long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    String folderName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    if (folderNames.containsKey(folderName)) {
                        int index = folderNames.get(folderName);
                        ArrayList<Long> aGroupOfIds = localImageThumbnailIds.get(index);
                        aGroupOfIds.add(id);
                    } else {
                        ArrayList<Long> aGroupOfIds = new ArrayList<>();
                        aGroupOfIds.add(id);
                        localImageThumbnailIds.add(aGroupOfIds);
                        folderNames.put(folderName, localImageThumbnailIds.size() - 1);
                    }
                }
                cursor.close();
            }
            mLoadImagePresenter.notifyImageThumbnailLoaded(localImageThumbnailIds);
        }
    }

    /**
     * 加载图片的Runnable
     */
    public class LoadImageRunnable extends BaseRunnable {
        private final ImageCache images;
        private long id;
        private int position;

        LoadImageRunnable(int priority, final long id, final Context context, final ImageCache images, final int position) {
            super(priority,context);
            if (priority < 0)
                throw new IllegalArgumentException();
            this.id = id;
            this.images = images;
            this.position = position;
        }


        @Override
        void doSth(Context context) {
            Bitmap image = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MICRO_KIND,
                    null);
            synchronized (images) {
                images.putBitmap(id, image);
            }
            synchronized (mImageLoader){
                while (!mLoadImagePresenter.checkViewReadyToRefresh()){
                    try {
                        mImageLoader.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mLoadImagePresenter.notifyImageLoaded(position);
            }
        }
    }


}
