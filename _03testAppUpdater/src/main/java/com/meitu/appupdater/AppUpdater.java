package com.meitu.appupdater;

import com.meitu.appupdater.updater.net.INetManager;
import com.meitu.appupdater.updater.net.OkHttpNetManager;

/**
 * @Author shaowenwen
 * @Date 2020-06-14 18:03
 */
public class AppUpdater {

    public final static AppUpdater sInstance = new AppUpdater();

    private INetManager manager = new OkHttpNetManager();

    /**
     * 可以在application的时候去调用
     */
    public void setManager(INetManager manager) {
        this.manager = manager;
    }

    public INetManager getManager() {
        return manager;
    }



}
