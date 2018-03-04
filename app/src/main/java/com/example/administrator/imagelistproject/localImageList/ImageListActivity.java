package com.example.administrator.imagelistproject.localImageList;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.example.administrator.imagelistproject.R;
import com.example.administrator.imagelistproject.image.ImageCache;
import com.example.administrator.imagelistproject.presenter.LoadImagePresenter;
import com.example.administrator.imagelistproject.util.LogUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;


/**
 * Edited by Administrator on 2018/2/27.
 */

public class ImageListActivity extends AppCompatActivity implements IImageList {

    private static final int ANIMATOR_INTERVAL_DEFAULT = 200;//默认的动画时间
    private RecyclerView mRvImageList;
    private ArrayList<ArrayList<Long>> mAllImageIds;//所有的图片ID数据
    private ArrayList<Long[]> mIdAndTypeOfShowingImages;//用于展示的图片ID
    private boolean mRecyclerViewExecutingAnimation = false;//recyclerView是否正在执行动画
    private ArrayList<Integer> mMarkItemTypeList = new ArrayList<>();//用于记录被展开的Item位置以及展开子项数
    private ImageListAdapter mImageListAdapter;
    private LinearLayoutManager mLayoutManager;
    private TelescopicItemAnimator mTelescopicItemAnimator;//重新写的ItemAnimator
    private ProgressBar mPbLoadingImageIds;
    private LoadImagePresenter mLoadImagePresenter;
    private ImageCache mImageCache;
    private int mLastExtendIndexInAllImageIds;
    /*
     *图片Id为键，对应于mShowingImageIdAndType中的position为值，用于加载图片完成后快速找到该图片ID对应的position来刷新mRecyclerView
     *因为会有多个线程操作该集合，所以用ConcurrentHashMap保证线程安全
     */
    private ConcurrentHashMap<Long, Integer> mImageIdAndItsPositionInShowingImageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        //获得所有本地图片ID
        mLoadImagePresenter.loadLocalImageThumbnailId();
    }

    private void initData() {
        Context context = this;
        mLoadImagePresenter = new LoadImagePresenter(this);
        mIdAndTypeOfShowingImages = new ArrayList<>();
        mImageCache = new ImageCache();
        mImageIdAndItsPositionInShowingImageList = new ConcurrentHashMap<>();
        mTelescopicItemAnimator = new TelescopicItemAnimator();
        //设置动画时间保证各动画同步执行
        mTelescopicItemAnimator.setAddDuration(ANIMATOR_INTERVAL_DEFAULT);
        mTelescopicItemAnimator.setMoveDuration(ANIMATOR_INTERVAL_DEFAULT);
        mTelescopicItemAnimator.setChangeDuration(ANIMATOR_INTERVAL_DEFAULT);
        mTelescopicItemAnimator.setRemoveDuration(ANIMATOR_INTERVAL_DEFAULT);
        //设置屏幕宽度用于Remove位移计算
        mTelescopicItemAnimator.setScreenWidth(this.getWindowManager().getDefaultDisplay().getWidth());
        mImageListAdapter = new ImageListAdapter(context, mIdAndTypeOfShowingImages, mImageCache, mImageIdAndItsPositionInShowingImageList, mRvImageList);
        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        mRvImageList.setLayoutManager(mLayoutManager);
        mRvImageList.setAdapter(mImageListAdapter);
        mRvImageList.setItemAnimator(mTelescopicItemAnimator);
        mImageListAdapter.setItemClickListener(new ImageListAdapter.ItemClickListener() {
            @Override
            public void OnItemClick(int position, ImageListAdapter.ImageListViewHolder holder) {
                //如果RecyclerView正在执行动画，不执行点击事件以防止数据混乱造成的数组越界
                if (mRecyclerViewExecutingAnimation) return;
                //以防止越界
                if (position < 0 || position > mIdAndTypeOfShowingImages.size() - 1) return;
                //点击项为子项时暂不进行操作
                if (mIdAndTypeOfShowingImages.get(position)[1] == ImageListAdapter.ITEM_TYPE_SUB_ITEM)
                    return;
                //将被点击的View及其位置传递给ItemAnimator
                mTelescopicItemAnimator.setClickedView(holder.itemView);
                //传入View的中点坐标
                mTelescopicItemAnimator.setClickedX((holder.itemView.getLeft() + holder.itemView.getRight()) / 2);
                //动画开始
                animatorStart();
                //判断该位置在mMarkList中是否有值，如果是0，则该item没有被展开，如果有值，该值为该item的子项数目
                if (mMarkItemTypeList.get(position) == 0) {
                    //寻找点击的Item在mData中的位置
                    int index = -1;
                    for (int i = 0; i <= position; i++) {
                        if (mIdAndTypeOfShowingImages.get(i)[1] == ImageListAdapter.ITEM_TYPE_ITEM) {
                            index++;
                        }
                    }
                    //防止越界
                    if (index<0||index>=mAllImageIds.size()) return;
                    //将点击Item所属mData的项从第一张图片开始导入到mDataToShow中
                    ArrayList<Long> newData = mAllImageIds.get(index);
                    //遍历所有id对应的position，假如position大于所添加位置，则该position会发生变化，变为position+newData.size()
                    for (Map.Entry<Long, Integer> entry : mImageIdAndItsPositionInShowingImageList.entrySet()) {
                        int entryPosition = entry.getValue();
                        if (entryPosition > position)
                            entry.setValue(entryPosition + newData.size());
                    }
                    for (int i = newData.size() - 1; i >= 0; i--) {
                        mIdAndTypeOfShowingImages.add(position + 1, new Long[]{newData.get(i), (long) ImageListAdapter.ITEM_TYPE_SUB_ITEM});
                        //将展开信息同步到mMarkList
                        mMarkItemTypeList.add(position + 1, 0);
                        mImageIdAndItsPositionInShowingImageList.put(newData.get(i), position + 1 + i);
                    }
                    //将position标记为被展开
                    mMarkItemTypeList.set(position, newData.size());
                    //执行该函数来触发Add动画
                    mImageListAdapter.notifyItemRangeInserted(position + 1, newData.size());
                    //该操作用于更新RecyclerView的position，因为Add和Remove后RecyclerView中item的position没有自动更新，引起错乱
                    mImageListAdapter.notifyItemRangeChanged(position + newData.size() + 1, mIdAndTypeOfShowingImages.size() - 1 - (position + newData.size()), 0);
                    //被点击项滑动至最左边
                    mLayoutManager.scrollToPositionWithOffset(position, 0);
                    //检查是否有其他的项被展开，有则记录下被展开的子项数目以及该展开项的position
                    boolean existExtendedItem = false;
                    int lastExtendedPosition = -1;
                    int lastExtendedSubItemCount = 0;
                    for (int i = 0; i < mMarkItemTypeList.size(); i++) {
                        if (i != position && mMarkItemTypeList.get(i) != 0) {
                            existExtendedItem = true;
                            lastExtendedPosition = i;
                            lastExtendedSubItemCount = mMarkItemTypeList.get(i);
                        }
                    }
                    //如果存在被展开的其他项，则在添加动画完成之后，收起子项并执行删除动画
                    if (existExtendedItem) {
                        //设置存在被展开的其他项
                        mTelescopicItemAnimator.setExistExtendedItem(true);
                        final int finalSubItemCount = lastExtendedSubItemCount;//要收起的子项数目
                        final int finalExtendedItemPosition = lastExtendedPosition;//要收起的项的位置
                        final int finalIndex = index;
                        holder.itemView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mMarkItemTypeList.set(finalExtendedItemPosition, 0);
                                for (int i = 0; i < finalSubItemCount; i++) {
                                    //删除数据
                                    mIdAndTypeOfShowingImages.remove(finalExtendedItemPosition + 1);
                                    //列表同步
                                    mMarkItemTypeList.remove(finalExtendedItemPosition + 1);
                                    //这边不删除i=0的图片，因为这张图片是封面
                                    if (i!=0){
                                        mImageIdAndItsPositionInShowingImageList.remove(mAllImageIds.get(mLastExtendIndexInAllImageIds).get(i));
                                    }else {
                                        //在添加时封面图片id对应的位置会被更新为子项的位置，子项删除后将其重新设回封面位置，即子项位置-1
                                        int position = mImageIdAndItsPositionInShowingImageList.get(mAllImageIds.get(mLastExtendIndexInAllImageIds).get(i));
                                        mImageIdAndItsPositionInShowingImageList.put(mAllImageIds.get(mLastExtendIndexInAllImageIds).get(i),position-1);
                                    }
                                }
                                //遍历所有id对应的position，假如position大于所添加位置，则该position会发生变化，变为position-newData.size()
                                for (Map.Entry<Long, Integer> entry : mImageIdAndItsPositionInShowingImageList.entrySet()) {
                                    int entryPosition = entry.getValue();
                                    if (entryPosition > finalExtendedItemPosition)
                                        entry.setValue(entryPosition - finalSubItemCount);
                                }
                                //执行该函数来触发Remove动画
                                mImageListAdapter.notifyItemRangeRemoved(finalExtendedItemPosition + 1, finalSubItemCount);
                                mImageListAdapter.notifyItemRangeChanged(finalExtendedItemPosition + finalSubItemCount + 1, mIdAndTypeOfShowingImages.size() - finalExtendedItemPosition - 1, 0);
                                //动画结束
                                animatorEnd();
                                //更新上一次被展开的list在mAllImageIds中的index
                                mLastExtendIndexInAllImageIds = finalIndex;
                            }
                        }, (long) (1.5f*ANIMATOR_INTERVAL_DEFAULT));
                    } else {
                        //动画结束
                        animatorEnd();
                        //更新上一次被展开的list在mAllImageIds中的index
                        mLastExtendIndexInAllImageIds = index;
                    }

                } else {
                    //设置不存在被展开的其他项
                    mTelescopicItemAnimator.setExistExtendedItem(false);
                    //拿到position对应的子项数目
                    int size = mMarkItemTypeList.get(position);
                    //重新设置为没有被展开
                    mMarkItemTypeList.set(position, 0);
                    //删除子项，并同步到mMarkList
                    for (int i = position + size; i > position; i--) {
                        mIdAndTypeOfShowingImages.remove(i);
                        mMarkItemTypeList.remove(i);
                        if (i!=position+1){
                            mImageIdAndItsPositionInShowingImageList.remove(mAllImageIds.get(mLastExtendIndexInAllImageIds).get(i - 1 - position));
                        }else {
                            //在添加时封面图片id对应的位置会被更新为子项的位置，子项删除后将其重新设回封面位置，即子项位置-1
                            int index = mImageIdAndItsPositionInShowingImageList.get(mAllImageIds.get(mLastExtendIndexInAllImageIds).get(i - 1 - position));
                            mImageIdAndItsPositionInShowingImageList.put(mAllImageIds.get(mLastExtendIndexInAllImageIds).get(i - 1 - position),index-1);
                        }
                    }
                    //遍历所有id对应的position，假如position大于所添加位置，则该position会发生变化，变为position-newData.size()
                    for (Map.Entry<Long, Integer> entry : mImageIdAndItsPositionInShowingImageList.entrySet()) {
                        int entryPosition = entry.getValue();
                        if (entryPosition > position)
                            entry.setValue(entryPosition - size);
                    }
                    //执行该函数来触发Remove动画
                    mImageListAdapter.notifyItemRangeRemoved(position + 1, size);
                    //该操作用于更新RecyclerView的position，因为Add和Remove后RecyclerView中item的position没有自动更新，引起错乱
                    mImageListAdapter.notifyItemRangeChanged(position + 1, mIdAndTypeOfShowingImages.size() - position - 1, 0);
                    //被点击项滑动至最左边
                    mLayoutManager.scrollToPositionWithOffset(position, 0);
                    //动画结束
                    animatorEnd();
                }
            }
        });
        mRvImageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    mImageListAdapter.recyclerViewScrollStateIsIDEL();
                }
            }
        });
    }

    private void initView() {
        mPbLoadingImageIds = findViewById(R.id.pb);
        mRvImageList = findViewById(R.id.rv_image_list);
    }

    private void animatorStart() {
        mRecyclerViewExecutingAnimation = true;
    }

    private void animatorEnd() {
        mRvImageList.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerViewExecutingAnimation = false;
            }
        }, ANIMATOR_INTERVAL_DEFAULT);
    }


    @Override
    public void showProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPbLoadingImageIds.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPbLoadingImageIds.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void imageThumbnailLoadedCallback(long imageId) {
        final Integer position = mImageIdAndItsPositionInShowingImageList.get(imageId);
        if (position != null) {
            mRvImageList.post(new Runnable() {
                @Override
                public void run() {
                    LogUtil.e("...", "加载成功，更新了Item" + position);
                    mImageListAdapter.notifyItemChanged(position);
                }
            });
        }
    }

    //图片缩略图ID加载完毕后进行数据初始化并通知RecyclerView刷新，这是一个异步回调
    @Override
    public void imageIdsLoadedCallback(@NonNull final ArrayList<ArrayList<Long>> ImageIds) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //先添加每个文件夹的第一张图片
                mAllImageIds = ImageIds;
                for (ArrayList<Long> aGroupOfId : mAllImageIds) {
                    mIdAndTypeOfShowingImages.add(new Long[]{aGroupOfId.get(0), (long) ImageListAdapter.ITEM_TYPE_ITEM});
                    mLoadImagePresenter.loadImageThumbnail(aGroupOfId.get(0), mImageCache);
                    mImageIdAndItsPositionInShowingImageList.put(aGroupOfId.get(0), mIdAndTypeOfShowingImages.size() - 1);
                    mMarkItemTypeList.add(0);
                }
            }
        });
    }

    @Override
    public boolean isReadyToRefreshView() {
        return mRvImageList.getScrollState() == SCROLL_STATE_IDLE;
    }
}
