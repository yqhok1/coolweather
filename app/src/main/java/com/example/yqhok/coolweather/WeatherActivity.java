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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVUser;
import com.bumptech.glide.Glide;
import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivityWeatherBinding;
import com.example.yqhok.coolweather.databinding.ForecastItemBinding;
import com.example.yqhok.coolweather.databinding.ItemHourInfoItemBinding;
import com.example.yqhok.coolweather.db.WeatherInfo;
import com.example.yqhok.coolweather.gson.Forecast;
import com.example.yqhok.coolweather.gson.Hourly;
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

    private Intent intent;

    private Toolbar toolbar;
    private ScrollView weatherLayout;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout hourinfoLayout;
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

    private ItemHourInfoItemBinding itemHourInfoItemBinding;
    private ForecastItemBinding forecastItemBinding;

    private String mWeatherId;

    private ImageView weatherimg;
    private TextView updateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        initData();
        initView();
        showContentView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initMenu();
        onRefresh();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, WeatherActivity.class);
        context.startActivity(intent);
    }

    private void initData() {
        intent = getIntent();
        String flag = intent.getStringExtra("flag");
        if (flag != null && flag.equals("loadData")) {
            Utility.LoadDataTask task = new Utility.LoadDataTask();
            task.execute();
            while (!task.isFinished);
        }
    }

    private void initView() {
        toolbar = getToolBar();
        weatherLayout = bindingView.weatherLayout;
        degreeText = bindingView.now.degreeText;
        weatherInfoText = bindingView.now.weatherInfoText;
        hourinfoLayout =bindingView.itemhourinfo.hourinfoLayout;
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
        weatherimg = bindingView.now.weatherImg;
        updateTime = bindingView.now.updateTime;
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
        initMenu();
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeResources(R.color.Blue);
        fab.setOnClickListener(this);
        fab.setBackgroundResource(android.R.color.transparent);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (intent.hasExtra("weather_id")) {
            mWeatherId = intent.getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        } else if (weatherString != null) {
            Weather currentWeather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = currentWeather.basic.weatherId;
            showWeatherInfo(currentWeather);
        }
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(getRootPic());
        } else {
            loadBingPic();
        }
        getRootPic().setVisibility(View.VISIBLE);
    }

    private void initMenu() {
        WeatherInfo weather = DataSupport.where("isCurrent = ?", "1").findFirst(WeatherInfo.class);
        List<WeatherInfo> weatherList = DataSupport.findAll(WeatherInfo.class);
        if (weather != null) {
            navigationView.getMenu().clear();
            navigationView.getMenu().add(R.id.choose_city, 0, 0, weather.getCityName()).setIcon(R.drawable.city);
            for (int i = 0, j = 1; i < weatherList.size(); i ++, j ++) {
                if (weatherList.get(i).getIsCurrent()) {
                    j --;
                } else {
                    navigationView.getMenu().add(R.id.choose_city, j, j, weatherList.get(i).getCityName()).setIcon(R.drawable.city);
                }
            }
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
                        if (!DataSupport.isExist(WeatherInfo.class)) {
                            currentWeather.setIsCurrent(true);
                        }
                        currentWeather.setWeatherId(weather.basic.weatherId);
                    }
                    currentWeather.setCityName(weather.basic.cityName);
                    currentWeather.setMin(weather.forecastList.get(0).temperature.min);
                    currentWeather.setMax(weather.forecastList.get(0).temperature.max);
                    currentWeather.setInfo(weather.now.more.info);
                    currentWeather.save();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Utility.updateUserData();
                        }
                    }).start();
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
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        String updatetime = "上次更新时间："+ weather.basic.update.updateTime.split(" ")[1];
        updateTime.setText(updatetime);
        toolbar.setTitle(cityName);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        hourinfoLayout.removeAllViews();
        for (Hourly hourly :weather.hourlyList) {
            itemHourInfoItemBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.item_hour_info_item, hourinfoLayout, false);
            TextView hourdateText = itemHourInfoItemBinding.hourDataText;
            ImageView hourWeatherImg = itemHourInfoItemBinding.hourWeatherImg;
            TextView hourinfoText = itemHourInfoItemBinding.hourInfoText;
            TextView hourHum = itemHourInfoItemBinding.hourHum;
            TextView hourPop = itemHourInfoItemBinding.hourPop;
            TextView hourPres = itemHourInfoItemBinding.hourPres;
            TextView hourTemp = itemHourInfoItemBinding.hourTemp;
            hourdateText.setText("时间：" + hourly.date);
            hourinfoText.setText(hourly.more1.info);
            weatherimginfo(hourly.more1.info, hourWeatherImg);
            hourHum.setText("相对湿度：" + hourly.hum + "%");
            hourPop.setText("降水概率：" + hourly.pop + "%");
            hourPres.setText("气压：" + hourly.pres);
            hourTemp.setText("温度：" + hourly.tmp + "℃");
            hourinfoLayout.addView(itemHourInfoItemBinding.getRoot());
        }
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList){
            forecastItemBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.forecast_item, forecastLayout, false);
            TextView dateText = forecastItemBinding.dataText;
            TextView infoText = forecastItemBinding.infoText;
            TextView maxText = forecastItemBinding.maxText;
            TextView minText = forecastItemBinding.minText;
            ImageView smallweatherimg = forecastItemBinding.weatherImg;
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            weatherimginfo(forecast.more.info, smallweatherimg);
            maxText.setText(forecast.temperature.max + "℃");
            minText.setText(forecast.temperature.min + "℃");
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
        hourinfoLayout.setVisibility(View.VISIBLE);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
        nowweatherimginfo(weatherInfo, weatherimg);
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
        getMenuInflater().inflate(R.menu.toolbar_weather, menu);
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
                    initMenu();
                    onRefresh();
                    Utility.updateUserData();
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
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("flag", "WeatherActivity");
                startActivity(intent);
                finish();
                break;
        }
    }

    public void nowweatherimginfo(String type, ImageView img) {
        switch (type) {
            case "晴":
                Glide.with(this).load(R.drawable.sunny_white).into(img);
                break;
            case "多云":
                Glide.with(this).load(R.drawable.cloudy_white).into(img);
                break;
            case "少云":
                Glide.with(this).load(R.drawable.few_clouds_white).into(img);
                break;
            case "晴间多云":
                Glide.with(this).load(R.drawable.partly_cloudy_white).into(img);
                break;
            case "阴":
                Glide.with(this).load(R.drawable.overcast_white).into(img);
                break;
            case "有风":
                Glide.with(this).load(R.drawable.windy_white).into(img);
                break;
            case "平静" :
                Glide.with(this).load(R.drawable.calm_white).into(img);
                break;
            case "微风":
                Glide.with(this).load(R.drawable.light_breeze_white).into(img);
                break;
            case "和风":
                Glide.with(this).load(R.drawable.moderate_white).into(img);
                break;
            case "清风":
                Glide.with(this).load(R.drawable.fresh_breeze_white).into(img);
                break;
            case "强风":
                Glide.with(this).load(R.drawable.strong_breeze_white).into(img);
                break;
            case "疾风":
                Glide.with(this).load(R.drawable.high_wind_white).into(img);
                break;
            case "大风":
                Glide.with(this).load(R.drawable.gale_white).into(img);
                break;
            case "烈风":
                Glide.with(this).load(R.drawable.strong_gale_white).into(img);
                break;
            case "风暴":
                Glide.with(this).load(R.drawable.storm_white).into(img);
                break;
            case "狂爆风":
                Glide.with(this).load(R.drawable.violent_storm_white).into(img);
                break;
            case "飓风":
                Glide.with(this).load(R.drawable.hurricane_white).into(img);
                break;
            case "龙卷风":
                Glide.with(this).load(R.drawable.tornado_white).into(img);
                break;
            case "热带风暴":
                Glide.with(this).load(R.drawable.tropical_storm_white).into(img);
                break;
            case "阵雨":
                Glide.with(this).load(R.drawable.shower_rain_white).into(img);
                break;
            case "强阵雨":
                Glide.with(this).load(R.drawable.heavy_shower_rain_white).into(img);
                break;
            case "雷阵雨":
                Glide.with(this).load(R.drawable.thundershower_white).into(img);
                break;
            case "强雷阵雨":
                Glide.with(this).load(R.drawable.heavy_thunderstorm_white).into(img);
                break;
            case "雷阵雨伴有冰雹":
                Glide.with(this).load(R.drawable.hail_white).into(img);
                break;
            case "小雨":
                Glide.with(this).load(R.drawable.light_rain_white).into(img);
                break;
            case "中雨":
                Glide.with(this).load(R.drawable.moderate_rain_white).into(img);
                break;
            case "大雨":
                Glide.with(this).load(R.drawable.heavy_rain_white).into(img);
                break;
            case "极端降雨":
                Glide.with(this).load(R.drawable.extreme_rain_white).into(img);
                break;
            case "细雨":
                Glide.with(this).load(R.drawable.drizzle_rain_white).into(img);
                break;
            case "暴雨":
                Glide.with(this).load(R.drawable.storm1_white).into(img);
                break;
            case "大暴雨":
                Glide.with(this).load(R.drawable.heavy_storm_white).into(img);
                break;
            case "特大暴雨":
                Glide.with(this).load(R.drawable.severe_storm_white).into(img);
                break;
            case "冻雨":
                Glide.with(this).load(R.drawable.freezing_rain_white).into(img);
                break;
            case "小雪":
                Glide.with(this).load(R.drawable.light_snow_white).into(img);
                break;
            case "中雪":
                Glide.with(this).load(R.drawable.moderate_snow_white).into(img);
                break;
            case "大雪":
                Glide.with(this).load(R.drawable.heavy_snow_white).into(img);
                break;
            case "暴雪":
                Glide.with(this).load(R.drawable.snowstorm_white).into(img);
                break;
            case "雨夹雪":
                Glide.with(this).load(R.drawable.sleet_white).into(img);
                break;
            case "雨雪天气":
                Glide.with(this).load(R.drawable.rain_and_snow_white).into(img);
                break;
            case "阵雨夹雪":
                Glide.with(this).load(R.drawable.shower_snow_white).into(img);
                break;
            case "阵雪":
                Glide.with(this).load(R.drawable.snow_flurry_white).into(img);
                break;
            case "薄雾":
                Glide.with(this).load(R.drawable.mist_white).into(img);
                break;
            case "雾":
                Glide.with(this).load(R.drawable.foggy_white).into(img);
                break;
            case "霾":
                Glide.with(this).load(R.drawable.haze_white).into(img);
                break;
            case "扬沙":
                Glide.with(this).load(R.drawable.sand_white).into(img);
                break;
            case "浮尘":
                Glide.with(this).load(R.drawable.dust_white).into(img);
                break;
            case "沙尘暴":
                Glide.with(this).load(R.drawable.duststorm_white).into(img);
                break;
            case "强沙尘暴":
                Glide.with(this).load(R.drawable.sandstorm_white).into(img);
                break;
            case "热":
                Glide.with(this).load(R.drawable.hot_white).into(img);
                break;
            case "冷":
                Glide.with(this).load(R.drawable.cold_white).into(img);
                break;
            case "未知":
                Glide.with(this).load(R.drawable.unknown_white).into(img);
                break;
            default:break;
        }
    }

    public void weatherimginfo(String type, ImageView img) {
        switch (type) {
            case "晴":
                Glide.with(this).load(R.drawable.sunny_black).into(img);
                break;
            case "多云":
                Glide.with(this).load(R.drawable.cloudy_black).into(img);
                break;
            case "少云":
                Glide.with(this).load(R.drawable.few_clouds_black).into(img);
                break;
            case "晴间多云":
                Glide.with(this).load(R.drawable.partly_cloudy_black).into(img);
                break;
            case "阴":
                Glide.with(this).load(R.drawable.overcast_black).into(img);
                break;
            case "有风":
                Glide.with(this).load(R.drawable.windy_black).into(img);
                break;
            case "平静" :
                Glide.with(this).load(R.drawable.calm_black).into(img);
                break;
            case "微风":
                Glide.with(this).load(R.drawable.light_breeze_black).into(img);
                break;
            case "和风":
                Glide.with(this).load(R.drawable.moderate_black).into(img);
                break;
            case "清风":
                Glide.with(this).load(R.drawable.fresh_breeze_black).into(img);
                break;
            case "强风":
                Glide.with(this).load(R.drawable.strong_breeze_black).into(img);
                break;
            case "疾风":
                Glide.with(this).load(R.drawable.high_wind_black).into(img);
                break;
            case "大风":
                Glide.with(this).load(R.drawable.gale_black).into(img);
                break;
            case "烈风":
                Glide.with(this).load(R.drawable.strong_gale_black).into(img);
                break;
            case "风暴":
                Glide.with(this).load(R.drawable.storm_black).into(img);
                break;
            case "狂爆风":
                Glide.with(this).load(R.drawable.violent_storm_black).into(img);
                break;
            case "飓风":
                Glide.with(this).load(R.drawable.hurricane_black).into(img);
                break;
            case "龙卷风":
                Glide.with(this).load(R.drawable.tornado_black).into(img);
                break;
            case "热带风暴":
                Glide.with(this).load(R.drawable.tropical_storm_black).into(img);
                break;
            case "阵雨":
                Glide.with(this).load(R.drawable.shower_rain_black).into(img);
                break;
            case "强阵雨":
                Glide.with(this).load(R.drawable.heavy_shower_rain_black).into(img);
                break;
            case "雷阵雨":
                Glide.with(this).load(R.drawable.thundershower_black).into(img);
                break;
            case "强雷阵雨":
                Glide.with(this).load(R.drawable.heavy_thunderstorm_black).into(img);
                break;
            case "雷阵雨伴有冰雹":
                Glide.with(this).load(R.drawable.hail_black).into(img);
                break;
            case "小雨":
                Glide.with(this).load(R.drawable.light_rain_black).into(img);
                break;
            case "中雨":
                Glide.with(this).load(R.drawable.moderate_rain_black).into(img);
                break;
            case "大雨":
                Glide.with(this).load(R.drawable.heavy_rain_black).into(img);
                break;
            case "极端降雨":
                Glide.with(this).load(R.drawable.extreme_rain_black).into(img);
                break;
            case "细雨":
                Glide.with(this).load(R.drawable.drizzle_rain_black).into(img);
                break;
            case "暴雨":
                Glide.with(this).load(R.drawable.storm1_black).into(img);
                break;
            case "大暴雨":
                Glide.with(this).load(R.drawable.heavy_storm_black).into(img);
                break;
            case "特大暴雨":
                Glide.with(this).load(R.drawable.severe_storm_black).into(img);
                break;
            case "冻雨":
                Glide.with(this).load(R.drawable.freezing_rain_black).into(img);
                break;
            case "小雪":
                Glide.with(this).load(R.drawable.light_snow_black).into(img);
                break;
            case "中雪":
                Glide.with(this).load(R.drawable.moderate_snow_black).into(img);
                break;
            case "大雪":
                Glide.with(this).load(R.drawable.heavy_snow_black).into(img);
                break;
            case "暴雪":
                Glide.with(this).load(R.drawable.snowstorm_black).into(img);
                break;
            case "雨夹雪":
                Glide.with(this).load(R.drawable.sleet_black).into(img);
                break;
            case "雨雪天气":
                Glide.with(this).load(R.drawable.rain_and_snow_black).into(img);
                break;
            case "阵雨夹雪":
                Glide.with(this).load(R.drawable.shower_snow_black).into(img);
                break;
            case "阵雪":
                Glide.with(this).load(R.drawable.snow_flurry_black).into(img);
                break;
            case "薄雾":
                Glide.with(this).load(R.drawable.mist_black).into(img);
                break;
            case "雾":
                Glide.with(this).load(R.drawable.foggy_black).into(img);
                break;
            case "霾":
                Glide.with(this).load(R.drawable.haze_black).into(img);
                break;
            case "扬沙":
                Glide.with(this).load(R.drawable.sand_black).into(img);
                break;
            case "浮尘":
                Glide.with(this).load(R.drawable.dust_black).into(img);
                break;
            case "沙尘暴":
                Glide.with(this).load(R.drawable.duststorm_black).into(img);
                break;
            case "强沙尘暴":
                Glide.with(this).load(R.drawable.sandstorm_black).into(img);
                break;
            case "热":
                Glide.with(this).load(R.drawable.hot_black).into(img);
                break;
            case "冷":
                Glide.with(this).load(R.drawable.cold_black).into(img);
                break;
            case "未知":
                Glide.with(this).load(R.drawable.unknown_black).into(img);
                break;
            default:break;
        }
    }

}
