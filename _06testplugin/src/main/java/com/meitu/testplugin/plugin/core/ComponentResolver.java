package com.meitu.testplugin.plugin.core;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.meitu.testplugin.plugin.constant.PluginConstant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chidehang on 2019-11-22
 */
public class ComponentResolver {

    public static Intent preparePluginIntent(PluginManager pluginManager, Intent intent) {
        ComponentName component = intent.getComponent();
        if (component == null || component.getPackageName().equals(pluginManager.getHostContext().getPackageName())) {
            for (Plugin p : pluginManager.getLoadedPlugins().values()) {
                if (!p.isApk()) {
                    continue;
                }
                // 遍历插件查找匹配intent的activity
                ResolveInfo info = p.resolveActivity(intent, 0);
                if (info != null && info.activityInfo != null) {
                    // 查找到对应的插件activity信息
                    transformIntent(pluginManager, intent, info.activityInfo);
                    break;
                }
            }
        }
        return intent;
    }

    private static void transformIntent(PluginManager pluginManager, Intent intent, ActivityInfo activityInfo) {
        intent.putExtra(PluginConstant.EXTRA_IS_PLUGIN, true);
        intent.putExtra(PluginConstant.EXTRA_TARGET_PACKAGE, activityInfo.packageName);
        intent.putExtra(PluginConstant.EXTRA_TARGET_ACTIVITY, activityInfo.name);
        // 修改跳转activity为占位activity
        intent.setClassName(pluginManager.getHostContext(), PluginConstant.STUB_ACTIVITY_CLASS_NAME);
    }

    /**
     * 是否跳转插件意图
     */
    public static boolean isPluginIntent(Intent intent) {
        return intent != null && intent.getBooleanExtra(PluginConstant.EXTRA_IS_PLUGIN, false);
    }

    public static ComponentName createPluginComponent(Intent intent) {
        if (intent == null) {
            return null;
        }
        if (isPluginIntent(intent)) {
            return new ComponentName(intent.getStringExtra(PluginConstant.EXTRA_TARGET_PACKAGE),
                    intent.getStringExtra(PluginConstant.EXTRA_TARGET_ACTIVITY));
        }
        return intent.getComponent();
    }

    public static ResolveInfo resolveActivity(PluginManager pluginManager, Intent intent, int flags) {
        for (Plugin plugin : pluginManager.getLoadedPlugins().values()) {
            if (!plugin.isApk()) {
                continue;
            }
            ResolveInfo resolveInfo = plugin.resolveActivity(intent, flags);
            if (null != resolveInfo) {
                return resolveInfo;
            }
        }
        return null;
    }

    /**
     * 查找匹配的Activities
     */
    public static List<ResolveInfo> queryIntentActivities(PackageManager hostPm, PluginManager pluginManager, Intent intent, int flags) {
        ComponentName component = intent.getComponent();
        if (null == component) {
            if (intent.getSelector() != null) {
                intent = intent.getSelector();
                component = intent.getComponent();
            }
        }

        if (null != component) {
            Plugin plugin = pluginManager.getApkPlugin(component.getPackageName());
            if (null != plugin) {
                ActivityInfo activityInfo = plugin.getActivityInfo(component);
                if (activityInfo != null) {
                    ResolveInfo resolveInfo = new ResolveInfo();
                    resolveInfo.activityInfo = activityInfo;
                    return Arrays.asList(resolveInfo);
                }
            }
        }

        List<ResolveInfo> all = new ArrayList<ResolveInfo>();

        List<ResolveInfo> pluginResolveInfos = ComponentResolver.queryIntentActivities(pluginManager, intent, flags);
        if (null != pluginResolveInfos && pluginResolveInfos.size() > 0) {
            all.addAll(pluginResolveInfos);
        }

        List<ResolveInfo> hostResolveInfos = hostPm.queryIntentActivities(intent, flags);
        if (null != hostResolveInfos && hostResolveInfos.size() > 0) {
            all.addAll(hostResolveInfos);
        }

        return all;
    }

    private static List<ResolveInfo> queryIntentActivities(PluginManager pluginManager, Intent intent, int flags) {
        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
        for (Plugin plugin : pluginManager.getLoadedPlugins().values()) {
            if (!plugin.isApk()) {
                continue;
            }
            List<ResolveInfo> result = plugin.queryIntentActivities(intent, flags);
            if (null != result && result.size() > 0) {
                resolveInfos.addAll(result);
            }
        }
        return resolveInfos;
    }

    /**
     * 查找匹配的广播ResolveInfo
     */
    public static List<ResolveInfo> queryBroadcastReceivers(PackageManager hostPm, PluginManager pluginManager, Intent intent, int flags) {
        ComponentName component = intent.getComponent();
        if (null == component) {
            if (intent.getSelector() != null) {
                intent = intent.getSelector();
                component = intent.getComponent();
            }
        }

        if (null != component) {
            Plugin plugin = pluginManager.getPlugin(component);
            if (null != plugin) {
                ActivityInfo activityInfo = plugin.getReceiverInfo(component);
                if (activityInfo != null) {
                    ResolveInfo resolveInfo = new ResolveInfo();
                    resolveInfo.activityInfo = activityInfo;
                    return Arrays.asList(resolveInfo);
                }
            }
        }

        List<ResolveInfo> all = new ArrayList<>();

        List<ResolveInfo> pluginResolveInfos = ComponentResolver.queryBroadcastReceivers(pluginManager, intent, flags);
        if (null != pluginResolveInfos && pluginResolveInfos.size() > 0) {
            all.addAll(pluginResolveInfos);
        }

        List<ResolveInfo> hostResolveInfos = hostPm.queryBroadcastReceivers(intent, flags);
        if (null != hostResolveInfos && hostResolveInfos.size() > 0) {
            all.addAll(hostResolveInfos);
        }

        return all;
    }

    private static List<ResolveInfo> queryBroadcastReceivers(PluginManager pluginManager, Intent intent, int flags) {
        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();

        for (Plugin plugin : pluginManager.getLoadedPlugins().values()) {
            if (!plugin.isApk()) {
                continue;
            }
            List<ResolveInfo> result = plugin.queryBroadcastReceivers(intent, flags);
            if (null != result && result.size() > 0) {
                resolveInfos.addAll(result);
            }
        }

        return resolveInfos;
    }

    /**
     * 查找最匹配的Service
     */
    public static ResolveInfo resolveService(PackageManager hostPm, PluginManager pluginManager, Intent intent, int flags) {
        ResolveInfo resolveInfo = ComponentResolver.resolveService(pluginManager, intent, flags);
        if (null != resolveInfo) {
            return resolveInfo;
        }

        return hostPm.resolveService(intent, flags);
    }


}
