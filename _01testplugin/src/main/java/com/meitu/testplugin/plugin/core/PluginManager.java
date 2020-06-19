package com.meitu.testplugin.plugin.core;

import android.app.Application;
import android.content.Context;

import com.meitu.testplugin.plugin.core.proxy.InstrumentationProxy;

public class PluginManager {

    private Application mHostApplication;
    private Context mHostContext;
    private InstrumentationProxy mInstrumentationProxy;

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
    }

    private void initHostContext(Context context) {
        if (context instanceof Application) {
            this.mHostApplication = (Application) context;
            this.mHostContext = context;
        } else {

        }


    }

}
