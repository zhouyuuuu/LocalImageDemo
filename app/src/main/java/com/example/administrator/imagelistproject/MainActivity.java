package com.example.administrator.imagelistproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    ArrayList<String> mData;
    private ImageListAdapter mImageListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private int mPrePosition=-1;
    private int mPreSize=0;
    private CustomItemAnimator mCustomItemAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initData() {
        mData = new ArrayList<>();
        for (int i=0;i<20;i++){
            mData.add(i+"");
        }
        mImageListAdapter = new ImageListAdapter(this,mData);
        mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mImageListAdapter.setItemClickListener(new ImageListAdapter.ItemClickListener() {
            @Override
            public void OnItemClick(int position, ImageListAdapter.ImageListViewHolder holder) {
                View v = holder.itemView;
                mCustomItemAnimator.setEventX(v.getLeft());
                mCustomItemAnimator.setEventView(v);
                if (mPrePosition == position){
                    mCustomItemAnimator.setState(CustomItemAnimator.STATE_SHRINKING);
                    for (int i=mPrePosition+mPreSize;i>mPrePosition;i--) {
                        mData.remove(i);
                    }
//                    mImageListAdapter.notifyItemRangeChanged(position+1,mPreSize,"change_position");
                    mImageListAdapter.notifyItemRangeRemoved(mPrePosition+1,mPreSize);
                    mPrePosition = -1;
                    mPreSize = 0;
                }else {
                    if (mPrePosition != -1){
                        for (int i=mPrePosition+mPreSize;i>=mPrePosition+1;i--) {
                            mData.remove(i);
                        }
                    }
                    mImageListAdapter.notifyItemRangeRemoved(mPrePosition,mPreSize);
                    mImageListAdapter.notifyItemRangeChanged(0,mData.size());
                    if (position>mPrePosition)
                        position -= mPreSize;
                    mCustomItemAnimator.setState(CustomItemAnimator.STATE_EXPANDING);
                    ArrayList<String> newData = new ArrayList<>();
                    for (int i=0;i<5;i++) {
                        newData.add("new");
                    }
                    mPrePosition = position;
                    mData.addAll(position+1,newData);
                    mPreSize = newData.size();
                    mImageListAdapter.notifyItemRangeInserted(position+1,mPreSize);
                    ((LinearLayoutManager)mLayoutManager).scrollToPositionWithOffset(position,0);
                }
//                mData.set(position,"new");
//                mImageListAdapter.notifyItemChanged(position);
                mImageListAdapter.notifyItemRangeChanged(position+1,mData.size());
            }
        });
        mCustomItemAnimator = new CustomItemAnimator();
        mCustomItemAnimator.setAddDuration(2000);
        mCustomItemAnimator.setMoveDuration(2000);
        mCustomItemAnimator.setChangeDuration(2000);
        mCustomItemAnimator.setRemoveDuration(2000);
        WindowManager wm1 = this.getWindowManager();
        int width1 = wm1.getDefaultDisplay().getWidth();
        mCustomItemAnimator.setmScreenWidth(width1);


    }

    private void initView() {
        mRecyclerView = findViewById(R.id.rv_image_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mImageListAdapter);
//        mRecyclerView.getItemAnimator().setMoveDuration(2000);
        mRecyclerView.setItemAnimator(mCustomItemAnimator);
    }


}
