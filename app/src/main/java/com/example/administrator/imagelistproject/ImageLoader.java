package com.example.administrator.imagelistproject;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.HashMap;

import static android.provider.MediaStore.Images.Thumbnails.MICRO_KIND;

/**
 * Edited by Administrator on 2018/2/27.
 */

class ImageLoader {

    /**
     * 通过Cursor拿到所有图片的路径，每拿到一张图片，对其路径提取出文件名，如果folderNames中存在该文件名，就通过文件名找到对应分组在分组列表中的位置后将图片添加进去，如果不存在则创建一个新分组，folderNames中记录该分组名以及
     * 对应于分组列表中的位置，然后添加进分组列表。
     *
     * @param context 上下文
     * @return 返回一个列表，列表存放图片的所有分组
     */
    static ArrayList<ArrayList<Long>> loadLocalImageThumbnailId(Context context) {
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
        return localImageThumbnailIds;
    }

    /**
     * 通过图片路径获得该图片对应的系统生成的缩略图id
     *
     * @param path    本地图片路径
     * @param context 上下文
     * @return 返回图片的缩略图ID
     */
    private static long getDbId(String path, Context context) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.MediaColumns.DATA + "=?";
        String[] selectionArgs = new String[]{
                path
        };
        String[] columns = new String[]{
                MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA
        };
        Cursor c = context.getContentResolver()
                .query(uri, columns, selection, selectionArgs, null);
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
     * 通过图片的缩略图id得到图片的缩略图
     *
     * @param id      图片缩略图ID
     * @param context 上下文
     * @return 返回缩略图
     */
    static Bitmap getThumbnailBitmap(long id, Context context) {
        return MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MICRO_KIND,
                null);
    }
}
