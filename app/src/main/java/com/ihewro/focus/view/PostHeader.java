package com.ihewro.focus.view;

import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihewro.focus.R;
import com.ihewro.focus.bean.FeedItem;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshKernel;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;

public class PostHeader extends LinearLayout implements RefreshHeader {

    private TextView mHeaderText;
    private ImageView mArrowView;
    private ImageView mProgressView;

    private FeedItem feedItem;

    public PostHeader(Context context, FeedItem feedItem) {
        super(context);
        this.feedItem = feedItem;
        initView(context);
    }

    private void initView(Context context) {
        setGravity(Gravity.CENTER);
        mHeaderText = new TextView(context);
        mArrowView = new ImageView(context);
        mProgressView = new ImageView(context);
        mProgressView.setImageResource(R.drawable.ic_loading);
        mArrowView.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
        addView(mProgressView, dp2px(20), dp2px(20));
        addView(mArrowView, dp2px(20), dp2px(20));
        addView(new View(context), dp2px(20), dp2px(20));
        addView(mHeaderText, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        setMinimumHeight(dp2px(60));
    }

    private int dp2px(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;
    }

    @Override
    public void onStartAnimator(RefreshLayout layout, int headHeight, int maxDragHeight) {
        mProgressView.animate().rotation(360).setDuration(500).start();
    }

    @Override
    public int onFinish(RefreshLayout layout, boolean success) {
        mProgressView.animate().cancel();
        mProgressView.setRotation(0);
        if (success) {
            mHeaderText.setText("刷新完成");
        } else {
            mHeaderText.setText("刷新失败");
        }
        return 500;
    }

    @Override
    public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {
        switch (newState) {
            case None:
            case PullDownToRefresh:
                mHeaderText.setText("载入完整页面");
                mArrowView.setVisibility(VISIBLE);
                mProgressView.setVisibility(GONE);
                mArrowView.animate().rotation(0);
                break;
            case Refreshing:
                mHeaderText.setText("正在载入……");
                mProgressView.setVisibility(VISIBLE);
                mArrowView.setVisibility(GONE);
                break;
            case ReleaseToRefresh:
                mHeaderText.setText("加载完整页面");
                mArrowView.animate().rotation(180);
                break;
        }
    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public void onInitialized(RefreshKernel kernel, int height, int maxDragHeight) {
    }

    @Override
    public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) {
    }

    @Override
    public void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {
    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {
    }

    @Override
    public void setPrimaryColors(@ColorInt int... colors) {
    }

    @Override
    public boolean autoOpen(int height, float maxDragHeight, boolean isRefresh) {
        return false;
    }
}
