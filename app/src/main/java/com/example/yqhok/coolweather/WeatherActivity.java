package com.example.yqhok.coolweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.example.yqhok.coolweather.application.MyApplication;
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
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends BaseActivity<ActivityWeatherBinding> implements SwipeRefreshLayout.OnRefreshListener, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    private Intent intent;

    private Toolbar toolbar;
    private ScrollView weatherLayout;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView nowFl;
    private TextView nowHum;
    private TextView nowPcpn;
    private TextView nowPres;
    private TextView nowVis;
    private TextView nowDir;
    private TextView nowSc;
    private TextView nowSpd;
    private LinearLayout hourInfoLayout;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfBrf;
    private TextView comfTxt;
    private TextView cwBrf;
    private TextView cwTxt;
    private TextView drsgBrf;
    private TextView drsgTxt;
    private TextView fluBrf;
    private TextView fluTxt;
    private TextView sportBrf;
    private TextView sportTxt;
    private TextView travBrf;
    private TextView travTxt;
    private TextView uvBrf;
    private TextView uvTxt;
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private FloatingActionButton fab;

    private ItemHourInfoItemBinding itemHourInfoItemBinding;
    private ForecastItemBinding forecastItemBinding;

    private String mWeatherId;

    private ImageView weatherImg;
    private TextView updateTime;

    private static boolean isNetworkAvailable;

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
        initNetReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initWeather();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
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
            while (!task.isFinished) ;
        }
    }

    private void initView() {
        intent = getIntent();
        toolbar = getToolBar();
        weatherLayout = bindingView.weatherLayout;
        degreeText = bindingView.now.degreeText;
        weatherInfoText = bindingView.now.weatherInfoText;
        nowFl = (TextView) findViewById(R.id.now_fl);
        nowHum = (TextView) findViewById(R.id.now_hum);
        nowPcpn = (TextView) findViewById(R.id.now_pcpn);
        nowPres = (TextView) findViewById(R.id.now_pres);
        nowVis = (TextView) findViewById(R.id.now_vis);
        nowDir = (TextView) findViewById(R.id.now_dir);
        nowSc = (TextView) findViewById(R.id.now_sc);
        nowSpd = (TextView) findViewById(R.id.now_spd);
        hourInfoLayout = bindingView.itemhourinfo.hourinfoLayout;
        forecastLayout = bindingView.forecast.forecastLayout;
        aqiText = bindingView.aqi.aqiText;
        pm25Text = bindingView.aqi.pm25Text;
        comfBrf = bindingView.suggestion.suggestionComfBrf;
        comfTxt = bindingView.suggestion.suggestionComfTxt;
        cwBrf = bindingView.suggestion.suggestionCwBrf;
        cwTxt = bindingView.suggestion.suggestionCwTxt;
        drsgBrf = bindingView.suggestion.suggestionDrsgBrf;
        drsgTxt = bindingView.suggestion.suggestionDrsgTxt;
        fluBrf = bindingView.suggestion.suggestionFluBrf;
        fluTxt = bindingView.suggestion.suggestionFluTxt;
        sportBrf = bindingView.suggestion.suggestionSportBrf;
        sportTxt = bindingView.suggestion.suggestionSportTxt;
        travBrf = bindingView.suggestion.suggestionTravBrf;
        travTxt = bindingView.suggestion.suggestionTravTxt;
        uvBrf = bindingView.suggestion.suggestionUvBrf;
        uvTxt = bindingView.suggestion.suggestionUvTxt;
        swipeRefresh = bindingView.swipeRefresh;
        drawerLayout = bindingView.drawerLayout;
        navigationView = bindingView.navigationView;
        fab = bindingView.fab;
        weatherImg = bindingView.now.weatherImg;
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
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeResources(R.color.Blue);
        fab.setOnClickListener(this);
        fab.setBackgroundResource(android.R.color.transparent);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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
        List<WeatherInfo> weatherList = DataSupport.where("isCurrent = ?", "0").find(WeatherInfo.class);
        if (weather != null) {
            navigationView.getMenu().clear();
            navigationView.getMenu().add(R.id.choose_city, 0, 0, weather.getCityName()).setIcon(R.drawable.city);
            for (int i = 1; i < weatherList.size() + 1; i++) {
                navigationView.getMenu().add(R.id.choose_city, i, i, weatherList.get(i-1).getCityName()).setIcon(R.drawable.city);
            }
        }
    }

    private void initWeather() {
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
        initMenu();
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
                        } else {
                            currentWeather.setIsCurrent(false);
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
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
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
        String upDatetime = "上次更新时间：" + weather.basic.update.updateTime.split(" ")[1];
        updateTime.setText(upDatetime);
        toolbar.setTitle(cityName);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        nowFl.setText("体感温度：" + weather.now.fl + "℃");
        nowHum.setText("相对湿度：" + weather.now.hum + "%");
        nowPcpn.setText("降水量：" + weather.now.hum + "mm");
        nowPres.setText("气压：" + weather.now.pres);
        nowVis.setText("能见度：" + weather.now.vis + "km");
        nowDir.setText("风向：" + weather.now.wind1.dir);
        nowSc.setText("风力：" + weather.now.wind1.sc);
        nowSpd.setText("风速：" + weather.now.wind1.spd + "kmph");
        hourInfoLayout.removeAllViews();
        for (Hourly hourly : weather.hourlyList) {
            itemHourInfoItemBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.item_hour_info_item, hourInfoLayout, false);
            TextView hourDateText = itemHourInfoItemBinding.hourDataText;
            ImageView hourWeatherImg = itemHourInfoItemBinding.hourWeatherImg;
            TextView hourInfoText = itemHourInfoItemBinding.hourInfoText;
            TextView hourHum = itemHourInfoItemBinding.hourHum;
            TextView hourPop = itemHourInfoItemBinding.hourPop;
            TextView hourPres = itemHourInfoItemBinding.hourPres;
            TextView hourTemp = itemHourInfoItemBinding.hourTemp;
            TextView hourDir = itemHourInfoItemBinding.hourDir;
            TextView hourSpd = itemHourInfoItemBinding.hourSpd;
            hourDateText.setText("时间：" + hourly.date);
            hourInfoText.setText(hourly.more1.info);
            weatherImgInfo(hourly.more1.info, hourWeatherImg);
            hourHum.setText("相对湿度：" + hourly.hum + "%");
            hourPop.setText("降水概率：" + hourly.pop + "%");
            hourPres.setText("气压：" + hourly.pres);
            hourTemp.setText("温度：" + hourly.tmp + "℃");
            hourDir.setText("风力：" + hourly.wind.dir + " " + hourly.wind.sc + "级");
            hourSpd.setText("风速：" + hourly.wind.spd + "kmph");
            hourInfoLayout.addView(itemHourInfoItemBinding.getRoot());
        }
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            forecastItemBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.forecast_item, forecastLayout, false);
            TextView dateText = forecastItemBinding.dataText;
            TextView infoText = forecastItemBinding.infoText;
            TextView maxText = forecastItemBinding.maxText;
            TextView minText = forecastItemBinding.minText;
            ImageView smallWeatherImg = forecastItemBinding.weatherImg;
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            weatherImgInfo(forecast.more.info, smallWeatherImg);
            maxText.setText(forecast.temperature.max + "℃");
            minText.setText(forecast.temperature.min + "℃");
            forecastLayout.addView(forecastItemBinding.getRoot());
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        comfBrf.setText("舒适度指数——" + weather.suggestion.comfort.brf);
        comfTxt.setText(weather.suggestion.comfort.info);
        cwBrf.setText("洗车指数——" + weather.suggestion.carwash.brf);
        cwTxt.setText(weather.suggestion.carwash.info);
        drsgBrf.setText("穿衣指数——" + weather.suggestion.dress.brf);
        drsgTxt.setText(weather.suggestion.dress.info);
        fluBrf.setText("感冒指数——" + weather.suggestion.flu.brf);
        fluTxt.setText(weather.suggestion.flu.info);
        sportBrf.setText("运动指数——" + weather.suggestion.sport.brf);
        sportTxt.setText(weather.suggestion.sport.info);
        travBrf.setText("旅游指数——" + weather.suggestion.travel.brf);
        travTxt.setText(weather.suggestion.travel.info);
        uvBrf.setText("紫外线指数——" + weather.suggestion.uv.brf);
        uvTxt.setText(weather.suggestion.uv.info);
        hourInfoLayout.setVisibility(View.VISIBLE);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
        nowWeatherImgInfo(weatherInfo, weatherImg);
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

    private void initNetReceiver() {
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
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
                swipeRefresh.setRefreshing(true);
                if (DataSupport.isExist(WeatherInfo.class)) {
                    WeatherInfo weather = DataSupport.where("cityName = ?", item.getTitle().toString()).findFirst(WeatherInfo.class);
                    mWeatherId = weather.getWeatherId();
                    navigationView.getMenu().clear();
                    initMenu();
                    onRefresh();
                }
                break;
        }
        return true;
    }

