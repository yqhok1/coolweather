package com.example.yqhok.coolweather;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.widget.TimePicker;

import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.databinding.ActivitySettingsBinding;
import com.example.yqhok.coolweather.service.SettingTimeUpdateService;

public class SettingsActivity extends BaseActivity<ActivitySettingsBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final SettingsFragment settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(R.id.settings_frame_layout, settingsFragment).commit();
        setTitle("设置");
        initView();
        showContentView();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    private void initView() {

    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

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
            weatherAlertsMorningPref = (SwitchPreference) findPreference("pref_key_weather_alerts_morning");
            weatherAlertsMorningPref.setOnPreferenceChangeListener(this);
            weatherAlertsMorningPref.setOnPreferenceClickListener(this);
            weatherAlertsNightPref = (SwitchPreference) findPreference("pref_key_weather_alerts_night");
            weatherAlertsNightPref.setOnPreferenceChangeListener(this);
            weatherAlertsNightPref.setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            SharedPreferences.Editor editor = MyApplication.getContext().getSharedPreferences("pref", MODE_PRIVATE).edit();
            switch (preference.getKey()) {
                case "pref_key_weather_alerts_morning":
                    if (weatherAlertsMorningPref.isChecked()) {
                        editor.putBoolean("pref_key_weather_alerts_morning", true);
                    } else {
                        editor.putBoolean("pref_key_weather_alerts_morning", false);
                        Intent intent = new Intent(getActivity(), SettingTimeUpdateService.class);
                        getActivity().stopService(intent);
                    }
                    break;
                case "pref_key_weather_alerts_night":
                    if (weatherAlertsNightPref.isChecked()) {
                        editor.putBoolean("pref_key_weather_alerts_night", true);
                    } else {
                        editor.putBoolean("pref_key_weather_alerts_night", false);
                        Intent intent = new Intent(getActivity(), SettingTimeUpdateService.class);
                        getActivity().stopService(intent);
                    }
                    break;
            }
            editor.apply();
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
                                editor.putString("pref_key_weather_alters_time_morning", time.toString());
                                weatherAlertsMorningPref.setSummaryOn(time);
                                Intent intent = new Intent(getActivity(), SettingTimeUpdateService.class);
                                intent.putExtra("time", time.toString());
                                getActivity().startService(intent);
                            }
                        }, 0, 0, true);
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
                                editor.putString("pref_key_weather_alters_time_night", time.toString());
                                weatherAlertsMorningPref.setSummaryOn(time);
                                Intent intent = new Intent(getActivity(), SettingTimeUpdateService.class);
                                intent.putExtra("time", time.toString());
                                getActivity().startService(intent);
                            }
                        }, 0, 0, true);
                        timePickerDialog.show();
                    }
                    break;
            }
            editor.apply();
            return true;
        }
    }

}
