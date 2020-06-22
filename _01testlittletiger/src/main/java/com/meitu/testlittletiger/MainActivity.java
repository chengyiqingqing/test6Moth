package com.meitu.testlittletiger;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.qhmt.mobile.Utils;

/**
 * 小虎sdk0612质量检测
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
//                Looper.prepare();
                Log.d(TAG, "onCreate() called with: 初始化前");
                Utils.init(getApplication());
                Log.d(TAG, "onCreate() called with: 初始化后");
//                Looper.loop();
            }
        }).start();
    }

}
