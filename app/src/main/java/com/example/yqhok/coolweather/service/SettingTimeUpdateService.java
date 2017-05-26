package com.example.yqhok.coolweather.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.example.yqhok.coolweather.R;
import com.example.yqhok.coolweather.WeatherActivity;

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
        java.util.Timer timer = new java.util.Timer(true);
        final Calendar nowtime = Calendar.getInstance();
        int year = nowtime.get(Calendar.YEAR);
        int month = nowtime.get(Calendar.MONTH);
        int day = nowtime.get(Calendar.DATE);
        int hour = 14;
        int minute = 50;
        Date date = new Date(year-1900, month, day, hour, minute);
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Intent i = new Intent(SettingTimeUpdateService.this, WeatherActivity.class);
                final PendingIntent pi = PendingIntent.getActivity(SettingTimeUpdateService.this, 0, i, 0);
                NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                Notification notification = new NotificationCompat.Builder(SettingTimeUpdateService.this)
                        .setContentTitle("Cool Weather")
                        .setContentText("有一条新的天气信息")
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();
                manager.notify(1, notification);
            }
        };
        timer.schedule(task, date);
        return super.onStartCommand(intent, flags, startId);
    }
}
