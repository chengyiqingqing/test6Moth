package com.meitu.testplugin.plugin.core;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import com.meitu.testplugin.plugin.constant.PluginConstant;

/**
 * Created by chidehang on 2019-11-22
 */
public class ComponentResolver {

    public static Intent preparePluginIntent(PluginManager pluginManager, Intent intent) {
        ComponentName componentName = intent.getComponent();
        if (componentName == null || componentName.getPackageName().equals(pluginManager.getHostContext().getPackageName())) {
            for (Plugin plugin : pluginManager.getLoadedPlugins().values()) {
                if (!plugin.isApk()) {
                    continue;
                }
                // TODO: 遍历插件，查找匹配intent的activity  by shaowenwen 2020-06-22
                ResolveInfo info = plugin.resolveActivity(intent,0);
            }
        }
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


}
