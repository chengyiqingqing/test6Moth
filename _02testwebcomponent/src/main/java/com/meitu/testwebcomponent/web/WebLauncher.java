package com.meitu.testwebcomponent.web;

import android.app.Activity;
import android.content.Intent;

import com.meitu.testwebcomponent.BuildConfig;
import com.meitu.testwebcomponent.web.view.WebViewActivity;

/**
 * 联盟定制浏览器，打开入口
 * @author ShaoWenWen
 * @date 2019-09-19
 */
public class WebLauncher {

    private static final String TAG = "WebLauncher";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * 联盟定制浏览器，打开入口
     */
    public static void launchWeb(Activity activity, String url) {
        LogUtils.d(TAG, "" + activity + " --- " + url);
        if (activity == null || url == null) return;
        Intent intent = new Intent(activity, WebViewActivity.class);
        intent.putExtra(ArgumentKey.WEB_URL,url);
        activity.startActivity(intent);
    }

}
