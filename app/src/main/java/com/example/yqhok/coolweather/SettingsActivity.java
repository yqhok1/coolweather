package com.example.yqhok.coolweather;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.widget.TimePicker;

import com.example.yqhok.coolweather.application.MyApplication;
import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivitySettingsBinding;
import com.example.yqhok.coolweather.db.WeatherInfo;
import com.example.yqhok.coolweather.service.SettingTimeUpdateService;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BaseActivity<ActivitySettingsBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final SettingsFragment settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(R.id.settings_frame_layout, settingsFragment).commit();
        setTitle("设置");
        showContentView();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        private ListPreference chooseCurrentCityPref;
        private SwitchPreference weatherAlertsMorningPref;
        private SwitchPreference weatherAlertsNightPref;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
        }

        @Override
        public void onStart() {
            super.onStart();
            register();
        }


        @Override
        public void onStop() {
            super.onStop();
        }

        private void register() {
            chooseCurrentCityPref = (ListPreference) findPreference("pref_key_choose_current_city");
            chooseCurrentCityPref.setOnPreferenceChangeListener(this);
            weatherAlertsMorningPref = (SwitchPreference) findPreference("pref_key_weather_alerts_morning");
            weatherAlertsMorningPref.setOnPreferenceChangeListener(this);
            weatherAlertsMorningPref.setOnPreferenceClickListener(this);
            weatherAlertsNightPref = (SwitchPreference) findPreference("pref_key_weather_alerts_night");
            weatherAlertsNightPref.setOnPreferenceChangeListener(this);
            weatherAlertsNightPref.setOnPreferenceClickListener(this);
            List<WeatherInfo> weatherList = DataSupport.findAll(WeatherInfo.class);
            List<String> cityNameList = new ArrayList<>();
            for (WeatherInfo weather : weatherList) {
                cityNameList.add(weather.getCityName());
            }
            chooseCurrentCityPref.setEntries(cityNameList.toArray(new String[cityNameList.size()]));
            chooseCurrentCityPref.setEntryValues(cityNameList.toArray(new String[cityNameList.size()]));
            String s = DataSupport.where("isCurrent = ?", "1").findFirst(WeatherInfo.class).getCityName();
            if (s != null) {
                chooseCurrentCityPref.setSummary(s);
            }
            SharedPreferences preferences = MyApplication.getContext().getSharedPreferences("pref", MODE_PRIVATE);
            if (weatherAlertsMorningPref.isChecked()) {
                String time = preferences.getString("pref_key_weather_alerts_time_morning", null);
                if (time != null) {
                    weatherAlertsMorningPref.setSummaryOn(time);
                }
            }
            if (weatherAlertsNightPref.isChecked()) {
                String time = preferences.getString("pref_key_weather_alerts_time_night", null);
                if (time != null) {
                    weatherAlertsNightPref.setSummaryOn(time);
                }
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            SharedPreferences.Editor editor = MyApplication.getContext().getSharedPreferences("pref", MODE_PRIVATE).edit();
            switch (preference.getKey()) {
                case "pref_key_choose_current_city":
                    if (DataSupport.isExist(WeatherInfo.class)) {
                        List<WeatherInfo> weatherList = DataSupport.findAll(WeatherInfo.class);
                        for (WeatherInfo weatherInfo : weatherList) {
                            weatherInfo.setIsCurrent(false);
                            weatherInfo.save();
                        }
                    }
                    String cityName = chooseCurrentCityPref.getEntries()[chooseCurrentCityPref.findIndexOfValue(newValue.toString())].toString();
                    chooseCurrentCityPref.setSummary(cityName);
                    WeatherInfo weather = DataSupport.where("cityName = ?", cityName).findFirst(WeatherInfo.class);
                    if (weather != null) {
                        weather.setIsCurrent(true);
                        weather.save();
                    }
                    break;
                case "pref_key_weather_alerts_morning":
                    Boolean morningChecked = (Boolean) newValue;
                    if (morningChecked) {
                        editor.putBoolean("pref_key_weather_alerts_morning", true);
                    } else {
                        editor.putBoolean("pref_key_weather_alerts_morning", false);
                        Intent intent = new Intent(getActivity(), SettingTimeUpdateService.class);
                        getActivity().stopService(intent);
                    }
                    editor.apply();
                    break;
                case "pref_key_weather_alerts_night":
                    Boolean nightChecked = (Boolean) newValue;
                    if (nightChecked) {
                        editor.putBoolean("pref_key_weather_alerts_night", true);
                    } else {
                        editor.putBoolean("pref_key_weather_alerts_night", false);
                        Intent intent = new Intent(getActivity(), SettingTimeUpdateService.class);
                        getActivity().stopService(intent);
                    }
                    editor.apply();
                    break;
            }
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            final SharedPreferences.Editor editor = MyApplication.getContext().getSharedPreferences("pref", MODE_PRIVATE).edit();
            switch (preference.getKey()) {
                case "pref_key_weather_alerts_morning":
                    if (weatherAlertsMorningPref.isChecked()) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                StringBuilder time = new StringBuilder();
                                String strHourOfDay = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
                                String strMinuteOfDay = minute < 10 ? "0" + minute : "" + minute;
                                time.append(strHourOfDay + ":" + strMinuteOfDay);
                                editor.putString("pref_key_weather_alerts_time_morning", time.toString());
                                editor.apply();
                                weatherAlertsMorningPref.setSummaryOn(time);
                                Intent intent = new Intent(getActivity(), SettingTimeUpdateService.class);
                                getActivity().startService(intent);
                            }
                        }, 0, 0, true);
                        timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                weatherAlertsMorningPref.setChecked(false);
                                editor.putBoolean("pref_key_weather_alerts_morning", false);
                                editor.apply();
                            }
                        });
                        timePickerDialog.setMessage("请选择时间");
                        timePickerDialog.show();
                    }
                    break;
                case "pref_key_weather_alerts_night":
                    if (weatherAlertsNightPref.isChecked()) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                StringBuilder time = new StringBuilder();
                                String strHourOfDay = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
                                String strMinuteOfDay = minute < 10 ? "0" + minute : "" + minute;
                                time.append(strHourOfDay + ":" + strMinuteOfDay);
                                editor.putString("pref_key_weather_alerts_time_night", time.toString());
                                editor.apply();
                                weatherAlertsNightPref.setSummaryOn(time);
                                Intent intent = new Intent(getActivity(), SettingTimeUpdateService.class);
                                getActivity().startService(intent);
                            }
                        }, 0, 0, true);
                        timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                weatherAlertsNightPref.setChecked(false);
                                editor.putBoolean("pref_key_weather_alerts_night", false);
                                editor.apply();
                            }
                        });
                        timePickerDialog.setMessage("请选择时间");
                        timePickerDialog.show();
                    }
                    break;
            }
            return true;
        }
    }

}
