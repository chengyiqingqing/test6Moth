package com.meitu.testwebcomponent.web.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * @author ShaoWenWen
 * @date 2019-09-19
 */
public class WebViewComponent extends WebView {

    public WebViewComponent(Context context) {
        this(context, null);
    }

    public WebViewComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWebSetting();
        initListener();
    }

    private void initListener() {
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (KeyEvent.ACTION_DOWN == keyEvent.getAction() && canGoBack()) {
                    goBack();
                    return true;
                }
                return false;
            }
        });
    }

    private void initWebSetting() {
        // （1）普通设置
        WebSettings webSettings = getSettings();
        // 支持缩放，默认为true。是下面那个的前提
        webSettings.setSupportZoom(true);
        // 缩放至屏幕的大小
        webSettings.setLoadWithOverviewMode(true);
        // 将图片调整到适合webview的大小
        webSettings.setUseWideViewPort(true);
        // 设置编码格式
        webSettings.setDefaultTextEncodingName("utf-8");
        // 支持自动加载图片
        webSettings.setLoadsImagesAutomatically(true);

        // （2）调用JS方法.安卓版本大于17,加上注解 @JavascriptInterface
        webSettings.setJavaScriptEnabled(true);

        //（3）有时候网页需要自己保存一些关键数据,Android WebView 需要自己设置
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAppCacheEnabled(true);
        String appCachePath = getContext().getApplicationContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);

        // （4）html中的_bank标签就是新建窗口打开，有时会打不开，需要加以下
        //  （5）然后 复写 WebChromeClient的onCreateWindow方法
        webSettings.setSupportMultipleWindows(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    }

}
