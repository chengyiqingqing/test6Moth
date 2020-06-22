package com.meitu.testplugin.plugin.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.meitu.testplugin.plugin.constant.PluginConstant;
import com.meitu.testplugin.plugin.util.FileUtils;

import java.io.File;

/**
 * Created by chidehang on 2019-11-14
 */
public class PluginHelper {

    /** 插件下载目录 */
    private static String pluginStorageDir;
    private static String dexOptDir;
    private static String nativeLibDir;
    /** 默认插件目录 */
    private static final String defaultPluginDir = "plugin";

    public static String getPluginStorageDir() {
        if (TextUtils.isEmpty(pluginStorageDir)) {
            // 暂时存放在外部目录
//            pluginStorageDir = Environment.getExternalStorageDirectory() + "/mtplugin/";
            pluginStorageDir = PluginManager.getInstance().getHostContext().getFilesDir() + "/mtplugin/";
        }
        return pluginStorageDir;
    }

    public static String getPluginStorageFilePath(Plugin plugin) {
        return getPluginStorageDir() + plugin.mFileName;
    }

    public static String getDexOptDir(Context context) {
        if (TextUtils.isEmpty(dexOptDir)) {
            dexOptDir = context.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath();
        }
        return dexOptDir;
    }

    public static String getNativeLibDir(Context context) {
        if (TextUtils.isEmpty(nativeLibDir)) {
            nativeLibDir = context.getDir("libs", Context.MODE_PRIVATE).getAbsolutePath();
        }
        return nativeLibDir;
    }

    public static String getDefaultPluginDir() {
        return defaultPluginDir;
    }

    public static boolean isPluginFile(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return false;
        }
        String name = file.getName();
        return name.endsWith(PluginConstant.PLUGIN_SUFFIX_APK) ||
                name.endsWith(PluginConstant.PLUGIN_SUFFIX_JAR) ||
                name.endsWith(PluginConstant.PLUGIN_SUFFIX_DEX);
    }

    public static String readPluginMeta(String path) {
        return FileUtils.readFileFromZip(path, PluginConstant.PLUGMETA_FILENAME);
    }

    public static String readPluginMeta(AssetManager assetManager, String path) {
        return FileUtils.readFileFromZip(assetManager,
                PluginHelper.getDefaultPluginDir() + "/" + path,
                PluginConstant.PLUGMETA_FILENAME);
    }

    public static String getPluginName(Plugin plugin) {
        return plugin.getPlugMeta() == null? "" : plugin.getPlugMeta().packageName;
    }

    public static boolean installDefaultPlugin(Context context, Plugin plugin) {
        try {
            File destFile = new File(getPluginStorageFilePath(plugin));
            String assetsPath = getDefaultPluginDir() + "/" + plugin.mFileName;
            FileUtils.copyFileFromAssets(context, assetsPath, destFile);
            plugin.setFileAbsolutePath(destFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deletePluginFile(Plugin plugin) {
        try {
            File f = new File(plugin.getFileAbsolutePath());
            FileUtils.delete(f);
        } catch (Throwable t) {
            LogUtils.printStackTrace(t);
        }
    }

    public static boolean isApkFile(String path) {
        return !TextUtils.isEmpty(path) && path.endsWith(PluginConstant.PLUGIN_SUFFIX_APK);
    }
}
