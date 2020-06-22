package com.meitu.testwebcomponent.web.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.meitu.testwebcomponent.R;

/**
 * @author ShaoWenWen
 * @date 2019-09-19
 */
public class WebTitleBar extends LinearLayout {

    private ImageButton mImageBack;
    private ImageButton mImageClose;
    private TextView mTextTitle;
    private ProgressBar mProgressBar;

    private OnTitleBarClickListener mOnTitleBarClickListener;

    public WebTitleBar(Context context) {
        super(context);
    }

    public WebTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.linear_web_title_bar, this);
        initView();
        initListener();
    }

    private void initListener() {
        mImageBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnTitleBarClickListener != null) mOnTitleBarClickListener.onBackClick();
            }
        });
        mImageClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnTitleBarClickListener != null) mOnTitleBarClickListener.onCloseClick();
            }
        });
    }

    private void initView() {
        mImageBack = findViewById(R.id.image_back_h5);
        mImageClose = findViewById(R.id.image_close_h5);
        mTextTitle = findViewById(R.id.text_title);
        mProgressBar = findViewById(R.id.pb_web);
    }

    public void setTitle(String textTitle) {
        mTextTitle.setText(textTitle);
    }

    public void setCloseButtonStatus(boolean visible) {
        mImageClose.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setOnTitleBarClickListener(OnTitleBarClickListener onTitleBarClickListener) {
        this.mOnTitleBarClickListener = onTitleBarClickListener;
    }

    public void hideProgressBar(boolean anim) {
        if (mProgressBar == null || mProgressBar.getVisibility() != View.VISIBLE) {
            return;
        }
        if (anim) {
            AlphaAnimation animation = new AlphaAnimation(1, 0);
            animation.setDuration(1000);
            mProgressBar.startAnimation(animation);
            mProgressBar.setVisibility(View.GONE);
        } else {
            mProgressBar.setProgress(0);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void showProgressBar(int progress) {
        if (mProgressBar != null) {
            if (mProgressBar.getVisibility() != View.VISIBLE) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mProgressBar.setProgress(progress);
        }
    }

    public interface OnTitleBarClickListener {
        /**
         * H5页面，点击titlebar的回退监听
         */
        void onBackClick();

        /**
         * 关闭监听
         */
        void onCloseClick();
    }

}
