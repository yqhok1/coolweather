package com.example.yqhok.coolweather.gson;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yqhok on 2017/5/24.
 */

public class Now implements Parcelable{

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(temperature);
    }

    public static final Parcelable.Creator<Now> CREATOR = new Parcelable.Creator<Now>() {
        @Override
        public Now createFromParcel(Parcel source) {
            Now now = new Now();
            now.temperature = source.readString();
            return now;
        }

        @Override
        public Now[] newArray(int size) {
            return new Now[size];
        }
    };
}
