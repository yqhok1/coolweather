package com.example.yqhok.coolweather;

import android.content.Context;

import org.litepal.LitePalApplication;

/**
 * Created by yqhok on 2017/5/26.
 */

public class MyApplication extends LitePalApplication {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePalApplication.initialize(context);
    }

    public static Context getContext() {
        return context;
    }

}
