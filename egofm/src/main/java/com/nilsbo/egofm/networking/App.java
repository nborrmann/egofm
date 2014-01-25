package com.nilsbo.egofm.networking;

import android.app.Application;
import android.util.Log;

public class App extends Application {
    private static final String TAG = "com.nilsbo.egofm.volley.App";
    private static final boolean DEBUG = true;

    public void onCreate() {
        super.onCreate();
        initSingletons();
    }

    protected void initSingletons() {
        MyVolley.init(this);
    }
}