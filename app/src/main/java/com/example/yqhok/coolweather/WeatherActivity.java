package com.example.yqhok.coolweather;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivityWeatherBinding;
import com.example.yqhok.coolweather.gson.Weather;

public class WeatherActivity extends BaseActivity<ActivityWeatherBinding> {

    private ScrollView weatherLayout;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        initView();
        initData();
    }

    private void initView() {
        weatherLayout = bindingView.weatherLayout;
        degreeText = bindingView.now.degreeText;
    }

    private void initData() {

    }

    public void requestWeather(final String weatherId) {

    }

    private void showWeatherInfo(Weather weather) {

    }

}