//    @Override
//    public boolean onLongClick(View v) {
//        NavigationMenuItemView itemView = (NavigationMenuItemView) navigationView.findViewById(v.getId());
//        DataSupport.deleteAll(WeatherInfo.class, "cityName = ?", itemView.getItemData().getTitle().toString());
//        return true;
//    }

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


    public void nowWeatherImgInfo(String type, ImageView img) {
        List<Integer> list = new ArrayList<>();
        list.add(R.drawable.sunny_white);
        list.add(R.drawable.cloudy_white);
        list.add(R.drawable.few_clouds_white);
        list.add(R.drawable.partly_cloudy_white);
        list.add(R.drawable.overcast_white);
        list.add(R.drawable.windy_white);
        list.add(R.drawable.calm_white);
        list.add(R.drawable.light_breeze_white);
        list.add(R.drawable.moderate_white);
        list.add(R.drawable.fresh_breeze_white);
        list.add(R.drawable.strong_breeze_white);
        list.add(R.drawable.high_wind_white);
        list.add(R.drawable.gale_white);
        list.add(R.drawable.strong_gale_white);
        list.add(R.drawable.storm_white);
        list.add(R.drawable.violent_storm_white);
        list.add(R.drawable.hurricane_white);
        list.add(R.drawable.tornado_white);
        list.add(R.drawable.tropical_storm_white);
        list.add(R.drawable.shower_rain_white);
        list.add(R.drawable.heavy_shower_rain_white);
        list.add(R.drawable.thundershower_white);
        list.add(R.drawable.heavy_thunderstorm_white);
        list.add(R.drawable.hail_white);
        list.add(R.drawable.light_rain_white);
        list.add(R.drawable.moderate_rain_white);
        list.add(R.drawable.heavy_rain_white);
        list.add(R.drawable.extreme_rain_white);
        list.add(R.drawable.drizzle_rain_white);
        list.add(R.drawable.storm1_white);
        list.add(R.drawable.heavy_storm_white);
        list.add(R.drawable.severe_storm_white);
        list.add(R.drawable.freezing_rain_white);
        list.add(R.drawable.light_snow_white);
        list.add(R.drawable.moderate_snow_white);
        list.add(R.drawable.heavy_snow_white);
        list.add(R.drawable.snowstorm_white);
        list.add(R.drawable.sleet_white);
        list.add(R.drawable.rain_and_snow_white);
        list.add(R.drawable.shower_snow_white);
        list.add(R.drawable.snow_flurry_white);
        list.add(R.drawable.mist_white);
        list.add(R.drawable.foggy_white);
        list.add(R.drawable.haze_white);
        list.add(R.drawable.sand_white);
        list.add(R.drawable.dust_white);
        list.add(R.drawable.duststorm_white);
        list.add(R.drawable.sandstorm_white);
        list.add(R.drawable.hot_white);
        list.add(R.drawable.cold_white);
        list.add(R.drawable.unknown_white);
        switch (type) {
            case "晴":
                Glide.with(this).load(list.get(0)).into(img);
                break;
            case "多云":
                Glide.with(this).load(list.get(1)).into(img);
                break;
            case "少云":
                Glide.with(this).load(list.get(2)).into(img);
                break;
            case "晴间多云":
                Glide.with(this).load(list.get(3)).into(img);
                break;
            case "阴":
                Glide.with(this).load(list.get(4)).into(img);
                break;
            case "有风":
                Glide.with(this).load(list.get(5)).into(img);
                break;
            case "平静":
                Glide.with(this).load(list.get(6)).into(img);
                break;
            case "微风":
                Glide.with(this).load(list.get(7)).into(img);
                break;
            case "和风":
                Glide.with(this).load(list.get(8)).into(img);
                break;
            case "清风":
                Glide.with(this).load(list.get(9)).into(img);
                break;
            case "强风":
                Glide.with(this).load(list.get(10)).into(img);
                break;
            case "疾风":
                Glide.with(this).load(list.get(11)).into(img);
                break;
            case "大风":
                Glide.with(this).load(list.get(12)).into(img);
                break;
            case "烈风":
                Glide.with(this).load(list.get(13)).into(img);
                break;
            case "风暴":
                Glide.with(this).load(list.get(14)).into(img);
                break;
            case "狂爆风":
                Glide.with(this).load(list.get(15)).into(img);
                break;
            case "飓风":
                Glide.with(this).load(list.get(16)).into(img);
                break;
            case "龙卷风":
                Glide.with(this).load(list.get(17)).into(img);
                break;
            case "热带风暴":
                Glide.with(this).load(list.get(18)).into(img);
                break;
            case "阵雨":
                Glide.with(this).load(list.get(19)).into(img);
                break;
            case "强阵雨":
                Glide.with(this).load(list.get(20)).into(img);
                break;
            case "雷阵雨":
                Glide.with(this).load(list.get(21)).into(img);
                break;
            case "强雷阵雨":
                Glide.with(this).load(list.get(22)).into(img);
                break;
            case "雷阵雨伴有冰雹":
                Glide.with(this).load(list.get(23)).into(img);
                break;
            case "小雨":
                Glide.with(this).load(list.get(24)).into(img);
                break;
            case "中雨":
                Glide.with(this).load(list.get(25)).into(img);
                break;
            case "大雨":
                Glide.with(this).load(list.get(26)).into(img);
                break;
            case "极端降雨":
                Glide.with(this).load(list.get(27)).into(img);
                break;
            case "细雨":
                Glide.with(this).load(list.get(28)).into(img);
                break;
            case "暴雨":
                Glide.with(this).load(list.get(29)).into(img);
                break;
            case "大暴雨":
                Glide.with(this).load(list.get(30)).into(img);
                break;
            case "特大暴雨":
                Glide.with(this).load(list.get(31)).into(img);
                break;
            case "冻雨":
                Glide.with(this).load(list.get(32)).into(img);
                break;
            case "小雪":
                Glide.with(this).load(list.get(33)).into(img);
                break;
            case "中雪":
                Glide.with(this).load(list.get(34)).into(img);
                break;
            case "大雪":
                Glide.with(this).load(list.get(35)).into(img);
                break;
            case "暴雪":
                Glide.with(this).load(list.get(36)).into(img);
                break;
            case "雨夹雪":
                Glide.with(this).load(list.get(37)).into(img);
                break;
            case "雨雪天气":
                Glide.with(this).load(list.get(38)).into(img);
                break;
            case "阵雨夹雪":
                Glide.with(this).load(list.get(39)).into(img);
                break;
            case "阵雪":
                Glide.with(this).load(list.get(40)).into(img);
                break;
            case "薄雾":
                Glide.with(this).load(list.get(41)).into(img);
                break;
            case "雾":
                Glide.with(this).load(list.get(42)).into(img);
                break;
            case "霾":
                Glide.with(this).load(list.get(43)).into(img);
                break;
            case "扬沙":
                Glide.with(this).load(list.get(44)).into(img);
                break;
            case "浮尘":
                Glide.with(this).load(list.get(45)).into(img);
                break;
            case "沙尘暴":
                Glide.with(this).load(list.get(46)).into(img);
                break;
            case "强沙尘暴":
                Glide.with(this).load(list.get(47)).into(img);
                break;
            case "热":
                Glide.with(this).load(list.get(48)).into(img);
                break;
            case "冷":
                Glide.with(this).load(list.get(49)).into(img);
                break;
            case "未知":
                Glide.with(this).load(list.get(50)).into(img);
                break;
            default:
                break;
        }
    }

    public void weatherImgInfo(String type, ImageView img) {
        List<Integer> list = new ArrayList<>();
        list.add(R.drawable.sunny_black);
        list.add(R.drawable.cloudy_black);
        list.add(R.drawable.few_clouds_black);
        list.add(R.drawable.partly_cloudy_black);
        list.add(R.drawable.overcast_black);
        list.add(R.drawable.windy_black);
        list.add(R.drawable.calm_black);
        list.add(R.drawable.light_breeze_black);
        list.add(R.drawable.moderate_black);
        list.add(R.drawable.fresh_breeze_black);
        list.add(R.drawable.strong_breeze_black);
        list.add(R.drawable.high_wind_black);
        list.add(R.drawable.gale_black);
        list.add(R.drawable.strong_gale_black);
        list.add(R.drawable.storm_black);
        list.add(R.drawable.violent_storm_black);
        list.add(R.drawable.hurricane_black);
        list.add(R.drawable.tornado_black);
        list.add(R.drawable.tropical_storm_black);
        list.add(R.drawable.shower_rain_black);
        list.add(R.drawable.heavy_shower_rain_black);
        list.add(R.drawable.thundershower_black);
        list.add(R.drawable.heavy_thunderstorm_black);
        list.add(R.drawable.hail_black);
        list.add(R.drawable.light_rain_black);
        list.add(R.drawable.moderate_rain_black);
        list.add(R.drawable.heavy_rain_black);
        list.add(R.drawable.extreme_rain_black);
        list.add(R.drawable.drizzle_rain_black);
        list.add(R.drawable.storm1_black);
        list.add(R.drawable.heavy_storm_black);
        list.add(R.drawable.severe_storm_black);
        list.add(R.drawable.freezing_rain_black);
        list.add(R.drawable.light_snow_black);
        list.add(R.drawable.moderate_snow_black);
        list.add(R.drawable.heavy_snow_black);
        list.add(R.drawable.snowstorm_black);
        list.add(R.drawable.sleet_black);
        list.add(R.drawable.rain_and_snow_black);
        list.add(R.drawable.shower_snow_black);
        list.add(R.drawable.snow_flurry_black);
        list.add(R.drawable.mist_black);
        list.add(R.drawable.foggy_black);
        list.add(R.drawable.haze_black);
        list.add(R.drawable.sand_black);
        list.add(R.drawable.dust_black);
        list.add(R.drawable.duststorm_black);
        list.add(R.drawable.sandstorm_black);
        list.add(R.drawable.hot_black);
        list.add(R.drawable.cold_black);
        list.add(R.drawable.unknown_black);
        switch (type) {
            case "晴":
                Glide.with(this).load(list.get(0)).into(img);
                break;
            case "多云":
                Glide.with(this).load(list.get(1)).into(img);
                break;
            case "少云":
                Glide.with(this).load(list.get(2)).into(img);
                break;
            case "晴间多云":
                Glide.with(this).load(list.get(3)).into(img);
                break;
            case "阴":
                Glide.with(this).load(list.get(4)).into(img);
                break;
            case "有风":
                Glide.with(this).load(list.get(5)).into(img);
                break;
            case "平静":
                Glide.with(this).load(list.get(6)).into(img);
                break;
            case "微风":
                Glide.with(this).load(list.get(7)).into(img);
                break;
            case "和风":
                Glide.with(this).load(list.get(8)).into(img);
                break;
            case "清风":
                Glide.with(this).load(list.get(9)).into(img);
                break;
            case "强风":
                Glide.with(this).load(list.get(10)).into(img);
                break;
            case "疾风":
                Glide.with(this).load(list.get(11)).into(img);
                break;
            case "大风":
                Glide.with(this).load(list.get(12)).into(img);
                break;
            case "烈风":
                Glide.with(this).load(list.get(13)).into(img);
                break;
            case "风暴":
                Glide.with(this).load(list.get(14)).into(img);
                break;
            case "狂爆风":
                Glide.with(this).load(list.get(15)).into(img);
                break;
            case "飓风":
                Glide.with(this).load(list.get(16)).into(img);
                break;
            case "龙卷风":
                Glide.with(this).load(list.get(17)).into(img);
                break;
            case "热带风暴":
                Glide.with(this).load(list.get(18)).into(img);
                break;
            case "阵雨":
                Glide.with(this).load(list.get(19)).into(img);
                break;
            case "强阵雨":
                Glide.with(this).load(list.get(20)).into(img);
                break;
            case "雷阵雨":
                Glide.with(this).load(list.get(21)).into(img);
                break;
            case "强雷阵雨":
                Glide.with(this).load(list.get(22)).into(img);
                break;
            case "雷阵雨伴有冰雹":
                Glide.with(this).load(list.get(23)).into(img);
                break;
            case "小雨":
                Glide.with(this).load(list.get(24)).into(img);
                break;
            case "中雨":
                Glide.with(this).load(list.get(25)).into(img);
                break;
            case "大雨":
                Glide.with(this).load(list.get(26)).into(img);
                break;
            case "极端降雨":
                Glide.with(this).load(list.get(27)).into(img);
                break;
            case "毛毛雨/细雨":
                Glide.with(this).load(list.get(28)).into(img);
                break;
            case "暴雨":
                Glide.with(this).load(list.get(29)).into(img);
                break;
            case "大暴雨":
                Glide.with(this).load(list.get(30)).into(img);
                break;
            case "特大暴雨":
                Glide.with(this).load(list.get(31)).into(img);
                break;
            case "冻雨":
                Glide.with(this).load(list.get(32)).into(img);
                break;
            case "小雪":
                Glide.with(this).load(list.get(33)).into(img);
                break;
            case "中雪":
                Glide.with(this).load(list.get(34)).into(img);
                break;
            case "大雪":
                Glide.with(this).load(list.get(35)).into(img);
                break;
            case "暴雪":
                Glide.with(this).load(list.get(36)).into(img);
                break;
            case "雨夹雪":
                Glide.with(this).load(list.get(37)).into(img);
                break;
            case "雨雪天气":
                Glide.with(this).load(list.get(38)).into(img);
                break;
            case "阵雨夹雪":
                Glide.with(this).load(list.get(39)).into(img);
                break;
            case "阵雪":
                Glide.with(this).load(list.get(40)).into(img);
                break;
            case "薄雾":
                Glide.with(this).load(list.get(41)).into(img);
                break;
            case "雾":
                Glide.with(this).load(list.get(42)).into(img);
                break;
            case "霾":
                Glide.with(this).load(list.get(43)).into(img);
                break;
            case "扬沙":
                Glide.with(this).load(list.get(44)).into(img);
                break;
            case "浮尘":
                Glide.with(this).load(list.get(45)).into(img);
                break;
            case "沙尘暴":
                Glide.with(this).load(list.get(46)).into(img);
                break;
            case "强沙尘暴":
                Glide.with(this).load(list.get(47)).into(img);
                break;
            case "热":
                Glide.with(this).load(list.get(48)).into(img);
                break;
            case "冷":
                Glide.with(this).load(list.get(49)).into(img);
                break;
            case "未知":
                Glide.with(this).load(list.get(50)).into(img);
                break;
            default:
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
            } else {
                isNetworkAvailable = true;
                loadBingPic();
            }
        }

    }

}
