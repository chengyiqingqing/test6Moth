package com.meitu.testwebcomponent.web.callback;

import android.os.Message;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.meitu.testwebcomponent.web.view.WebTitleBar;


/**
 * @author ShaoWenWen
 * @date 2019-09-19
 */
public class CustomWebChromeClient extends WebChromeClient {

    private static final String TAG = "CustomWebChromeClient";
    private WebTitleBar mWebTitleBar;

    public CustomWebChromeClient(WebTitleBar mWebTitleBar) {
        this.mWebTitleBar = mWebTitleBar;
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        Log.e(TAG, "onReceivedTitle: " + title);
        mWebTitleBar.setTitle(title);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (newProgress == 100) mWebTitleBar.hideProgressBar(true);
        else mWebTitleBar.showProgressBar(newProgress);
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(view);
        resultMsg.sendToTarget();
        return true;
    }

}
