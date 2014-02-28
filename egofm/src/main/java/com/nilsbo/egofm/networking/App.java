package com.nilsbo.egofm.networking;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.nilsbo.egofm.BuildConfig;

public class App extends Application {
    private static final String TAG = "com.nilsbo.egofm.volley.App";
    private static Context context;

    public void onCreate() {
        super.onCreate();
        initSingletons();
        this.context = getApplicationContext();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "is debug");
//            GoogleAnalytics.getInstance(this).setDryRun(true);
        }
    }

    protected void initSingletons() {
        MyVolley.init(this);
    }

    public static Context getAppContext() {
        return context;
    }
}