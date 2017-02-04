package com.coolweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.coolweather.android.Binding.Area;
import com.coolweather.android.databinding.ChooseAreaBinding;
import com.coolweather.android.db.City;
import com.coolweather.android.db.City_Table;
import com.coolweather.android.db.County;
import com.coolweather.android.db.County_Table;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.QueryArea;
import com.coolweather.android.util.Utility;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by 令子 on 2017/1/21.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**省市县列表**/
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    /**选中的省市**/
    private Province selectedProvince;
    private City selectedCity;

    /**当前选中的级别**/
    private int currentLevel;

    /**Databinding**/
    private ChooseAreaBinding binding;
    private Area area;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.choose_area,container,false);
        area = new Area();
        area.areaName.set("");
        binding.setArea(area);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        binding.listView.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity){
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.binding.drawerLayout.closeDrawers();
                        activity.binding.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    /**查询全国所有的省，优先从数据库查询，没有再去服务器查询**/
    private void queryProvinces(){
        area.areaName.set("中国");
        binding.backButton.setVisibility(View.GONE);
        provinceList = SQLite.select().from(Province.class).queryList();
        if(provinceList.size() > 0){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            binding.listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            queryFromServer(0,0,"province");
        }
    }

    /**查询选中省内所有的市，优先从数据库查询，没有再去服务器查询**/
    private void queryCities(){
        area.areaName.set(selectedProvince.getProvinceName());
        binding.backButton.setVisibility(View.VISIBLE);
        cityList = SQLite.select().from(City.class).where(City_Table.provinceId.eq(selectedProvince.getId())).queryList();
        if(cityList.size() > 0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            binding.listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            queryFromServer(provinceCode,0,"city");
        }
    }

    /**查询选中市内所有的县，优先从数据库查询，没有再去服务器查询**/
    private void queryCounties(){
        area.areaName.set(selectedCity.getCityName());
        binding.backButton.setVisibility(View.VISIBLE);
        countyList = SQLite.select().from(County.class).where(County_Table.cityId.eq(selectedCity.getId())).queryList();
        if(countyList.size() > 0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            binding.listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            queryFromServer(provinceCode,cityCode,"county");
        }
    }

    /**根据传入的代号和类型在服务器上查询数据**/
    private void  queryFromServer(int provinceCode,int cityCode,final String type) {
        showProgressDialog();
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://guolin.tech/api/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        QueryArea queryArea = retrofit.create(QueryArea.class);
        Subscriber subscriber = new Subscriber<ResponseBody>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ResponseBody o) {
                if ("province".equals(type)){
                    queryProvinces();
                }else if ("city".equals(type)){
                    queryCities();
                }else if ("county".equals(type)){
                    queryCounties();
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                closeProgressDialog();
            }
        };
        if("province".equals(type)){
            queryArea.queryProvince()
                    .subscribeOn(Schedulers.io())
                    .doOnNext(new Consumer<ResponseBody>() {
                        @Override
                        public void accept(ResponseBody responseBody) throws Exception {
                            String responseText = responseBody.string();
                            Utility.handleProvinceResponse(responseText);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber);
        }else if ("city".equals(type)){
            queryArea.queryCity(provinceCode)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(new Consumer<ResponseBody>() {
                        @Override
                        public void accept(ResponseBody responseBody) throws Exception {
                            String responseText = responseBody.string();
                            Utility.handleCityResponse(responseText,selectedProvince.getId());
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber);
        }else if ("county".equals(type)){
            queryArea.queryCounty(provinceCode,cityCode)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(new Consumer<ResponseBody>() {
                        @Override
                        public void accept(ResponseBody responseBody) throws Exception {
                            String responseText = responseBody.string();
                            Utility.handleCountyResponse(responseText,selectedCity.getId());
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber);
        }
    }

    /**显示进度对话框**/
    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**关闭进度对话框**/
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
