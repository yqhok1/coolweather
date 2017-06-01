package com.example.yqhok.coolweather.gson;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yqhok on 2017/5/24.
 */

public class Now implements Parcelable {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    @SerializedName("fl")
    public String fl;

    @SerializedName("hum")
    public String hum;

    @SerializedName("pcpn")
    public String pcpn;

    @SerializedName("pres")
    public String pres;

    @SerializedName("vis")
    public String vis;

    @SerializedName("wind")
    public Wind1 wind1;

    public class More {

        @SerializedName("txt")
        public String info;

    }

    public class Wind1 {

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
