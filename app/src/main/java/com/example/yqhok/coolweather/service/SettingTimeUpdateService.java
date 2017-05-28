package com.example.yqhok.coolweather.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.example.yqhok.coolweather.R;
import com.example.yqhok.coolweather.WeatherActivity;
import com.example.yqhok.coolweather.db.WeatherInfo;

import org.litepal.crud.DataSupport;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

public class SettingTimeUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        SharedPreferences preferences = getSharedPreferences("pref", MODE_PRIVATE);
        Boolean prefKeyWeatherAlertsMorning = preferences.getBoolean("pref_key_weather_alerts_morning", false);
        Boolean prefKeyWeatherAlertsNight = preferences.getBoolean("pref_key_weather_alerts_night", false);
        if (prefKeyWeatherAlertsMorning) {
            String morningTime = preferences.getString("pref_key_weather_alerts_time_morning", null);
            setTimer(morningTime, 1);
        }
        if (prefKeyWeatherAlertsNight) {
            String nightTime = preferences.getString("pref_key_weather_alerts_time_night", null);
            setTimer(nightTime, 2);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void setTimer(String setTime, final int id) {
        if (setTime == null) {
            return;
        }
        Intent i = new Intent(this, WeatherActivity.class);
        final PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        final Calendar time = Calendar.getInstance();
        String hour = setTime.substring(0, 2);
        String minute = setTime.substring(setTime.length()-2);
        int h = Integer.parseInt(hour);
        int m = Integer.parseInt(minute);
        time.set(Calendar.HOUR_OF_DAY, h);
        time.set(Calendar.MINUTE, m);
        time.set(Calendar.SECOND, 00);
        Date date = time.getTime();
        if(date.before(new Date())){
            date = this.addDate(date, 1);
        }
        final WeatherInfo weather = DataSupport.where("isCurrent = ?", "true").findFirst(WeatherInfo.class);
        final long Period = 24 * 60 * 60 * 1000;
        java.util.Timer timer = new java.util.Timer(true);
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification notification = new NotificationCompat.Builder(SettingTimeUpdateService.this)
                        .setContentTitle("CoolWeather")
                        .setContentText(weather.getCityName() + ":" + weather.getInfo() + " 最低" + weather.getMin() + "℃,最高" + weather.getMax() + "℃")
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();
                manager.notify(id, notification);
            }
        };
        timer.schedule(task, date, Period);
    }

    private Date addDate(Date date, int num) {
        Calendar startDay = Calendar.getInstance();
        startDay.setTime(date);
        startDay.add(Calendar.DAY_OF_MONTH, num);
        return startDay.getTime();
    }

}
