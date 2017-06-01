package com.example.yqhok.coolweather.gson;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kevin on 2017/5/31.
 */

public class Hourly implements Parcelable{
    @SerializedName("cond")
    public More1 more1;

    @SerializedName("date")
    public String date;

    @SerializedName("hum")
    public String hum;

    @SerializedName("pop")
    public String pop;

    @SerializedName("pres")
    public String pres;

    @SerializedName("tmp")
    public String tmp;

    @SerializedName("wind")
    public Wind wind;

    public class More1 {

        @SerializedName("txt")
        public String info;
    }

    public class Wind {

        @SerializedName("dir")
        public String dir;

        @SerializedName("sc")
        public String sc;

        @SerializedName("spd")
        public String spd;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flag) {
        dest.writeString(date);
        dest.writeString(hum);
        dest.writeString(pop);
        dest.writeString(pres);
        dest.writeString(tmp);
    }

    public static final Parcelable.Creator<Hourly> CREATOR = new Parcelable.Creator<Hourly>() {
        @Override
        public Hourly createFromParcel(Parcel source) {
            Hourly hourly = new Hourly();
            hourly.date = source.readString();
            hourly.hum = source.readString();
            hourly.pres = source.readString();
            hourly.pop = source.readString();
            hourly.tmp = source.readString();
            return hourly;
        }

        @Override
        public Hourly[] newArray(int size) {
            return new Hourly[size];
        }
    };
}
