package com.example.yqhok.coolweather.login;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SignUpCallback;
import com.bumptech.glide.Glide;
import com.example.yqhok.coolweather.ChooseAreaActivity;
import com.example.yqhok.coolweather.R;
import com.example.yqhok.coolweather.WeatherActivity;
import com.example.yqhok.coolweather.application.MyApplication;
import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivityLoginBinding;
import com.example.yqhok.coolweather.util.HttpUtil;
import com.example.yqhok.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> implements TextWatcher, View.OnClickListener {

    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    private Toolbar toolbar;
    private TextView basicInfo;
    private TextView info;
    private TextView hint;
    private EditText password;
    private Button confirm;

    private String strUserName;
    private String strPassword;
    private Boolean isSigned;

    private static boolean isNetworkAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setTitle("");
        initView();
        loadBingPic();
        showContentView();
        initNetReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    private void initView() {
        getRootPic().setAlpha(0.7f);
        toolbar = getToolBar();
        Intent intent = getIntent();
        strUserName = intent.getStringExtra("userName");
        isSigned = intent.getBooleanExtra("isSigned", false);
        basicInfo = bindingView.basicInfo;
        info = bindingView.info;
        hint = bindingView.hint;
        password = bindingView.password;
        confirm = bindingView.confirm;
        if (isSigned) {
            confirm.setText("登录");
            info.setVisibility(View.INVISIBLE);
        } else {
            confirm.setText("注册");
        }
        toolbar.setBackgroundResource(R.color.Black);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        password.addTextChangedListener(this);
        TextPaint paint = hint.getPaint();
        paint.setFakeBoldText(true);
        confirm.setOnClickListener(this);
        getRootPic().setVisibility(View.VISIBLE);
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
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(LoginActivity.this).load(bingPic).into(getRootPic());
                    }
                });
            }
        });
    }

    private void initNetReceiver() {
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        strPassword = s.toString();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                if (strPassword == null || strPassword.length() < 5 || strPassword.length() > 11) {
                    Toast.makeText(this, "密码不合法", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isNetworkAvailable) {
                    if (isSigned) {
                        AVUser.logInInBackground(strUserName, strPassword, new LogInCallback<AVUser>() {
                            @Override
                            public void done(AVUser avUser, AVException e) {
                                if (e == null) {
                                    if (avUser.get("currentCityId") != null) {
                                        Intent intent = new Intent(LoginActivity.this, WeatherActivity.class);
                                        intent.putExtra("flag", "loadData");
                                        startActivity(intent);
                                        LoginActivity.this.finish();
                                    } else {
                                        Intent intent = new Intent(LoginActivity.this, ChooseAreaActivity.class);
                                        intent.putExtra("flag", "LoginActivity");
                                        startActivity(intent);
                                        LoginActivity.this.finish();
                                    }
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        AVUser user = new AVUser();
                        user.setUsername(strUserName);
                        user.setPassword(strPassword);
                        user.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                                    if (preferences.getString("weather", null) != null) {
                                        Utility.updateUserData();
                                        Intent intent = new Intent(LoginActivity.this, WeatherActivity.class);
                                        startActivity(intent);
                                        LoginActivity.this.finish();
                                    } else {
                                        Intent intent = new Intent(LoginActivity.this, ChooseAreaActivity.class);
                                        intent.putExtra("flag", "LoginActivity");
                                        startActivity(intent);
                                        LoginActivity.this.finish();
                                    }
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(this, "当前无网络连接", Toast.LENGTH_SHORT).show();
                }
                break;
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
                basicInfo.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.Blue));
                info.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.Blue));
                hint.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.Blue));
                password.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.Blue));
                password.setHintTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.Blue));
            } else {
                isNetworkAvailable = true;
                loadBingPic();
                basicInfo.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.White));
                info.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.White));
                hint.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.White));
                password.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.White));
                password.setHintTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.White));
            }
        }

    }

}
