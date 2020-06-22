package com.meitu.testwebcomponent.web.view;

import android.os.Bundle;

import com.meitu.testwebcomponent.R;
import com.meitu.testwebcomponent.web.ArgumentKey;

/**
 * 浏览器Activity：用于打开H5页面
 * @author ShaoWenWen
 * @date 2019-09-19
 */
public class WebViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.openad_activity_web_view);
        initStatusBarStyle(false);
        findViewById(R.id.frame_root).setPadding(0, getStatusBarHeight(), 0, 0);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_root, WebViewFragment.getInstance(getIntent().getStringExtra(ArgumentKey.WEB_URL)), WebViewFragment.TAG)
                .commitAllowingStateLoss();
    }

}
