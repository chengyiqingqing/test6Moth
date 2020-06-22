package com.meitu.testwebcomponent.web.view;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import com.meitu.testwebcomponent.R;

/**
 * 通用浏览器父类
 * @author ShaoWenWen
 * @date 2019-09-19
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * 设置状态栏
     */
    protected void initStatusBarStyle(boolean fullScreen) {
        if(fullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ActionBar bar = getActionBar();
            if(getActionBar() != null) {
                bar.hide();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0+
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(Color.WHITE);
            } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                addDefaultStatusBarBackground();
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
    }

    private void addDefaultStatusBarBackground() {
        View view = new View(this);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight()));
        view.setBackgroundColor(getResources().getColor(R.color.web_status_bar_bg));
        ((ViewGroup) getWindow().getDecorView()).addView(view);
    }

    /**
     * 获取状态栏高度
     */
    protected int getStatusBarHeight() {
        int statusBarHeight = 0;
        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        if(statusBarHeight <= 0) {
            try {
                Class<?> clazz = Class.forName("com.android.internal.R$dimen");
                Object object = clazz.newInstance();
                int height = Integer.parseInt(clazz.getField("status_bar_height")
                        .get(object).toString());
                statusBarHeight = getResources().getDimensionPixelSize(height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(statusBarHeight <= 0) {
            statusBarHeight = getResources().getDimensionPixelSize(R.dimen.web_status_bar_height);
        }
        return statusBarHeight;
    }

}
