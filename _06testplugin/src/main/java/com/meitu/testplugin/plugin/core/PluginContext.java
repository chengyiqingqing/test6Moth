package com.meitu.testplugin.plugin.core;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * hook插件context，减少插件开发侵入性
 * Created by chidehang on 2019-11-21
 */
public class PluginContext extends ContextWrapper {

    private final Plugin mPlugin;

    public PluginContext(Plugin plugin) {
        super(plugin.getPluginManager().getHostContext());
        this.mPlugin = plugin;
    }

    public PluginContext(Plugin plugin, Context base) {
        super(base);
        this.mPlugin = plugin;
    }

    private Context getHostContext() {
        return getBaseContext();
    }

    @Override
    public Context getApplicationContext() {
        return mPlugin.getApplication();
    }

    @Override
    public ClassLoader getClassLoader() {
        return mPlugin.getClassLoader();
    }

    @Override
    public PackageManager getPackageManager() {
        return mPlugin.getPackageManager();
    }

    @Override
    public Object getSystemService(String name) {
        if (name.equals(Context.CLIPBOARD_SERVICE) || name.equals(Context.NOTIFICATION_SERVICE)) {
            return getHostContext().getSystemService(name);
        }
        return super.getSystemService(name);
    }

    @Override
    public Resources getResources() {
        return mPlugin.getResources();
    }

    @Override
    public AssetManager getAssets() {
        return mPlugin.getAssets();
    }

    @Override
    public Resources.Theme getTheme() {
        return mPlugin.getTheme();
    }

    @Override
    public void startActivity(Intent intent) {
        ComponentResolver.preparePluginIntent(mPlugin.getPluginManager(), intent);
        super.startActivity(intent);
    }

}
