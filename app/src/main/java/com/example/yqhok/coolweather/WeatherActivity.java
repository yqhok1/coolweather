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

import com.avos.avoscloud.AVUser;
import com.bumptech.glide.Glide;
import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivityWeatherBinding;
import com.example.yqhok.coolweather.databinding.ForecastItemBinding;
import com.example.yqhok.coolweather.db.WeatherInfo;
import com.example.yqhok.coolweather.gson.Forecast;
import com.example.yqhok.coolweather.gson.Weather;
import com.example.yqhok.coolweather.login.RegisterActivity;
import com.example.yqhok.coolweather.service.AutoUpdateService;
import com.example.yqhok.coolweather.util.HttpUtil;
import com.example.yqhok.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

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
        WeatherInfo weather = DataSupport.where("isCurrent = ?", "true").findFirst(WeatherInfo.class);
        if (weather != null) {
            navigationView.getMenu().add(R.id.choose_city, 0, 0, weather.getCityName()).setIcon(R.drawable.city);
            List<WeatherInfo> weatherList = DataSupport.where("isCurrent = ?", "false").find(WeatherInfo.class);
            for (int i = 1; i < weatherList.size() + 1; i ++) {
                navigationView.getMenu().add(R.id.choose_city, i, i, weatherList.get(i).getCityName()).setIcon(R.drawable.city);
            }
        } else {
            List<WeatherInfo> weatherList = DataSupport.findAll(WeatherInfo.class);
            for (int i = 0; i < weatherList.size(); i ++) {
                navigationView.getMenu().add(R.id.choose_city, i, i, weatherList.get(i).getCityName()).setIcon(R.drawable.city);
            }
        }
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeResources(R.color.Red);
        fab.setOnClickListener(this);
        fab.setBackgroundResource(android.R.color.transparent);
        getRootPic().setVisibility(View.VISIBLE);
    }

    private void initData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        Intent intent = getIntent();
        if (intent.hasExtra("weather_id")) {
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        } else if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }
        String bingPic = prefs.getString("bing_pic", null);
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
                if (weather != null && "ok".equals(weather.status)) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString("weather", responseText);
                    editor.apply();
                    mWeatherId = weather.basic.weatherId;
                    WeatherInfo currentWeather = DataSupport.where("weatherId = ?", mWeatherId).findFirst(WeatherInfo.class);
                    if (currentWeather == null) {
                        currentWeather = new WeatherInfo();
                        currentWeather.setWeatherId(weather.basic.weatherId);
                        currentWeather.setIsCurrent(true);
                    }
                    currentWeather.setCityName(weather.basic.cityName);
                    currentWeather.setMin(weather.forecastList.get(0).temperature.min);
                    currentWeather.setMax(weather.forecastList.get(0).temperature.max);
                    currentWeather.setInfo(weather.now.more.info);
                    currentWeather.save();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeatherInfo(weather);
                            swipeRefresh.setRefreshing(false);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                            swipeRefresh.setRefreshing(false);
                        }
                    });
                }
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
                AVUser user = AVUser.getCurrentUser();
                if (user == null) {
                    RegisterActivity.start(this);
                } else {
                    UserInfoActivity.start(this);
                }
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
            default:
                drawerLayout.closeDrawers();
                if (DataSupport.isExist(WeatherInfo.class)) {
                    WeatherInfo weather = DataSupport.where("cityName = ?", item.getTitle().toString()).findFirst(WeatherInfo.class);
                    mWeatherId = weather.getWeatherId();
                    navigationView.getMenu().clear();
                    List<WeatherInfo> weatherList = DataSupport.findAll(WeatherInfo.class);
                    for (int i = 0; i < weatherList.size(); i ++) {
                        navigationView.getMenu().add(R.id.choose_city, i, i, weatherList.get(i).getCityName()).setIcon(R.drawable.city);
                    }
                    onRefresh();
                }
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                drawerLayout.closeDrawers();
                ChooseAreaActivity.start(this);
                finish();
                break;
        }
    }

}
