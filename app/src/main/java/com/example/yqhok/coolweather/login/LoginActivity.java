package com.example.yqhok.coolweather.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    private Toolbar toolbar;
    private TextView info;
    private TextView hint;
    private EditText password;
    private Button confirm;

    private String strUserName;
    private String strPassword;
    private Boolean isSigned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        initView();
        loadBingPic();
        showContentView();
    }

    private void initView() {
        getRootPic().setAlpha(0.7f);
        toolbar = getToolBar();
        Intent intent = getIntent();
        strUserName = intent.getStringExtra("userName");
        isSigned = intent.getBooleanExtra("isSigned", false);
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
                if (isSigned) {
                    AVUser.logInInBackground(strUserName, strPassword, new LogInCallback<AVUser>() {
                        @Override
                        public void done(AVUser avUser, AVException e) {
                            if (e == null) {
                                if (avUser.get("currentCityId") != null) {
                                    Utility.LoadDataTask task = new Utility.LoadDataTask();
                                    task.execute();
                                    while (!task.isFinished);
                                    WeatherActivity.start(LoginActivity.this);
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
                }
                break;
        }
    }

}
