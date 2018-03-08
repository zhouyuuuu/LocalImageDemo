package com.example.administrator.imagelistproject.image;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import com.example.administrator.imagelistproject.R;
import com.example.administrator.imagelistproject.application.ImageListApplication;
import com.example.administrator.imagelistproject.localImageList.ImageListAdapter;
import com.example.administrator.imagelistproject.presenter.LoadImagePresenter;
import com.example.administrator.imagelistproject.util.BitmapUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import static android.provider.MediaStore.Images.Thumbnails.MICRO_KIND;

/**
 * Edited by Administrator on 2018/2/27.
 */

public class ImageLoader implements IImageLoader {

    private final ImageLoader mImageLoader;
    private LoadImagePresenter mLoadImagePresenter;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private Bitmap mDefaultBitmap;

    public ImageLoader(@NonNull LoadImagePresenter mLoadImagePresenter) {
        this.mLoadImagePresenter = mLoadImagePresenter;
        //使用线程池来管理加载图片和询问数据库的耗时操作
        mThreadPoolExecutor = new ThreadPoolExecutor(6, 10, 10, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
        mImageLoader = this;
    }

    private void loadDefaultBitmap() {
        mDefaultBitmap = BitmapFactory.decodeResource(ImageListApplication.getApplication().getResources(), R.mipmap.error);
    }

    /**
     * @param context 上下文
     */
    @Override
    public void loadLocalImageBeans(@NonNull Context context) {
        mThreadPoolExecutor.execute(new LoadImageBeansRunnable(1, context));
    }

    @Override
    public void ImageListIsReadyToRefreshViewCallback() {
        //新建一条线程来通知线程池中的所有等待线程可以进行View的更新了
        new Thread(new NotifyThreadsToRefreshImageListRunnable(1, null)).start();
    }

    /**
     * @param imageBean 图片信息
     * @param context   上下文
     */
    @Override
    public void loadImageThumbnail(final ImageBean imageBean, @NonNull final Context context, @NonNull final ImageCache imageCache) {
        //再确认一次是否Cache中没有该图片
        if (imageCache.getBitmap(imageBean) == null) {
            mThreadPoolExecutor.execute(new LoadImageRunnable(1, imageBean, context, imageCache));
        }
    }

    public abstract class BaseRunnable implements Runnable, Comparable<BaseRunnable> {
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
            methodToRun(context);
        }

        abstract void methodToRun(Context context);
    }

    public class NotifyThreadsToRefreshImageListRunnable extends BaseRunnable {
        NotifyThreadsToRefreshImageListRunnable(int priority, Context context) {
            super(priority, context);
        }

        @Override
        void methodToRun(Context context) {
            synchronized (mImageLoader) {
                mImageLoader.notifyAll();
            }
        }
    }

    /**
     * 加载图片缩略图信息的Runnable
     */
    public class LoadImageBeansRunnable extends BaseRunnable {

        LoadImageBeansRunnable(int priority, Context context) {
            super(priority, context);
        }

        /*
         * 通过Cursor拿到所有图片的路径，每拿到一张图片，对其路径提取出文件名，如果folderNames中存在该文件名，就通过文件名找到对应分组在分组列表中的位置后将图片添加进去，如果不存在则创建一个新分组，
         * folderNames中记录该分组名以及对应于分组列表中的位置，然后添加进分组列表。
         */
        @Override
        void methodToRun(Context context) {
            ArrayList<ArrayList<ImageBean>> localImageThumbnailIds = new ArrayList<>();
            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            HashMap<String, Integer> folderNames = new HashMap<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //获取图片的id和文件夹名称
                    ImageBean imageBean = new ImageBean();
                    Long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    imageBean.setId(id);
                    imageBean.setUri(uri);
                    String folderName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    if (folderNames.containsKey(folderName)) {
                        imageBean.setItemType(ImageListAdapter.ITEM_TYPE_SUB_ITEM);
                        int index = folderNames.get(folderName);
                        ArrayList<ImageBean> aGroupOfBeans = localImageThumbnailIds.get(index);
                        aGroupOfBeans.add(imageBean);
                    } else {
                        ArrayList<ImageBean> aGroupOfBeans = new ArrayList<>();
                        imageBean.setItemType(ImageListAdapter.ITEM_TYPE_ITEM);
                        aGroupOfBeans.add(imageBean);
                        ImageBean copy = new ImageBean();
                        copy.setUri(imageBean.getUri());
                        copy.setId(imageBean.getId());
                        copy.setItemType(ImageListAdapter.ITEM_TYPE_SUB_ITEM);
                        aGroupOfBeans.add(copy);
                        localImageThumbnailIds.add(aGroupOfBeans);
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
        private ImageBean imageBean;

        LoadImageRunnable(int priority, final ImageBean imageBean, final Context context, final ImageCache images) {
            super(priority, context);
            if (priority < 0)
                throw new IllegalArgumentException();
            this.imageBean = imageBean;
            this.images = images;
        }


        @Override
        void methodToRun(Context context) {
            Bitmap image = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), imageBean.getId(), MICRO_KIND,
                    null);
            if (image == null) {
                image = BitmapUtil.loadBitmapThumbnail(imageBean.getUri());
                   if (image == null) {
                       if (mDefaultBitmap == null)
                           loadDefaultBitmap();
                       image = mDefaultBitmap;
                   }
            }
            synchronized (images) {
                images.putBitmap(imageBean, image);
            }
            synchronized (mImageLoader) {
                while (!mLoadImagePresenter.checkViewReadyToRefresh()) {
                    try {
                        //先让线程wait，等到View的状态是可以更新的时候notify
                        mImageLoader.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mLoadImagePresenter.refreshView(imageBean);
            }
        }
    }


}
