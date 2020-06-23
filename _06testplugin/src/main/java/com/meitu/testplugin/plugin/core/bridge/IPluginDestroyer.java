package com.meitu.testplugin.plugin.core.bridge;

import android.content.Context;

/**
 * Created by chidehang on 2020-03-18
 */
public interface IPluginDestroyer {

    /**
     * 结束插件中运行的任务、释放资源等
     */
    void destroy(Context hostContext);
}
