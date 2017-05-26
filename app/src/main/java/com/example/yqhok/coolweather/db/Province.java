package com.example.yqhok.coolweather.db;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

/**
 * Created by yqhok on 2017/5/21.
 */

public class Province extends DataSupport implements Parcelable{

    private int id;
    private String provinceName;
    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(provinceName);
        dest.writeInt(provinceCode);
    }

    public static final Parcelable.Creator<Province> CREATOR = new Parcelable.Creator<Province>() {

        @Override
        public Province createFromParcel(Parcel source) {
            Province province = new Province();
            province.id = source.readInt();
            province.provinceName = source.readString();
            province.provinceCode = source.readInt();
            return province;
        }

        @Override
        public Province[] newArray(int size) {
            return new Province[size];
        }
    };
}
