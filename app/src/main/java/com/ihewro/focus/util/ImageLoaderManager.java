package com.ihewro.focus.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.ihewro.focus.R;
import com.ihewro.focus.callback.ImageLoaderCallback;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.ImageViewerPopupView;
import com.lxj.xpopup.interfaces.XPopupImageLoader;

import java.io.File;

/**
 * Glide 图片加载管理器
 * 替代 UniversalImageLoader
 */
public class ImageLoaderManager {

    /**
     * 初始化 Glide（可选自定义配置）
     */
    public static void init(Context context) {
        // Glide 4.x 已经自动初始化，这里可以添加自定义配置
    }

    /**
     * 获取订阅图标加载选项（已废弃，Glide 不需要预定义选项）
     * 保留此方法以兼容现有代码
     */
    public static Object getSubsciptionIconOptions(Context context) {
        // Glide 使用链式调用，不需要预定义 options
        return null;
    }

    /**
     * 加载图片到 ImageView（带回调）
     */
    public static void loadImageUrlToImageView(String imageUrl, final ImageView imageView, final ImageLoaderCallback callback) {

        Glide.with(imageView.getContext())
                .asBitmap()
                .load(imageUrl)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.loading_error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (callback != null) {
                            callback.onFailed(imageView, e);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (callback != null) {
                            callback.onSuccess(imageView, resource);
                        }
                        return false;
                    }
                })
                .preload();
    }

    /**
     * 加载订阅图标（简化版）
     */
    public static void loadFeedIcon(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.loading_error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * 显示单张图片对话框
     * TODO: 需要更新 XPopup 2.9.0 的 API 调用
     */
    public static void showSingleImageDialog(final Context context, final String imageUrl, View srcView) {
        // 暂时简化实现，直接使用 Toast 提示
        android.widget.Toast.makeText(context, "查看图片: " + imageUrl, android.widget.Toast.LENGTH_SHORT).show();
    }
}

/**
 * Glide 图片加载工具类
 */
class GlideImageLoaderManager {
    /**
     * 加载订阅图标
     */
    public static void loadFeedIcon(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.loading_error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }
}
