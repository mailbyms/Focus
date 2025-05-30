package com.ihewro.focus.util;

import android.content.Context;
import android.webkit.WebView;

import com.blankj.ALog;
import com.ihewro.focus.view.ImageManagePopupView;
import com.lxj.xpopup.XPopup;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/05/11
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class MJavascriptInterface {
    private Context activity;
    private String[] imageUrls;
    private WebView webView;

    public MJavascriptInterface(Context context, String[] imageUrls, WebView webView) {
        this.activity = context;
        this.imageUrls = imageUrls;
        this.webView = webView;
    }

    @android.webkit.JavascriptInterface
    public void openImage(String img) {
        ALog.d("点击了图片" +img);
        ImageLoaderManager.showSingleImageDialog(activity,img,null);
    }

    @android.webkit.JavascriptInterface
    public void longClickImage(String img) {
        ALog.d("长按图片" +img);
        //显示下拉底部弹窗
        new XPopup.Builder(activity)
                .asCustom(new ImageManagePopupView(activity,img,null))
                .show();
    }
}