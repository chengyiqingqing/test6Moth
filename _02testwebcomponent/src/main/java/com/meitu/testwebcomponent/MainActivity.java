package com.meitu.testwebcomponent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.meitu.testwebcomponent.web.WebLauncher;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WebLauncher.launchWeb(this, "file:///android_asset/www/common/privacy.html");
//        WebLauncher.launchWeb(this, "file:///android_asset/www/common/service.html");
    }

}
