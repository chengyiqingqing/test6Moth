package com.meitu.testplugin.plugin.core;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Singleton;

import com.meitu.testplugin.plugin.core.proxy.InstrumentationProxy;
import com.meitu.testplugin.plugin.util.Reflector;
import com.meitu.testplugin.plugin.util.RunUtil;

import java.lang.reflect.Proxy;
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
            }
        });
    }

    private void createPlugPath() {

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


}
