package com.meitu.appupdater.updater.net;

import java.io.File;

/**
 * @Author shaowenwen
 * @Date 2020-06-14 18:00
 */
public interface INetManager {

    void get(String url, INetCallback callback);

    void download(String url, File targetFile, INetDownloadCallback iNetDownloadCallback);

}
