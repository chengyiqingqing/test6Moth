package com.meitu.testwebcomponent.web.callback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.DownloadListener;

import com.meitu.testwebcomponent.web.ContextUtils;
import com.meitu.testwebcomponent.web.LogUtils;

/**
 * 自定义下载监听器
 * @author ShaoWenWen
 * @date 2019-09-19
 */
public class CustomWebDownloadListener implements DownloadListener {

    private static final String TAG = "CustomWebDownloadListen";
    private Context mcontext;

    public CustomWebDownloadListener(Activity activity) {
        mcontext = activity;
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        if (LogUtils.isEnabled) LogUtils.e(TAG, "onDownloadStart: " + url);
        if (ContextUtils.isActivityValid(mcontext)) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            mcontext.startActivity(intent);
        }
    }

}
