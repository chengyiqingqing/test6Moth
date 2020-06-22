package com.meitu.appupdater.updater.net;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @Author shaowenwen
 * @Date 2020-06-14 18:04
 */
public class OkHttpNetManager implements INetManager {

    private static OkHttpClient okHttpClient;
    private static Handler handler = new Handler(Looper.getMainLooper());

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(15, TimeUnit.SECONDS);
        okHttpClient = builder.build();
    }

    @Override
    public void get(String url, final INetCallback callback) {
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(url)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) callback.failed(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    final String target = response.body().string();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.success(target);
                            }
                        }
                    });
                } catch (Throwable t) {
                    t.printStackTrace();
                    if (callback != null) callback.failed(t);
                }
            }
        });
    }

    @Override
    public void download(String url, File targetFile, INetDownloadCallback iNetDownloadCallback) {

    }

}
