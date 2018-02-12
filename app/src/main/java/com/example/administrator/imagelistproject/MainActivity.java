package com.example.administrator.imagelistproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<Integer> markList = new ArrayList<>();//用于标记那些item被展开
    RecyclerView mRecyclerView;
    ArrayList<String> mData;
    private ImageListAdapter mImageListAdapter;
    private LinearLayoutManager mLayoutManager;
    private RewriteItemAnimator mCustomItemAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initData() {
        mData = new ArrayList<>();
        for (int i=0;i<10;i++){
            mData.add(i+"");
            markList.add(0);
        }
        mImageListAdapter = new ImageListAdapter(this,mData);
        mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mImageListAdapter.setItemClickListener(new ImageListAdapter.ItemClickListener() {
            @Override
            public void OnItemClick(int position, ImageListAdapter.ImageListViewHolder holder) {
                if (mData.get(position).equals("new")) return;
                mCustomItemAnimator.setClickedView(holder.itemView);
                mCustomItemAnimator.setClickedX(holder.itemView.getLeft());
                if (markList.get(position)==0){
                    ArrayList<String> newData = new ArrayList<>();
                    for (int i=0;i<5;i++) {
                        newData.add("new");
                    }
                    mData.addAll(position+1,newData);
                    for (int i=0;i<newData.size();i++){
                        markList.add(position+1,0);
                    }
                    markList.set(position,newData.size());
                    mImageListAdapter.notifyItemRangeInserted(position+1,newData.size());
                    mImageListAdapter.notifyItemRangeChanged(position+1,newData.size());
                    mLayoutManager.scrollToPositionWithOffset(position,0);
                }else {
                    int size = markList.get(position);
                    markList.set(position,0);
                    for (int i=position+size;i>position;i--) {
                        mData.remove(i);
                        markList.remove(i);
                    }
                    mImageListAdapter.notifyItemRangeRemoved(position+1,size);
                    mImageListAdapter.notifyItemRangeChanged(position+1,size);
                    mLayoutManager.scrollToPositionWithOffset(position,0);
                }
            }
        });
        mCustomItemAnimator = new RewriteItemAnimator();
        mCustomItemAnimator.setAddDuration(200);
        mCustomItemAnimator.setMoveDuration(200);
        mCustomItemAnimator.setChangeDuration(200);
        mCustomItemAnimator.setRemoveDuration(200);
        WindowManager wm1 = this.getWindowManager();
        int width1 = wm1.getDefaultDisplay().getWidth();
        mCustomItemAnimator.setScreenWidth(width1);
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.rv_image_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mImageListAdapter);
        mRecyclerView.setItemAnimator(mCustomItemAnimator);
    }


}
