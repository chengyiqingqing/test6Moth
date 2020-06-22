package com.meitu.testwebcomponent.web.view;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.meitu.testwebcomponent.R;
import com.meitu.testwebcomponent.web.callback.CustomWebChromeClient;
import com.meitu.testwebcomponent.web.callback.CustomWebDownloadListener;
import com.meitu.testwebcomponent.web.callback.CustomWebViewClient;


/**
 * WebView容器：包含浏览器标题栏和浏览器内容
 * @author ShaoWenWen
 * @date 2019-09-18
 */
public class WebViewFragment extends Fragment {

    public static final String TAG = "WebViewFragment";
    private static final String URL = "URL";

    private View mRoot;
    /* 浏览器的标题栏组件 */
    private WebTitleBar mWebTitleBar;
    /* 浏览器组件，继承WebView */
    private WebViewComponent mWebView;
    /**
     * 浏览器打开失败，错误页面
     */
    private View mErrorView;

    public static WebViewFragment getInstance(String url) {
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(URL, url);
        webViewFragment.setArguments(bundle);
        return webViewFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRoot != null) {
            ViewGroup viewParent = (ViewGroup) mRoot.getParent();
            if (viewParent != null) viewParent.removeView(mRoot);
            return mRoot;
        }
        mRoot = inflater.inflate(R.layout.openad_fragment_webview, container, false);
        initView();
        initListener();
        setWebClient();
        return mRoot;
    }

    private void initView() {
        mWebTitleBar = mRoot.findViewById(R.id.web_title_bar);
        mWebTitleBar.setCloseButtonStatus(true);
        mWebView = mRoot.findViewById(R.id.web_view);
        mErrorView = mRoot.findViewById(R.id.web_error_page);
    }

    private void initListener() {
        mWebTitleBar.setOnTitleBarClickListener(new WebTitleBar.OnTitleBarClickListener() {
            @Override
            public void onBackClick() {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
            }

            @Override
            public void onCloseClick() {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mErrorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mErrorView.setVisibility(View.GONE);
                mWebView.loadUrl(getArguments().getString(URL));
            }
        });
    }

    private void setWebClient() {
        mWebView.setWebViewClient(new CustomWebViewClient(mWebTitleBar, mErrorView));
        mWebView.setWebChromeClient(new CustomWebChromeClient(mWebTitleBar));
        mWebView.setDownloadListener(new CustomWebDownloadListener(getActivity()));
        mWebView.loadUrl(getArguments().getString(URL));
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
        mWebView.resumeTimers();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
        mWebView.pauseTimers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.clearHistory();
            if (mWebView.getParent() != null) {
                ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            }
            mWebView.loadUrl("about:blank");
            mWebView.stopLoading();
            mWebView.setWebViewClient(null);
            mWebView.destroy();
            mWebView = null;
        }
    }

}
