package com.example.administrator.imagelistproject;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Administrator on 2018/2/8.
 */

class MoveInfo {
    private RecyclerView.ViewHolder holder;
    private int fromX;
    private int fromY;
    private int toX;
    private int toY;

    public MoveInfo(RecyclerView.ViewHolder holder,
                    int fromX, int fromY, int toX, int toY) {
        this.holder = holder;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }
}
