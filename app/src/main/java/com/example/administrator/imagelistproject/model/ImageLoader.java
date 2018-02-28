package com.example.administrator.imagelistproject.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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

    public ImageLoader(LoadImagePresenter mLoadImagePresenter) {
        this.mLoadImagePresenter = mLoadImagePresenter;
        //使用线程池来管理加载图片和询问数据库的耗时操作
        mThreadPoolExecutor = new ThreadPoolExecutor(6, 10, 10, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
    }

    /**
     * @param context 上下文
     */
    @Override
    public void loadLocalImageThumbnailId(Context context) {
        mThreadPoolExecutor.execute(new LoadImageThumbnailIdRunnable(1, context));
    }

    /**
     * 通过图片路径获得该图片对应的系统生成的缩略图id
     *
     * @param path    本地图片路径
     * @param context 上下文
     * @return 返回图片的缩略图ID
     */
    private long getDbId(String path, Context context) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.MediaColumns.DATA + "=?";
        String[] selectionArgs = new String[]{path};
        String[] columns = new String[]{MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA};
        Cursor c = context.getContentResolver().query(uri, columns, selection, selectionArgs, null);
        if (c == null) {
            return 0;
        }
        long id = 0;
        if (c.moveToNext()) {
            id = c.getLong(0);
        }
        c.close();
        return id;
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

    /**
     * 加载图片缩略图ID的Runnable
     */
    public class LoadImageThumbnailIdRunnable implements Runnable, Comparable<LoadImageRunnable> {
        private int priority;
        private Context context;

        LoadImageThumbnailIdRunnable(int priority, final Context context) {
            if (priority < 0)
                throw new IllegalArgumentException();
            this.priority = priority;
            this.context = context;
        }

        @Override
        public int compareTo(@NonNull LoadImageRunnable another) {
            int my = this.getPriority();
            int other = another.getPriority();
            return my < other ? 1 : my > other ? -1 : 0;
        }

        /**
         * 通过Cursor拿到所有图片的路径，每拿到一张图片，对其路径提取出文件名，如果folderNames中存在该文件名，就通过文件名找到对应分组在分组列表中的位置后将图片添加进去，如果不存在则创建一个新分组，
         * folderNames中记录该分组名以及对应于分组列表中的位置，然后添加进分组列表。
         */
        @Override
        public void run() {
            ArrayList<ArrayList<Long>> localImageThumbnailIds = new ArrayList<>();
            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            HashMap<String, Integer> folderNames = new HashMap<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //获取图片的路径
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    StringBuilder stringBuilder = new StringBuilder(path);
                    String[] strings = path.split("/");
                    String fileName = strings[strings.length - 1];
                    stringBuilder.delete(path.length() - fileName.length(), path.length());
                    stringBuilder.trimToSize();
                    String folderName = stringBuilder.toString();
                    if (folderNames.containsKey(folderName)) {
                        int index = folderNames.get(folderName);
                        ArrayList<Long> aGroupOfIds = localImageThumbnailIds.get(index);
                        aGroupOfIds.add(getDbId(path, context));
                    } else {
                        ArrayList<Long> aGroupOfIds = new ArrayList<>();
                        aGroupOfIds.add(getDbId(path, context));
                        localImageThumbnailIds.add(aGroupOfIds);
                        folderNames.put(folderName, localImageThumbnailIds.size() - 1);
                    }
                }
                cursor.close();
            }
            mLoadImagePresenter.notifyImageThumbnailLoaded(localImageThumbnailIds);
        }

        int getPriority() {
            return priority;
        }
    }

    /**
     * 加载图片的Runnable
     */
    public class LoadImageRunnable implements Runnable, Comparable<LoadImageRunnable> {
        private final ImageCache images;
        private int priority;
        private long id;
        private Context context;
        private int position;

        LoadImageRunnable(int priority, final long id, final Context context, final ImageCache images, final int position) {
            if (priority < 0)
                throw new IllegalArgumentException();
            this.priority = priority;
            this.id = id;
            this.context = context;
            this.images = images;
            this.position = position;
        }

        @Override
        public int compareTo(@NonNull LoadImageRunnable another) {
            int my = this.getPriority();
            int other = another.getPriority();
            return my < other ? 1 : my > other ? -1 : 0;
        }

        /**
         * 通过图片的缩略图id得到图片的缩略图
         */
        @Override
        public void run() {
            Bitmap image = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MICRO_KIND,
                    null);
            synchronized (images) {
                images.putBitmap(id, image);
            }
            mLoadImagePresenter.notifyImageLoaded(position);
        }

        int getPriority() {
            return priority;
        }
    }


}
