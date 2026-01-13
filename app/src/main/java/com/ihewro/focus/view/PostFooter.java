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
import com.scwang.smart.refresh.layout.api.RefreshFooter;
import com.scwang.smart.refresh.layout.api.RefreshKernel;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;

public class PostFooter extends LinearLayout implements RefreshFooter {

    private TextView mHeaderText;
    private ImageView mArrowView;
    private ImageView mProgressView;

    private FeedItem feedItem;

    public PostFooter(Context context, FeedItem feedItem) {
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
        addView(mHeaderText, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setMinimumHeight(dp2px(60));
    }

    private int dp2px(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public boolean setNoMoreData(boolean noMoreData) {
        return false;
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @NonNull
    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;
    }

    @Override
    public void setPrimaryColors(int... colors) {
    }

    @Override
    public void onInitialized(@NonNull RefreshKernel kernel, int height, int maxDragHeight) {
    }

    @Override
    public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) {
    }

    @Override
    public void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {
    }

    @Override
    public void onStartAnimator(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {
        mProgressView.animate().rotation(360).setDuration(500).start();
    }

    @Override
    public int onFinish(@NonNull RefreshLayout refreshLayout, boolean success) {
        mProgressView.animate().cancel();
        mProgressView.setRotation(0);
        return 0;
    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {
    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {
        switch (newState) {
            case None:
            case PullUpToLoad:
                mHeaderText.setText("访问源网站");
                mArrowView.setVisibility(VISIBLE);
                mProgressView.setVisibility(GONE);
                mArrowView.animate().rotation(0);
                break;
            case Loading:
            case LoadReleased:
                mHeaderText.setText("访问源网站");
                mArrowView.animate().rotation(180);
                break;
            case Refreshing:
                mHeaderText.setText("正在载入……");
                mProgressView.setVisibility(VISIBLE);
                mArrowView.setVisibility(VISIBLE);
                break;
        }
    }

    @Override
    public boolean autoOpen(int height, float maxDragHeight, boolean isRefresh) {
        return false;
    }
}
