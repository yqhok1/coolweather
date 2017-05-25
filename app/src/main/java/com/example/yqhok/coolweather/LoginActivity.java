package com.example.yqhok.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivityLoginBinding;
import com.example.yqhok.coolweather.util.HttpUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.utils.SMSLog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> implements TextWatcher, View.OnClickListener {

    private static final String DEFAULT_COUNTRY_ID = "42";

    private Toolbar toolbar;
    private TextView info;
    private TextView hint;
    private EditText code;
    private Button getVerificationCode;
    private Button confirm;

    private String strPhone;
    private String strCode;

    private EventHandler eventHandler;
    private OnSendMessageHandler osmHandler;

    private TimeCount time;

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
        initSMS();
        loadBingPic();
        showContentView();
    }

    @Override
    protected void onDestroy() {
        cn.smssdk.SMSSDK.unregisterEventHandler(eventHandler);
        super.onDestroy();
    }

    private void initView() {
        getRootPic().setAlpha(0.7f);
        toolbar = getToolBar();
        Intent intent = getIntent();
        strPhone = intent.getStringExtra("phone");
        info = bindingView.info;
        hint = bindingView.hint;
        code = bindingView.code;
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
        getVerificationCode = bindingView.getVerificationCode;
        info.setText("短信已发送至" + strPhone + "\n请输入验证码");
        code.addTextChangedListener(this);
        TextPaint paint = hint.getPaint();
        paint.setFakeBoldText(true);
        getVerificationCode.setOnClickListener(this);
        confirm.setOnClickListener(this);
        getRootPic().setVisibility(View.VISIBLE);
        time = new TimeCount(30000, 1000);
        time.start();
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

    private String checkSMS(String strPhone, String code) {
        String address = "https://webapi.sms.mob.com/sms/verify";
        String params = "appkey=1e2481279d874&phone=" + strPhone + "&zone=86&&code=" + code;
        HttpURLConnection conn = null;
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
                public X509Certificate[] getAcceptedIssuers(){return null;}
                public void checkClientTrusted(X509Certificate[] certs, String authType){}
                public void checkServerTrusted(X509Certificate[] certs, String authType){}
            }};

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());

            //ip host verify
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return urlHostName.equals(session.getPeerHost());
                }
            };

            //set ip host verify
            HttpsURLConnection.setDefaultHostnameVerifier(hv);

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            URL url = new URL(address);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");// POST
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            // set params ;post params
            if (params != null) {
                conn.setDoOutput(true);
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.write(params.getBytes(Charset.forName("UTF-8")));
                out.flush();
                out.close();
            }
            conn.connect();
            //get result
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String result = parsRtn(conn.getInputStream());
                return result;
            } else {
                System.out.println(conn.getResponseCode() + " "+ conn.getResponseMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null)
                conn.disconnect();
        }
        return null;
    }

    private String parsRtn(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = null;
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if(first){
                first = false;
            }else{
                buffer.append("\n");
            }
            buffer.append(line);
        }
        return buffer.toString();
    }

    private boolean parseResult(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            int status = jsonObject.getInt("status");
            if (status == 200) {
                return true;
            } else if (status == 405 || status == 406) {
                Toast.makeText(this, "错误", Toast.LENGTH_SHORT).show();
            } else if (status == 456 || status == 457) {
                Toast.makeText(this, "手机号码错误", Toast.LENGTH_SHORT).show();
                finish();
            } else if (status == 466) {
                Toast.makeText(this, "校验码为空", Toast.LENGTH_SHORT).show();
            } else if (status == 467) {
                Toast.makeText(this, "请求校验码频繁", Toast.LENGTH_SHORT).show();
            } else if (status == 468) {
                Toast.makeText(this, "校验码错误", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "未知错误", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        strCode = s.toString();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                if (strCode.length() == 6) {
                    String result = checkSMS(strPhone, strCode);
                    if (parseResult(result)) {
                        WeatherActivity.start(this);
                    }
                }
                break;
            case R.id.get_verification_code:
                time.start();
                SMSLog.getInstance().i("verification phone ==>>" + strPhone);
                cn.smssdk.SMSSDK.getVerificationCode("+" + cn.smssdk.SMSSDK.getCountry(DEFAULT_COUNTRY_ID)[1], strPhone, osmHandler);
                break;
        }
    }

    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            getVerificationCode.setBackgroundResource(R.color.Gray);
            getVerificationCode.setClickable(false);
            getVerificationCode.setText(millisUntilFinished / 1000 + "秒");
        }

        @Override
        public void onFinish() {
            getVerificationCode.setBackgroundResource(R.color.White);
            getVerificationCode.setClickable(true);
            getVerificationCode.setText("重新发送");
        }
    }

}
