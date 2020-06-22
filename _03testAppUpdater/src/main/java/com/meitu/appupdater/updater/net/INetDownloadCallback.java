package com.meitu.appupdater.updater.net;

import java.io.File;

/**
 * @Author shaowenwen
 * @Date 2020-06-14 18:00
 */
public interface INetDownloadCallback {

    void success(File apkFile);

    void progress(int progress);

    void failed(Throwable t);

}
