package com.meitu.appupdater.updater.net;

/**
 * @Author shaowenwen
 * @Date 2020-06-14 17:58
 */
public interface INetCallback {

    void success(String response);

    void failed(Throwable t);

}
