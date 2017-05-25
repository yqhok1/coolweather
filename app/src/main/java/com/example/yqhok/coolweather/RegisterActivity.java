package com.example.yqhok.coolweather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivityRegisterBinding;

import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.utils.SMSLog;

public class RegisterActivity extends BaseActivity<ActivityRegisterBinding> implements TextWatcher, View.OnClickListener {

    private static final String DEFAULT_COUNTRY_ID = "42";

    private EditText phone;
    private Button getVerificationCode;

    private String strPhone;

    private EventHandler eventHandler;
    private OnSendMessageHandler osmHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("登录");
        initView();
        initSMS();
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
        phone = bindingView.phone;
        getVerificationCode = bindingView.getVerificationCode;
        phone.addTextChangedListener(this);
        getVerificationCode.setOnClickListener(this);
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        strPhone = s.toString();
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
