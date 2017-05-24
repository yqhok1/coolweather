package com.example.yqhok.coolweather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.yqhok.coolweather.base.BaseFragment;
import com.example.yqhok.coolweather.databinding.FragmentChooseAreaBinding;
import com.example.yqhok.coolweather.db.City;
import com.example.yqhok.coolweather.db.County;
import com.example.yqhok.coolweather.db.Province;
import com.example.yqhok.coolweather.util.HttpUtil;
import com.example.yqhok.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by yqhok on 2017/5/22.
 */

public class ChooseAreaFragment extends BaseFragment<FragmentChooseAreaBinding> implements ListView.OnItemClickListener {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ListView listView;

    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    /**
     * Province list
     */
    private List<Province> provinceList;

    /**
     * City list
     */
    private List<City> cityList;

    /**
     * County list
     */
    private List<County> countyList;

    /**
     * Selected province
     */
    private Province selectedProvince;

    /**
     * Selected city
     */
    private City selectedCity;

    /**
     * Current level
     */
    private int currentLevel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        getActivity().setTitle("");
        queryProvinces();
    }

    @Override
    public int setContent() {
        return R.layout.fragment_choose_area;
    }

    private void initView() {
        listView = bindingView.listView;
        listView.setOnItemClickListener(this);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (currentLevel == LEVEL_PROVINCE) {
            selectedProvince = provinceList.get(position);
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            selectedCity = cityList.get(position);
            queryCounties();
        } else if (currentLevel == LEVEL_COUNTY) {
            String weatherId = countyList.get(position).getWeatherId();
        }
    }

    /**
     * Query each province prior to database else from server
     */
    private void queryProvinces() {
        getActivity().setTitle("中国");
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * Query each city prior to database else from server
     */
    private void queryCities() {
        getActivity().setTitle(selectedProvince.getProvinceName());
        cityList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * Query each county prior to database else from server
     */
    private void queryCounties() {
        getActivity().setTitle(selectedCity.getCityName());
        countyList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * Query data according to address and type
     */
    private void queryFromServer(String address, final String type) {
        showLoading();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showContentView();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getCityCode());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showContentView();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }
}
