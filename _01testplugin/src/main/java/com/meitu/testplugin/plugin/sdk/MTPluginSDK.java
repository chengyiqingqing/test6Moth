package com.meitu.testplugin.plugin.sdk;

import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

public class MTPluginSDK {

    private static AtomicBoolean sInit = new AtomicBoolean(false);

    public static void init(Context context, String appId, String channel) {
        if (context==null){
            return;
        }
        if (sInit.compareAndSet(false,true)){

        }
    }

}
