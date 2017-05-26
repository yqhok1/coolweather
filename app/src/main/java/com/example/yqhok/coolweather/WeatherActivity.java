package com.example.yqhok.coolweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuItemView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivityWeatherBinding;
import com.example.yqhok.coolweather.databinding.ForecastItemBinding;
import com.example.yqhok.coolweather.gson.Forecast;
import com.example.yqhok.coolweather.gson.Weather;
import com.example.yqhok.coolweather.service.AutoUpdateService;
import com.example.yqhok.coolweather.service.SettingTimeUpdateService;
import com.example.yqhok.coolweather.util.HttpUtil;
import com.example.yqhok.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends BaseActivity<ActivityWeatherBinding> implements SwipeRefreshLayout.OnRefreshListener, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private Toolbar toolbar;
    private ScrollView weatherLayout;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private FloatingActionButton fab;

    private NavigationMenuItemView city1;
    private NavigationMenuItemView city2;
    private NavigationMenuItemView city3;
    private NavigationMenuItemView city4;
    private NavigationMenuItemView city5;

    private ForecastItemBinding forecastItemBinding;

    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        initView();
        initData();
        showContentView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRefresh();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, WeatherActivity.class);
        context.startActivity(intent);
    }

    private void initView() {
        toolbar = getToolBar();
        weatherLayout = bindingView.weatherLayout;
        degreeText = bindingView.now.degreeText;
        weatherInfoText = bindingView.now.weatherInfoText;
        forecastLayout = bindingView.forecast.forecastLayout;
        aqiText = bindingView.aqi.aqiText;
        pm25Text = bindingView.aqi.pm25Text;
        comfortText = bindingView.suggestion.comfortText;
        carWashText = bindingView.suggestion.carWashText;
        sportText = bindingView.suggestion.sportText;
        swipeRefresh = bindingView.swipeRefresh;
        drawerLayout = bindingView.drawerLayout;
        navigationView = bindingView.navigationView;
        fab = bindingView.fab;
        toolbar.setBackgroundResource(android.R.color.transparent);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);
        navigationView.inflateHeaderView(R.layout.nav_header);
        navigationView.inflateMenu(R.menu.nav_menu);
        navigationView.setNavigationItemSelectedListener(this);
        city1 = (NavigationMenuItemView) findViewById(R.id.city1);
        city2 = (NavigationMenuItemView) findViewById(R.id.city2);
        city3 = (NavigationMenuItemView) findViewById(R.id.city3);
        city4 = (NavigationMenuItemView) findViewById(R.id.city4);
        city5 = (NavigationMenuItemView) findViewById(R.id.city5);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeResources(R.color.Red);
        fab.setOnClickListener(this);
        fab.setBackgroundResource(android.R.color.transparent);
        getRootPic().setVisibility(View.VISIBLE);
    }

    private void initData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        String bingPic = prefs.getString("bing_pic",null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(getRootPic());
        } else {
            loadBingPic();
        }
    }

    public void requestWeather(final String weatherId) {
        String weatherURL = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=60e311f001ea4a7693e674f3671cda85";
        HttpUtil.sendOkHttpRequest(weatherURL, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

        });
        loadBingPic();
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        toolbar.setTitle(cityName);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList){
            forecastItemBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.forecast_item, forecastLayout, false);
            TextView dateText = forecastItemBinding.dataText;
            TextView infoText = forecastItemBinding.infoText;
            TextView maxText = forecastItemBinding.maxText;
            TextView minText = forecastItemBinding.minText;
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(forecastItemBinding.getRoot());
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carwash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
        Intent intent1 = new Intent(this, SettingTimeUpdateService.class);
        startService(intent1);
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
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(getRootPic());
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                RegisterActivity.start(this);
                return true;
            case R.id.settings:
                SettingsActivity.start(this);
                return true;
            default:
                break;
        }
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return false;
    }

    @Override
    public void onRefresh() {
        requestWeather(mWeatherId);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.city1:
                drawerLayout.closeDrawers();
                break;
            case R.id.city2:
                drawerLayout.closeDrawers();
                break;
            case R.id.city3:
                drawerLayout.closeDrawers();
                break;
            case R.id.city4:
                drawerLayout.closeDrawers();
                break;
            case R.id.city5:
                drawerLayout.closeDrawers();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                break;
        }
    }
}
