package com.example.yqhok.coolweather.db;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

/**
 * Created by yqhok on 2017/5/21.
 */

public class City extends DataSupport implements Parcelable{

    private int id;
    private String cityName;
    private int cityCode;
    private int provinceId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(cityName);
        dest.writeInt(cityCode);
        dest.writeInt(provinceId);
    }

    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {
        @Override
        public City createFromParcel(Parcel source) {
            City city = new City();
            city.id = source.readInt();
            city.cityName = source.readString();
            city.cityCode = source.readInt();
            city.provinceId = source.readInt();
            return city;
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };
}
