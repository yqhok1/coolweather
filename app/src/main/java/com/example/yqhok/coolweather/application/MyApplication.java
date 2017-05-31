package com.example.yqhok.coolweather.application;

import android.content.Context;

import com.avos.avoscloud.AVOSCloud;

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
        AVOSCloud.initialize(this, "ny1OrJPEwlsKYp8b4OlER0KR-gzGzoHsz", "o5SHIkdU2SOCMoxlhKLiQxmw");
        AVOSCloud.setDebugLogEnabled(true);
    }

    public static Context getContext() {
        return context;
    }

}
