package com.example.yqhok.coolweather;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.bumptech.glide.Glide;
import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivityHomeBinding;
import com.example.yqhok.coolweather.login.RegisterActivity;
import com.example.yqhok.coolweather.util.HttpUtil;
import com.example.yqhok.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeActivity extends BaseActivity<ActivityHomeBinding> implements View.OnClickListener {

    private Toolbar toolbar;
    private TextView info;
    private Button login;
    private Button chooseArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_home);
        initView();
        loadBingPic();
        showContentView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
    }

    private void initView() {
        toolbar = getToolBar();
        info = bindingView.info;
        login = bindingView.actionLogin;
        chooseArea = bindingView.actionChooseArea;
        toolbar.setBackgroundResource(R.color.Black);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setTitle("CoolWeather");
        login.setOnClickListener(this);
        chooseArea.setOnClickListener(this);
    }

    private void initData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getString("weather", null) != null) {
            WeatherActivity.start(this);
        }
        AVUser user = AVUser.getCurrentUser();
        if (user != null) {
            if (user.get("currentCityId") != null) {
                Utility.LoadDataTask task = new Utility.LoadDataTask();
                task.execute();
                while (!task.isFinished);
                WeatherActivity.start(HomeActivity.this);
                HomeActivity.this.finish();
            } else {
                login.setText("退出登录");
                info.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(HomeActivity.this).load(bingPic).into(getRootPic());
                    }
                });
            }
        });
        getRootPic().setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_login:
                if (login.getText().toString().equals("退出登录")) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                    dialog.setTitle("确定要退出？");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AVUser.logOut();
                            info.setVisibility(View.VISIBLE);
                            login.setText("注册或登录");
                        }
                    });
                    dialog.setNegativeButton("取消", null);
                    dialog.show();
                } else {
                    RegisterActivity.start(this);
                }
                break;
            case R.id.action_choose_area:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("flag", "HomeActivity");
                startActivity(intent);
                break;
        }
    }
}
