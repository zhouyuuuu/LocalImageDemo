package com.example.administrator.imagelistproject.image;

/**
 * Edited by Administrator on 2018/3/8.
 */

public class ImageBean {
    private String mUri;
    private Long mId;
    private int itemType;

    public int getItemType() {
        return itemType;
    }

    void setItemType(int itemType) {
        this.itemType = itemType;
    }

    String getUri() {
        return mUri;
    }

    void setUri(String mUrl) {
        this.mUri = mUrl;
    }

    public Long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }
}
