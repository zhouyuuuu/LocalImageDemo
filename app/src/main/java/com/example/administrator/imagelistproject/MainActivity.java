package com.example.administrator.imagelistproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<Integer> mMarkList = new ArrayList<>();//用于标记那些被展开的item位置
    RecyclerView mRecyclerView;
    ArrayList<String> mData;//RecyclerView数据集
    private ImageListAdapter mImageListAdapter;
    private LinearLayoutManager mLayoutManager;
    private RewriteItemAnimator mRewriteItemAnimator;//重新写的ItemAnimator

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initData() {
        mData = new ArrayList<>();
        //默认数据
        for (int i=0;i<10;i++){
            mData.add(i+"");
            mMarkList.add(0);
        }
        mImageListAdapter = new ImageListAdapter(this,mData);
        mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mImageListAdapter.setItemClickListener(new ImageListAdapter.ItemClickListener() {
            @Override
            public void OnItemClick(int position, ImageListAdapter.ImageListViewHolder holder) {
                //点击项为子项时暂不进行操作
                if (mData.get(position).equals("new")) return;
                //将被点击的View及其位置传递给ItemAnimator
                mRewriteItemAnimator.setClickedView(holder.itemView);
                mRewriteItemAnimator.setClickedX(holder.itemView.getLeft());
                //判断该位置在mMarkList中是否有值，如果是0，则该item没有被展开，如果有值，该值为该item的子项数目
                if (mMarkList.get(position)==0){
                    //默认添加假数据
                    ArrayList<String> newData = new ArrayList<>();
                    for (int i=0;i<5;i++) {
                        newData.add("new");
                    }
                    mData.addAll(position+1,newData);
                    //将展开信息同步到mMarkList
                    for (int i=0;i<newData.size();i++){
                        mMarkList.add(position+1,0);
                    }
                    //将position标记为被展开
                    mMarkList.set(position,newData.size());
                    //执行该函数来触发Add动画
                    mImageListAdapter.notifyItemRangeInserted(position+1,newData.size());
                    //该操作用于更新RecyclerView的position，因为Add和Remove后RecyclerView中item的position没有自动更新，引起错乱
                    mImageListAdapter.notifyItemRangeChanged(position+1,newData.size());
                    //被点击项滑动至最左边
                    mLayoutManager.scrollToPositionWithOffset(position,0);
                }else {
                    //拿到position对应的子项数目
                    int size = mMarkList.get(position);
                    //重新设置为没有被展开
                    mMarkList.set(position,0);
                    //删除子项，并同步到mMarkList
                    for (int i=position+size;i>position;i--) {
                        mData.remove(i);
                        mMarkList.remove(i);
                    }
                    //执行该函数来触发Remove动画
                    mImageListAdapter.notifyItemRangeRemoved(position+1,size);
                    //该操作用于更新RecyclerView的position，因为Add和Remove后RecyclerView中item的position没有自动更新，引起错乱
                    mImageListAdapter.notifyItemRangeChanged(position+1,size);
                    //被点击项滑动至最左边
                    mLayoutManager.scrollToPositionWithOffset(position,0);
                }
            }
        });
        mRewriteItemAnimator = new RewriteItemAnimator();
        //设置动画时间保证各动画同步执行
        mRewriteItemAnimator.setAddDuration(200);
        mRewriteItemAnimator.setMoveDuration(200);
        mRewriteItemAnimator.setChangeDuration(200);
        mRewriteItemAnimator.setRemoveDuration(200);
        //设置屏幕宽度用于Remove位移计算
        WindowManager wm1 = this.getWindowManager();
        int width1 = wm1.getDefaultDisplay().getWidth();
        mRewriteItemAnimator.setScreenWidth(width1);
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.rv_image_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mImageListAdapter);
        mRecyclerView.setItemAnimator(mRewriteItemAnimator);
    }


}
