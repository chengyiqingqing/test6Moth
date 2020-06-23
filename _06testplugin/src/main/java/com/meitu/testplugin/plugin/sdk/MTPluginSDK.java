package com.meitu.testplugin.plugin.sdk;

import android.content.Context;
import android.util.Log;

import com.meitu.testplugin.plugin.core.PluginManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MTPluginSDK {

    private static final String TAG = "MTPluginSDK";
    private static AtomicBoolean sInit = new AtomicBoolean(false);
    /** 监听插件加载 */
    private static final List<PluginObserver> sPluginObserverList = new LinkedList<>();

    public static void init(Context context, String appId, String channel) {
        if (context == null) {
            return;
        }
        if (sInit.compareAndSet(false, true)) {
            PluginManager.getInstance().init(context);
        }
    }

    /**
     * 添加插件加载监听
     */
    public static void addPluginObserver(PluginObserver observer) {
        synchronized (sPluginObserverList) {
            sPluginObserverList.add(observer);
        }
    }

    /**
     * 移除插件加载监听
     */
    public static void removePluginObserver(PluginObserver observer) {
        synchronized (sPluginObserverList) {
            sPluginObserverList.remove(observer);
        }
    }

    /** 通知观察者插件加载 */
    public static void dispatchPluginObserver(String plugName) {
        Log.d(TAG, "new plugin loaded ==>" + plugName);
        synchronized (sPluginObserverList) {
            for (PluginObserver observer : sPluginObserverList) {
                observer.onPluginLoaded(plugName);
            }
        }
    }

    public interface PluginObserver {
        void onPluginLoaded(String plugName);
    }

}
