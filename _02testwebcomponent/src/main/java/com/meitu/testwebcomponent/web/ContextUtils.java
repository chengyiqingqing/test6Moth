package com.meitu.testwebcomponent.web;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.view.View;

/**
 * Created by chidehang on 2019/4/7
 */
public class ContextUtils {

    /**
     * 判断用于UI上下文的activity对象是否安全
     *
     * @param activity 需要判断的activity
     * @return true: UI上下文安全的activity对象
     */
    public static boolean isSecureContextForUI(final Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                return !activity.isDestroyed();
            } catch (NoSuchMethodError e) {
                LogUtils.printStackTrace(e);
            }
        }
        return true;
    }

    public static Activity findActivity(View view) {
        return findActivity(view.getContext());
    }

    public static Activity findActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return findActivity(((ContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }

    /**
     * 检查当前上下文是否为可用的Activity
     */
    public static boolean isActivityValid(Context context) {
        if (context == null) {
            return false;
        }
        Activity activity = findActivity(context);
        if (activity != null) {
            if (activity.isFinishing()) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                return !activity.isDestroyed();

        }
        return false;
    }

}
