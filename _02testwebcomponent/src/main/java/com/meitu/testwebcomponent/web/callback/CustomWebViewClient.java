package com.meitu.testwebcomponent.web.callback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.meitu.testwebcomponent.web.LogUtils;
import com.meitu.testwebcomponent.web.view.WebTitleBar;


/**
 * @author ShaoWenWen
 * @date 2019-09-19
 */
public class CustomWebViewClient extends WebViewClient {

    private static final String TAG = "CustomWebViewClient";
    private WebTitleBar mWebTitleBar;
    private View mErrorView;

    public CustomWebViewClient(WebTitleBar mWebTitleBar, View errorView) {
        this.mWebTitleBar = mWebTitleBar;
        this.mErrorView = errorView;
    }

    /**
     * 多页面在同一个WebView中打开，就是不新建activity或者调用系统浏览器打开
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            if (URLUtil.isValidUrl(url)) {
                Log.e(TAG, "shouldOverrideUrlLoading: 自己加的 --> " + url);
                view.loadUrl(url);
            } else {
                Log.e(TAG, "shouldOverrideUrlLoading: --> " + url);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                if (view.getContext() instanceof Activity && null != intent.resolveActivity(view.getContext().getApplicationContext().getPackageManager())) {
                    view.getContext().startActivity(intent);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        mWebTitleBar.showProgressBar(0);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        mWebTitleBar.hideProgressBar(true);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        mWebTitleBar.hideProgressBar(true);
        LogUtils.e(TAG,
                "onReceivedError");
        if (null != view) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final WebView view1 = view;
                // 网页无法打开，没有匹配到host时，不会回调http error
                WebSettings settings = view1.getSettings();
                settings.setJavaScriptEnabled(true);
                view1.evaluateJavascript("javascript:document.body.innerHTML", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        LogUtils.e(TAG,
                                "onReceivedErro,value:" + value);
                        // 网页无法打开时，js获取到的值
                        if (TextUtils.isEmpty(value) || value.equalsIgnoreCase("null") || value.equalsIgnoreCase("\"\"")
                                || value.contains("\\u003Ch2>网页无法打开\\u003C/h2>\\n")) {
                            showRefreshView(view1, true);
                        }
                    }
                });
            }
        }

    }


    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        LogUtils.e(TAG,
                "onReceivedHttpError");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LogUtils.e(TAG,
                    "onReceivedHttpError.statusCode:" + errorResponse.getStatusCode());
            if (null != errorResponse && errorResponse.getStatusCode() >= 400 && errorResponse.getStatusCode() <= 599 &&
                    null != request && request.isForMainFrame()) {
                showRefreshView(view, true);
            }
        } else {
            showRefreshView(view, true);
        }
    }

    /**
     * @param view webview
     * @param shouldLoadBlank 是否手动打开空白页，避免有404类似的错误页
     */
    private void showRefreshView(WebView view, boolean shouldLoadBlank) {
        if (null != mErrorView) {
            mErrorView.setVisibility(View.VISIBLE);
        }
        if (shouldLoadBlank && null != view) {
            view.loadUrl("about:blank");
        }
    }

}
