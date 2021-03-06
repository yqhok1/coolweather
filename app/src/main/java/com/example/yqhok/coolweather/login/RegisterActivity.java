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
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.bumptech.glide.Glide;
import com.example.yqhok.coolweather.R;
import com.example.yqhok.coolweather.application.MyApplication;
import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivityRegisterBinding;
import com.example.yqhok.coolweather.util.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterActivity extends BaseActivity<ActivityRegisterBinding> implements TextWatcher, View.OnClickListener {

    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    private Toolbar toolbar;
    private TextView basicInfo;
    private TextView info;
    private TextView hint;
    private EditText userName;
    private Button confirm;

    private String strUserName;

    private static boolean isNetworkAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
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

    public static void start(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }

    private void initView() {
        getRootPic().setAlpha(0.7f);
        toolbar = getToolBar();
        basicInfo = bindingView.basicInfo;
        info = bindingView.info;
        hint = bindingView.hint;
        userName = bindingView.userName;
        confirm = bindingView.confirm;
        toolbar.setBackgroundResource(R.color.Black);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        userName.addTextChangedListener(this);
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
        strUserName = s.toString();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                if (isNetworkAvailable) {
                    if (strUserName == null || strUserName.length() < 5 || strUserName.length() > 11) {
                        Toast.makeText(this, "用户名不合法", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final Intent intent = new Intent(this, LoginActivity.class);
                    AVQuery<AVUser> query = new AVQuery<>("_User");
                    try {
                        query.whereEqualTo("username", strUserName);
                        query.getFirstInBackground(new GetCallback<AVUser>() {
                            @Override
                            public void done(AVUser avUser, AVException e) {
                                Boolean isSigned = false;
                                if (e != null) {
                                    e.printStackTrace();
                                    Toast.makeText(RegisterActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (avUser != null) {
                                        isSigned = true;
                                    }
                                    intent.putExtra("userName", strUserName);
                                    intent.putExtra("isSigned", isSigned);
                                    startActivity(intent);
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
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
                userName.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.Blue));
                userName.setHintTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.Blue));
            } else {
                isNetworkAvailable = true;
                loadBingPic();
                basicInfo.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.White));
                info.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.White));
                hint.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.White));
                userName.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.White));
                userName.setHintTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.White));
            }
        }

    }

}
