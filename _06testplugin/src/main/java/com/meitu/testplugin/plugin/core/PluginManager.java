package com.meitu.testplugin.plugin.core;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Singleton;

import com.meitu.testplugin.BuildConfig;
import com.meitu.testplugin.plugin.core.proxy.InstrumentationProxy;
import com.meitu.testplugin.plugin.sdk.MTPluginSDK;
import com.meitu.testplugin.plugin.util.FileUtils;
import com.meitu.testplugin.plugin.util.Reflector;
import com.meitu.testplugin.plugin.util.RunUtil;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;

public class PluginManager {

    private Application mHostApplication;
    private Context mHostContext;

    private InstrumentationProxy mInstrumentation;
    private IActivityManager mActivityManager;
    private final Map<String, Plugin> mLoadedPlugins = new ConcurrentHashMap<>();

    private PluginManager() {

    }

    private static class PluginManagerHolder {
        private final static PluginManager instance = new PluginManager();
    }

    public static PluginManager getInstance() {
        return PluginManagerHolder.instance;
    }

    public void init(Context context) {
        initHostContext(context);
        hookForPlugin();
        RunUtil.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                createPlugPath();
                loadInstalledPlugins();
            }
        });
    }

    private void loadInstalledPlugins() {
        // TODO:获取已下载完成的本地插件目录下插件信息列表 2020-06-22
        Map<String, Plugin> installedPlugins = scanInstalledPlugins();
        // TODO:获取assets目录下默认的插件信息列表 2020-06-22
        Map<String, Plugin> defaultPlugins = scanDefaultPlugins();
        // TODO:将上面两个插件列表，合并插件信息列表 2020-06-22
        Map<String, Plugin> intersectPlugins = mergeAndCopyPlugins(installedPlugins, defaultPlugins);
        // TODO:加载插件 2020-06-22
        setupInstalledPlugins(intersectPlugins);
    }

    private void setupInstalledPlugins(Map<String, Plugin> pluginMap) {
        if (pluginMap == null || pluginMap.size() == 0) {
            return;
        }

        List<String> plugNames = new ArrayList<>(pluginMap.size());
        synchronized (mLoadedPlugins) {
            for (Map.Entry<String, Plugin> entry : pluginMap.entrySet()) {
                Plugin plugin = entry.getValue();
                if (plugin.setup()) {
                    String name = PluginHelper.getPluginName(plugin);
                    mLoadedPlugins.put(name, plugin);
                    plugNames.add(name);
                }
            }
        }

        for (String name : plugNames) {
            MTPluginSDK.dispatchPluginObserver(name);
        }
    }

    private Map<String, Plugin> mergeAndCopyPlugins(Map<String, Plugin> installedPlugins, Map<String, Plugin> defaultPlugins) {
        if (installedPlugins == null) {
            installedPlugins = new HashMap<>(8);
        }
        if (defaultPlugins == null) {
            defaultPlugins = new HashMap<>(8);
        }
        Iterator<Map.Entry<String, Plugin>> iterator = defaultPlugins.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Plugin> entry = iterator.next();
            if (!installedPlugins.containsKey(entry.getKey())) {
                if (PluginHelper.installDefaultPlugin(mHostContext, entry.getValue())) {
                    installedPlugins.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return installedPlugins;
    }


    private Map<String, Plugin> scanDefaultPlugins() {
        Map<String, Plugin> defaultPlugins = new HashMap<>(8);
        try {
            AssetManager am = mHostContext.getAssets();
            // TODO: PluginHelper.getDefaultPluginDir() 结果为 plugin  2020-06-22
            String[] list = am.list(PluginHelper.getDefaultPluginDir());
            if (list != null && list.length > 0) {
                for (String pluginFileName : list) {
                    String meta = PluginHelper.readPluginMeta(am, pluginFileName);
                    if (TextUtils.isEmpty(meta)) {
                        continue;
                    }
                    Plugin plugin = new Plugin(this, this.mHostContext, pluginFileName);
                    plugin.parseMeta(meta);
                    // TODO: pluginName实际就是meta文件里的包名  2020-06-22
                    defaultPlugins.put(PluginHelper.getPluginName(plugin), plugin);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private Map<String, Plugin> scanInstalledPlugins() {
        Map<String, Plugin> installedPlugins = new HashMap<>(8);
        File pluginDir = new File(PluginHelper.getPluginStorageDir());
        if (pluginDir.exists() && pluginDir.isDirectory()) {
            File[] files = pluginDir.listFiles();
            if (files == null || files.length <= 0) {
                return null;
            }

            // TODO:遍历所有的文件。无效文件删除，有效文件生成Plugin，并添加进installedPlugins 2020-06-22
            for (File file : files) {
                Plugin plugin = generateValidPlugin(file);
                if (plugin != null) {
                    // TODO: pluginName实际就是meta文件里的包名  2020-06-22
                    String plugName = PluginHelper.getPluginName(plugin);
                    if (!TextUtils.isEmpty(plugName)) {
                        installedPlugins.put(plugName, plugin);
                    }
                }
            }
        }
        return installedPlugins;
    }

    /**
     * 生产Plugin对象
     */
    private Plugin generateValidPlugin(File file) {
        if (!PluginHelper.isPluginFile(file)) {
            FileUtils.delete(file);
            return null;
        }

        String meta = PluginHelper.readPluginMeta(file.getAbsolutePath());
        if (TextUtils.isEmpty(meta)) {
            FileUtils.delete(file);
            return null;
        }

        Plugin plugin = new Plugin(this, this.mHostContext, file);
        plugin.parseMeta(meta);
        Plugin.Meta pluMeta = plugin.getPlugMeta();
        if (pluMeta == null || pluMeta.sdkVersion != BuildConfig.VERSION_CODE) {
            FileUtils.delete(file);
            return null;
        }
        return plugin;
    }

    private void createPlugPath() {
        FileUtils.mkDirs(PluginHelper.getPluginStorageDir());
    }

    private void initHostContext(Context context) {
        if (context instanceof Application) {
            this.mHostApplication = (Application) context;
            this.mHostContext = context;
        } else {
            final Context app = context.getApplicationContext();
            if (app == null) {
                this.mHostContext = context;
                // TODO: 此时app为null  by shaowenwen 2020-06-21
                this.mHostApplication = (Application) app;
            } else {
                this.mHostApplication = (Application) app;
                this.mHostContext = mHostApplication.getBaseContext();
            }
        }
    }

    private void hookForPlugin() {
        hookInstrumentationAndCallback();
        hookActivityManager();
    }

    private void hookInstrumentationAndCallback() {
        // TODO: 获取ActivityThread，获取Instrumentation，创建Instrumentation实现类  by shaowenwen 2020-06-21
        ActivityThread activityThread = ActivityThread.currentActivityThread();
        Instrumentation base = activityThread.getInstrumentation();
        InstrumentationProxy instrumentationProxy = new InstrumentationProxy(this, base);
        // TODO: 重新对ActivityThread中的mInstrumentation赋值  by shaowenwen 2020-06-21
        try {
            Reflector.with(activityThread).field("mInstrumentation").set(instrumentationProxy);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // TODO: 重新给ActivityThread中的H的mCallback赋值  by shaowenwen 2020-06-21
        try {
            Handler h = Reflector.with(activityThread).method("getHandler").call();
            Reflector.with(h).field("mCallback").set(instrumentationProxy);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void hookActivityManager() {
        try {
            Singleton<IActivityManager> singleton;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                singleton = Reflector.on(ActivityManager.class).field("IActivityManagerSingleton").get();
            } else {
                singleton = Reflector.on(ActivityManagerNative.class).field("gDefault").get();
            }
            // TODO: 获得ActivityManagerService  by shaowenwen 2020-06-21
            IActivityManager origin = singleton.get();
            IActivityManager proxy = (IActivityManager) Proxy.newProxyInstance(mHostContext.getClassLoader(),
                    new Class[]{IActivityManager.class},
                    new ActivityManagerProxy(this, origin));
            Reflector.with(singleton).field("mInstance").set(proxy);

            if (singleton.get() == proxy) {
                this.mActivityManager = proxy;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public Map<String, Plugin> getLoadedPlugins() {
        return mLoadedPlugins;
    }

    public Context getHostContext() {
        return mHostContext;
    }

    public Plugin getApkPlugin(String packageName) {
        Plugin plugin = getPlugin(packageName);
        if (plugin != null && plugin.isApk()) {
            return plugin;
        }
        return null;
    }

    public Plugin getPlugin(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        return mLoadedPlugins.get(packageName);
    }

    public Plugin getPlugin(ComponentName component) {
        if (component == null) {
            return null;
        }
        return getApkPlugin(component.getPackageName());
    }


    public InstrumentationProxy getInstrumentation() {
        return mInstrumentation;
    }

}
