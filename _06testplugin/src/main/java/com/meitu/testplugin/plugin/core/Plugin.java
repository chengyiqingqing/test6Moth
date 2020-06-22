package com.meitu.testplugin.plugin.core;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author shaowenwen
 * @Date 2020-06-21 17:55
 */
public class Plugin {

    public static final String TAG = "Plugin";
    private Meta mPluginMata;
    private boolean isApk;
    public String mFileName;

    private Map<ComponentName, ActivityInfo> mActivityInfos;

    public boolean isApk() {
        return isApk;
    }

    public ActivityInfo getActivityInfo(ComponentName component) {
        return mActivityInfos.get(component);
    }

    public ResolveInfo resolveActivity(Intent intent, int flag) {
        List<ResolveInfo> query = queryIntentActivities(intent, flag);
        if (query == null || query.size() == 0) {
            return null;
        }
        return query.get(0);
    }

    private List<ResolveInfo> queryIntentActivities(Intent intent, int flag) {
        ComponentName componentName = intent.getComponent();
        ArrayList<ResolveInfo> resolveInfos = new ArrayList<>();

        return null;
    }

    public class Meta {
        public String aliasName;
        public String packageName;
        public int plugVersion;
        public int sdkVersion;
        public String executorFactory;
    }

}
