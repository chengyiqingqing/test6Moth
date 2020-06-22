package com.meitu.testplugin.plugin.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Build;

/**
 * Created by chidehang on 2019-11-22
 */
public class ThemeCompat {

    public static final int INVALID_THEME_ID = 0;

    public static int getTheme(Context context, Intent intent) {
        return getTheme(context, ComponentResolver.createPluginComponent(intent));
    }

    public static int getTheme(Context context, ComponentName component) {
        if (component == null) {
            return INVALID_THEME_ID;
        }

        Plugin plugin = PluginManager.getInstance().getApkPlugin(component.getPackageName());
        if (plugin == null) {
            return INVALID_THEME_ID;
        }

        ActivityInfo info = plugin.getActivityInfo(component);
        if (info == null) {
            return INVALID_THEME_ID;
        }

        if (info.theme != INVALID_THEME_ID) {
            return info.theme;
        }

        ApplicationInfo appInfo = info.applicationInfo;
        if (appInfo != null && appInfo.theme != INVALID_THEME_ID) {
            return appInfo.theme;
        }

        return selectDefaultTheme(INVALID_THEME_ID, Build.VERSION.SDK_INT);
    }

    public static int selectDefaultTheme(int curTheme, int targetVersion) {
        return selectSystemTheme(curTheme, targetVersion,
                android.R.style.Theme,
                android.R.style.Theme_Holo,
                android.R.style.Theme_DeviceDefault,
                android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
    }

    public static int selectSystemTheme(int curTheme, int targetVersion, int orig, int holo, int dark, int device) {
        if (curTheme != INVALID_THEME_ID) {
            return curTheme;
        }
        if (targetVersion < 11) {
            return orig;
        }
        if (targetVersion < 14) {
            return holo;
        }
        if (targetVersion < 24) {
            return dark;
        }
        return device;
    }
}
