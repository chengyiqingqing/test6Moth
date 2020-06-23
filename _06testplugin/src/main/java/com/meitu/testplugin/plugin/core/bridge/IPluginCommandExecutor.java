package com.meitu.testplugin.plugin.core.bridge;

/**
 * host调用plugin的可执行代码
 * Created by chidehang on 2019-11-24
 */
public interface IPluginCommandExecutor {

    /**
     * 执行plugin的方法，同步返回结果
     */
    Object executeSync(int command, Object... params) throws Exception;

    /**
     * 执行plugin的方法，异步返回结果
     */
    void executeAsync(int command, PluginCommandCallback callback, Object... params) throws Exception;

    interface PluginCommandCallback {
        void result(Object result);
    }
}
