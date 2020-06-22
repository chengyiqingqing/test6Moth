package com.meitu.testplugin.plugin.core;

import android.app.IActivityManager;
import android.content.Intent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Author shaowenwen
 * @Date 2020-06-21 19:54
 */
public class ActivityManagerProxy implements InvocationHandler {

    public static final int INTENT_SENDER_ACTIVITY = 2;

    private PluginManager mPluginManager;
    private IActivityManager mBaseAm;

    public ActivityManagerProxy(PluginManager pluginManager, IActivityManager activityManager) {
        this.mPluginManager = pluginManager;
        this.mBaseAm = activityManager;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if ("getIntentSender".equals(method.getName())) {
            getIntentSender(method, objects);
        }
        return null;
    }

    protected void getIntentSender(Method method, Object[] args) {
        String hostPackageName = mPluginManager.getHostContext().getPackageName();
        args[1] = hostPackageName;

        // TODO: 为什么会这样得到target  by shaowenwen 2020-06-21
        Intent target = ((Intent[]) args[5])[0];
        int intentSenderType = (int) args[0];
        if (intentSenderType == INTENT_SENDER_ACTIVITY) {
            ComponentResolver.preparePluginIntent(mPluginManager, target);
        }
    }

}
