package com.meitu.testplugin.plugin.core.bridge;

/**
 * Created by chidehang on 2019-12-10
 */
public interface IExecutorFactory {
    IPluginCommandExecutor createExecutor();
    IPluginDestroyer createDestroyer();
}
