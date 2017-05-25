package com.example.yqhok.coolweather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivitySettingsBinding;

public class SettingsActivity extends BaseActivity<ActivitySettingsBinding> {

    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("设置");
        initView();
        showContentView();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    private void initView() {
        //getFragmentManager().beginTransaction().replace(R.id.settings_frame_layout, settingsFragment).commit();
    }

}
