package com.example.administrator.voicemonitor;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 10/14/2017.
 */

public class PApplication extends Application {
    private static final String TAG = "PApp";
    private static PApplication instance;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = getApplicationContext();
    }

    public static PApplication getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return context;
    }
}
