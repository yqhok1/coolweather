package com.example.yqhok.coolweather.db;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

/**
 * Created by yqhok on 2017/5/21.
 */

public class County extends DataSupport implements Parcelable {

    private int id;
    private String countyName;
    private String weatherId;
    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(countyName);
        dest.writeString(weatherId);
        dest.writeInt(cityId);
    }

    public static final Parcelable.Creator<County> CREATOR = new Parcelable.Creator<County>() {
        @Override
        public County createFromParcel(Parcel source) {
            County county = new County();
            county.id = source.readInt();
            county.countyName = source.readString();
            county.weatherId = source.readString();
            county.cityId = source.readInt();
            return county;
        }

        @Override
        public County[] newArray(int size) {
            return new County[size];
        }
    };
}
