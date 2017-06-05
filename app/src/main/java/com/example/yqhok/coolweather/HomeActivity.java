package com.example.yqhok.coolweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVUser;
import com.bumptech.glide.Glide;
import com.example.yqhok.coolweather.application.MyApplication;
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

    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    private Toolbar toolbar;
    private TextView info;
    private TextView welcomeInfo;
    private Button login;
    private Button chooseArea;

    private static boolean isNetworkAvailable;

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
        initNetReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
    }

    private void initView() {
        toolbar = getToolBar();
        info = bindingView.info;
        welcomeInfo = bindingView.welcomeInfo;
        login = bindingView.actionLogin;
        chooseArea = bindingView.actionChooseArea;
        toolbar.setBackgroundResource(android.R.color.transparent);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
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
                while (!task.isFinished) ;
                WeatherActivity.start(HomeActivity.this);
                HomeActivity.this.finish();
            } else {
                login.setText("退出登录");
                info.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void loadBingPic() {
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

    private void initNetReceiver() {
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_login:
                if (isNetworkAvailable) {
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
                } else {
                    Toast.makeText(this, "当前无网络连接", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_choose_area:
                if (isNetworkAvailable) {
                    Intent intent = new Intent(this, ChooseAreaActivity.class);
                    intent.putExtra("flag", "HomeActivity");
                    startActivity(intent);
                    break;
                } else {
                    Toast.makeText(this, "当前无网络连接", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo == null) {
                Toast.makeText(context, "当前无网络连接", Toast.LENGTH_LONG).show();
                isNetworkAvailable = false;
                welcomeInfo.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.Blue));
                info.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.Blue));
            } else {
                isNetworkAvailable = true;
                loadBingPic();
                welcomeInfo.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.White));
                info.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.White));
            }
        }

    }

}
