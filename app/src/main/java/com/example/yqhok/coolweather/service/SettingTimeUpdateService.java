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
        Intent i = new Intent(this, WeatherActivity.class);
        final PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        java.util.Timer timer= new java.util.Timer(true);//程序结束timer也结束
        final Calendar settime = Calendar.getInstance();
        final Calendar time = Calendar.getInstance();
        String s = "04:39";
        String hour = s.substring(0,2);
        String minute = s.substring(s.length()-2);
        int h = Integer.parseInt(hour);
        int m = Integer.parseInt(minute);
        settime.set(Calendar.HOUR_OF_DAY, h);
        settime.set(Calendar.MINUTE, m);
        settime.set(Calendar.SECOND,00);
        Date date = settime.getTime();
        if(date.before(new Date())){
            date = this.addDate(date,1);
        }
        final long Perioday = 24 * 60 * 60 * 1000;
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    Notification notification = new NotificationCompat.Builder(SettingTimeUpdateService.this)
                            .setContentTitle("CoolWeather")
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
        timer.schedule(task,date,Perioday);
        return super.onStartCommand(intent, flags, startId);
    }

    public Date addDate(Date date, int num) {
        Calendar startdt = Calendar.getInstance();
        startdt.setTime(date);
        startdt.add(Calendar.DAY_OF_MONTH, num);
        return startdt.getTime();
    }
}
