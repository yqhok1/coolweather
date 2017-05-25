package com.example.yqhok.coolweather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivitySettingsBinding;

public class SettingsActivity extends BaseActivity<ActivitySettingsBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("设置");
        showContentView();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

}
