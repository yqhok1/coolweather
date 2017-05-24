package com.example.yqhok.coolweather;

import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivityWeatherBinding;

public class WeatherActivity extends BaseActivity<ActivityWeatherBinding> {

    private ScrollView weatherLayout;

    private TextView titleCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
    }
}
