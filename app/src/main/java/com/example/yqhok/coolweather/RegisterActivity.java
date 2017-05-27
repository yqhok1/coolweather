package com.example.yqhok.coolweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivityRegisterBinding;
import com.example.yqhok.coolweather.util.HttpUtil;

import java.io.IOException;

import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.utils.SMSLog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterActivity extends BaseActivity<ActivityRegisterBinding> implements TextWatcher, View.OnClickListener {

    private static final String DEFAULT_COUNTRY_ID = "42";

    private Toolbar toolbar;
    private EditText phone;
    private Button getVerificationCode;

    private String strPhone;

    private EventHandler eventHandler;
    private OnSendMessageHandler osmHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setTitle("登录");
        initView();
        initSMS();
        loadBingPic();
        showContentView();
    }

    @Override
    protected void onDestroy() {
        cn.smssdk.SMSSDK.unregisterEventHandler(eventHandler);
        super.onDestroy();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }

    private void initView() {
        getRootPic().setAlpha(0.7f);
        toolbar = getToolBar();
        phone = bindingView.phone;
        getVerificationCode = bindingView.getVerificationCode;
        toolbar.setBackgroundResource(R.color.Black);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        phone.addTextChangedListener(this);
        getVerificationCode.setOnClickListener(this);
        getVerificationCode.setClickable(false);
        getRootPic().setVisibility(View.VISIBLE);
    }

    private void initSMS() {
        cn.smssdk.SMSSDK.initSDK(this, "1e2481279d874", "88b7b1d5f46e5063b79146bd7e9e3c07");
        eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                if (result == cn.smssdk.SMSSDK.RESULT_COMPLETE) {
                    if (event == cn.smssdk.SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {

                    }
                } else {
                    ((Throwable)data).printStackTrace();
                }
            }
        };
        cn.smssdk.SMSSDK.registerEventHandler(eventHandler);
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
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(RegisterActivity.this).load(bingPic).into(getRootPic());
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
        if (s.length() == 11) {
            getVerificationCode.setClickable(true);
            strPhone = s.toString();
        } else {
            getVerificationCode.setClickable(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_verification_code:
                SMSLog.getInstance().i("verification phone ==>>" + strPhone);
                cn.smssdk.SMSSDK.getVerificationCode("+" + cn.smssdk.SMSSDK.getCountry(DEFAULT_COUNTRY_ID)[1], strPhone, osmHandler);
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                if (strPhone.length() == 11) {
                    intent.putExtra("phone", strPhone);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "手机号输入有误", Toast.LENGTH_SHORT).show();
                }
            break;
        }
    }

}
