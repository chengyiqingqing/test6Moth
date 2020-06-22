package com.meitu.testplugin.plugin.core.proxy;


import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.meitu.testplugin.plugin.core.ComponentResolver;
import com.meitu.testplugin.plugin.core.PluginManager;
import com.meitu.testplugin.plugin.core.ThemeCompat;
import com.meitu.testplugin.plugin.util.Reflector;

public class InstrumentationProxy extends Instrumentation implements Handler.Callback {

    public static final int LAUNCH_ACTIVITY = 100;

    private Context mHostContext;
    private PluginManager mPluginManager;
    private Instrumentation mBase;

    public InstrumentationProxy(PluginManager pluginManager, Instrumentation base) {
        this.mPluginManager = pluginManager;
        this.mHostContext = pluginManager.getHostContext();
        this.mBase = base;
    }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        if (message.what == LAUNCH_ACTIVITY) {
            Object activityClientRecord = message.obj;
            try {
                // TODO: 设置classLoader  by shaowenwen 2020-06-21
                Reflector reflector = Reflector.with(activityClientRecord);
                Intent intent = reflector.field("intent").get();
                intent.setExtrasClassLoader(mHostContext.getClassLoader());

                // TODO: 设置activity主题  by shaowenwen 2020-06-21
                ActivityInfo activityInfo = reflector.field("activityInfo").get();
                if (ComponentResolver.isPluginIntent(intent)) {
                    int theme = ThemeCompat.getTheme(mHostContext, intent);
                    if (theme != ThemeCompat.INVALID_THEME_ID) {
                        activityInfo.theme = theme;
                    }
                }

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return false;
    }

}
