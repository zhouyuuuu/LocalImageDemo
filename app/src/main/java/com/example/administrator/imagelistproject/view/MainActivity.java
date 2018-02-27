package com.example.administrator.imagelistproject.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import com.example.administrator.imagelistproject.R;
import com.example.administrator.imagelistproject.presenter.LoadImagePresenter;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/2/27.
 */

public class MainActivity extends AppCompatActivity implements IView {

    private static final int ANIMATOR_INTERVAL_DEFAULT = 200;//默认的动画时间
    RecyclerView mRecyclerView;
    ArrayList<ArrayList<Long>> mData;//所有的图片ID数据
    ArrayList<Long[]> mDataToShow;//用于展示的图片ID
    private ArrayList<Integer> mMarkList = new ArrayList<>();//用于记录被展开的Item位置以及展开子项数
    private ImageListAdapter mImageListAdapter;
    private LinearLayoutManager mLayoutManager;
    private RewriteItemAnimator mRewriteItemAnimator;//重新写的ItemAnimator
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        LoadImagePresenter mLoadImagePresenter = new LoadImagePresenter(this);
        //获得所有本地图片ID
        mData = mLoadImagePresenter.loadLocalImageThumbnailId(this);
        mDataToShow = new ArrayList<>();
        //先添加每个文件夹的第一张图片
        for (ArrayList<Long> ids : mData) {
            mDataToShow.add(new Long[]{ids.get(0), (long) ImageListAdapter.TYPE_ITEM});
            mMarkList.add(0);
        }
        mImageListAdapter = new ImageListAdapter(this, mDataToShow,mLoadImagePresenter);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mImageListAdapter.setItemClickListener(new ImageListAdapter.ItemClickListener() {
            @Override
            public void OnItemClick(int position, ImageListAdapter.ImageListViewHolder holder) {
                //点击项为子项时暂不进行操作
                if (mDataToShow.get(position)[1] == ImageListAdapter.TYPE_SUB_ITEM) return;
                //将被点击的View及其位置传递给ItemAnimator
                mRewriteItemAnimator.setClickedView(holder.itemView);
                //传入View的中点坐标
                mRewriteItemAnimator.setClickedX((holder.itemView.getLeft() + holder.itemView.getRight()) / 2);
                //判断该位置在mMarkList中是否有值，如果是0，则该item没有被展开，如果有值，该值为该item的子项数目
                if (mMarkList.get(position) == 0) {
                    //寻找点击的Item在mData中的位置
                    int index = -1;
                    for (int i = 0; i <= position; i++) {
                        if (mDataToShow.get(i)[1] == ImageListAdapter.TYPE_ITEM) {
                            index++;
                        }
                    }
                    //将点击Item所属mData的项从第一张图片开始导入到mDataToShow中
                    ArrayList<Long> newData = mData.get(index);
                    for (int i = newData.size() - 1; i >= 0; i--) {
                        mDataToShow.add(position + 1, new Long[]{newData.get(i), (long) ImageListAdapter.TYPE_SUB_ITEM});
                    }
                    //将展开信息同步到mMarkList
                    for (int i = 0; i < newData.size(); i++) {
                        mMarkList.add(position + 1, 0);
                    }
                    //将position标记为被展开
                    mMarkList.set(position, newData.size());
                    //执行该函数来触发Add动画
                    mImageListAdapter.notifyItemRangeInserted(position + 1, newData.size());
                    //该操作用于更新RecyclerView的position，因为Add和Remove后RecyclerView中item的position没有自动更新，引起错乱
                    mImageListAdapter.notifyItemRangeChanged(position + 1, newData.size());
                    //被点击项滑动至最左边
                    mLayoutManager.scrollToPositionWithOffset(position, 0);
                    //检查是否有其他的项被展开，有则记录下被展开的子项数目以及该展开项的position
                    boolean existExtendedItem = false;
                    int lastExtendedPosition = -1;
                    int lastExtendedSubItemCount = 0;
                    for (int i = 0; i < mMarkList.size(); i++) {
                        if (i != position && mMarkList.get(i) != 0) {
                            existExtendedItem = true;
                            lastExtendedPosition = i;
                            lastExtendedSubItemCount = mMarkList.get(i);
                        }
                    }
                    //如果存在被展开的其他项，则在添加动画完成之后，收起子项并执行删除动画
                    if (existExtendedItem) {
                        //设置存在被展开的其他项
                        mRewriteItemAnimator.setExistExtendedItem(true);
                        final int finalSubItemCount = lastExtendedSubItemCount;//要收起的子项数目
                        final int finalExtendedItemPosition = lastExtendedPosition;//要收起的项的位置
                        holder.itemView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mMarkList.set(finalExtendedItemPosition, 0);
                                for (int i = 0; i < finalSubItemCount; i++) {
                                    //删除数据
                                    mDataToShow.remove(finalExtendedItemPosition + 1);
                                    //列表同步
                                    mMarkList.remove(finalExtendedItemPosition + 1);
                                }
                                //执行该函数来触发Remove动画
                                mImageListAdapter.notifyItemRangeRemoved(finalExtendedItemPosition + 1, finalSubItemCount);
//                                mImageListAdapter.notifyItemRangeChanged(finalExtendedItemPosition + 1, finalSubItemCount);
                            }
                        }, ANIMATOR_INTERVAL_DEFAULT);
                    }
                } else {
                    //设置不存在被展开的其他项
                    mRewriteItemAnimator.setExistExtendedItem(false);
                    //拿到position对应的子项数目
                    int size = mMarkList.get(position);
                    //重新设置为没有被展开
                    mMarkList.set(position, 0);
                    //删除子项，并同步到mMarkList
                    for (int i = position + size; i > position; i--) {
                        mDataToShow.remove(i);
                        mMarkList.remove(i);
                    }
                    //执行该函数来触发Remove动画
                    mImageListAdapter.notifyItemRangeRemoved(position + 1, size);
                    //该操作用于更新RecyclerView的position，因为Add和Remove后RecyclerView中item的position没有自动更新，引起错乱
                    mImageListAdapter.notifyItemRangeChanged(position + 1, mDataToShow.size() - position - 1);
                    //被点击项滑动至最左边
                    mLayoutManager.scrollToPositionWithOffset(position, 0);
                }
            }
        });
        mRewriteItemAnimator = new RewriteItemAnimator();
        //设置动画时间保证各动画同步执行
        mRewriteItemAnimator.setAddDuration(ANIMATOR_INTERVAL_DEFAULT);
        mRewriteItemAnimator.setMoveDuration(ANIMATOR_INTERVAL_DEFAULT);
        mRewriteItemAnimator.setChangeDuration(ANIMATOR_INTERVAL_DEFAULT);
        mRewriteItemAnimator.setRemoveDuration(ANIMATOR_INTERVAL_DEFAULT);
        //设置屏幕宽度用于Remove位移计算
        mRewriteItemAnimator.setScreenWidth(this.getWindowManager().getDefaultDisplay().getWidth());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mImageListAdapter);
        mRecyclerView.setItemAnimator(mRewriteItemAnimator);
    }

    private void initView() {
        mProgressBar = findViewById(R.id.pb);
        mRecyclerView = findViewById(R.id.rv_image_list);
    }


    @Override
    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }
}
