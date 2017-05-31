package com.example.yqhok.coolweather.util;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.example.yqhok.coolweather.application.MyApplication;
import com.example.yqhok.coolweather.db.City;
import com.example.yqhok.coolweather.db.County;
import com.example.yqhok.coolweather.db.Province;
import com.example.yqhok.coolweather.db.WeatherInfo;
import com.example.yqhok.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by yqhok on 2017/5/21.
 */

public class Utility {

    /**
     * Parse and dispose province data from server
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Parse and dispose city from server
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Parse and dispose county from server
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateUserData() {
        AVUser user = AVUser.getCurrentUser();
        if (user == null) {
            return;
        } else {
            List<WeatherInfo> weatherList = DataSupport.findAll(WeatherInfo.class);
            StringBuilder allCitiesId = new StringBuilder();
            for (WeatherInfo weather : weatherList) {
                allCitiesId.append(weather.getWeatherId() + " ");
                if (weather.getIsCurrent()) {
                    user.put("currentCityId", weather.getWeatherId());
                }
            }
            user.put("allCitiesId", allCitiesId.toString());
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static class LoadDataTask extends AsyncTask<Void, Void, Boolean> {

        public boolean isFinished = false;

        @Override
        protected Boolean doInBackground(Void... params) {
            initData();
            return isFinished;
        }

        private void requestWeather(final String weatherId, final boolean isCurrent) {
            String weatherURL = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=60e311f001ea4a7693e674f3671cda85";
            HttpUtil.sendOkHttpRequest(weatherURL, new Callback() {

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText = response.body().string();
                    final Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        WeatherInfo weatherInfo = new WeatherInfo();
                        weatherInfo.setWeatherId(weather.basic.weatherId);
                        weatherInfo.setCityName(weather.basic.cityName);
                        weatherInfo.setMin(weather.forecastList.get(0).temperature.min);
                        weatherInfo.setMax(weather.forecastList.get(0).temperature.max);
                        weatherInfo.setInfo(weather.now.more.info);
                        if (isCurrent) {
                            weatherInfo.setIsCurrent(true);
                            weatherInfo.save();
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            isFinished = true;
                        } else {
                            weatherInfo.save();
                        }
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

            });
        }

        private void initData() {
            AVUser user = AVUser.getCurrentUser();
            if (user == null || user.get("currentCityId") == null) {
                isFinished = true;
                return;
            } else {
                DataSupport.deleteAll(WeatherInfo.class);
                String currentCityId = user.getString("currentCityId");
                requestWeather(currentCityId, true);
                String s = user.getString("allCitiesId");
                String allCitiesIdList[] = s.split(" ");
                for (String cityId : allCitiesIdList) {
                    if (!cityId.equals(currentCityId))
                        requestWeather(cityId, false);
                }
            }
        }

    }

}
