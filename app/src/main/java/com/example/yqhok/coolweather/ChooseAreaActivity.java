package com.example.yqhok.coolweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.yqhok.coolweather.adapter.ChooseAreaAdapter;
import com.example.yqhok.coolweather.application.MyApplication;
import com.example.yqhok.coolweather.base.BaseActivity;
import com.example.yqhok.coolweather.base.adapter.BaseRecyclerViewAdapter;
import com.example.yqhok.coolweather.databinding.ActivityChooseAreaBinding;
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

public class ChooseAreaActivity extends BaseActivity<ActivityChooseAreaBinding> implements BaseRecyclerViewAdapter.OnItemClickListener<String>, SearchView.OnQueryTextListener {

    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SearchView searchView;

    private ChooseAreaAdapter adapter;

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

    private static boolean isNetworkAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);
        initView();
        queryProvinces();
        showContentView();
        initNetReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        String flag = intent.getStringExtra("flag");
//        if (searchView.hasFocus()) {
//            searchView.clearFocus();
//        }
        if (flag != null) {
            if (currentLevel == LEVEL_PROVINCE && flag.equals("WeatherActivity")) {
                WeatherActivity.start(this);
                ChooseAreaActivity.this.finish();
            } else if (flag.equals("LoginActivity") || flag.equals("HomeActivity")) {
                HomeActivity.start(this);
                ChooseAreaActivity.this.finish();
            }
        } else if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        }
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ChooseAreaActivity.class);
        context.startActivity(intent);
    }

    private void initView() {
        toolbar = getToolBar();
        recyclerView = bindingView.recyclerView;
        adapter = new ChooseAreaAdapter();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    /**
     * Query each province prior to database else from server
     */
    private void queryProvinces() {
        setTitle("中国");
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.removeAll();
            adapter.addAll(dataList);
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(0);
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
        setTitle(selectedProvince.getProvinceName());
        cityList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.removeAll();
            adapter.addAll(dataList);
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(0);
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
        setTitle(selectedCity.getCityName());
        countyList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.removeAll();
            adapter.addAll(dataList);
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(0);
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
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showContentView();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
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
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    runOnUiThread(new Runnable() {
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

    private void initNetReceiver() {
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.toolbar_choose_area, getMenu());
//        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
//        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
//        searchView.setQueryHint("输入要查找的地区");
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setOnClickListener(this);
//        return true;
//    }

    @Override
    public void onClick(String s, int position) {
        if (isNetworkAvailable) {
            if (currentLevel == LEVEL_PROVINCE) {
                selectedProvince = provinceList.get(position);
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                selectedCity = cityList.get(position);
                queryCounties();
            } else if (currentLevel == LEVEL_COUNTY) {
                String weatherId = countyList.get(position).getWeatherId();
                Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                intent.putExtra("weather_id", weatherId);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(this, "当前无网络连接", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.action_search:
//                break;
//        }
//    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo == null) {
                Toast.makeText(context, "当前无网络连接", Toast.LENGTH_LONG).show();
                isNetworkAvailable = false;
            } else {
                isNetworkAvailable = true;
            }
        }

    }

}
