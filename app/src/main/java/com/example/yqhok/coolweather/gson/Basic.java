package com.example.yqhok.coolweather.gson;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yqhok on 2017/5/24.
 */

public class Basic implements Parcelable {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cityName);
        dest.writeString(weatherId);
    }

    public static final Parcelable.Creator<Basic> CREATOR = new Parcelable.Creator<Basic>() {
        @Override
        public Basic createFromParcel(Parcel source) {
            Basic basic = new Basic();
            basic.cityName = source.readString();
            basic.weatherId = source.readString();
            return basic;
        }

        @Override
        public Basic[] newArray(int size) {
            return new Basic[size];
        }
    };
}
