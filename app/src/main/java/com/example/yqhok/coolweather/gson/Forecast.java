package com.example.yqhok.coolweather.gson;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yqhok on 2017/5/24.
 */

public class Forecast implements Parcelable{

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature {

        public String max;

        public String min;

    }

    public class More {

        @SerializedName("txt_d")
        public String info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
    }

    public static final Parcelable.Creator<Forecast> CREATOR = new Parcelable.Creator<Forecast>() {
        @Override
        public Forecast createFromParcel(Parcel source) {
            Forecast forecast = new Forecast();
            forecast.date = source.readString();
            return forecast;
        }

        @Override
        public Forecast[] newArray(int size) {
            return new Forecast[size];
        }
    };
}
