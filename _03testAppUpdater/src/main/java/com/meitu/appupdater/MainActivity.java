package com.meitu.appupdater;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.meitu.appupdater.updater.net.INetCallback;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // http://59.110.162.30/app_updater_version.json
        AppUpdater.sInstance.getManager().get("http://59.110.162.30/app_updater_version.json", new INetCallback() {
            @Override
            public void success(String response) {
                Log.d(TAG, "success() called with: response = [" + response + "]");
                // 做JSon解析

                // 检查版本
                // 弹窗，触发更新

            }

            @Override
            public void failed(Throwable t) {
                Log.d(TAG, "failed() called with: t = [" + t.getMessage() + "]");
            }
        });
    }

}
