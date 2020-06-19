package com.meitu.test6month;

import android.app.Activity;

import java.lang.ref.WeakReference;

public class SingleInstance {

    private Activity activity;
    private WeakReference<Activity> activityWeakReference;

    private SingleInstance() {

    }

    private static class InnerHolder {
        private final static SingleInstance singleInstance = new SingleInstance();
    }

    public static SingleInstance getInstance() {
        return InnerHolder.singleInstance;
    }

    public void setActivityWeakReference(Activity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
