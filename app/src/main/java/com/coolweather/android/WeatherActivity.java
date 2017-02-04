package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.Binding.ForcastInfo;
import com.coolweather.android.Binding.Handers;
import com.coolweather.android.Binding.WeatherInfo;
import com.coolweather.android.Service.AutoUpdateService;
import com.coolweather.android.databinding.ActivityWeatherBinding;
import com.coolweather.android.databinding.ForecastItemBinding;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.HeWeather;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.QueryArea;
import com.google.gson.Gson;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by 令子 on 2017/1/22.
 */

public class WeatherActivity extends AppCompatActivity {

    public ActivityWeatherBinding binding;

    private WeatherInfo weatherInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        binding = DataBindingUtil.setContentView(this,R.layout.activity_weather);
        weatherInfo = new WeatherInfo();
        Handers handers = new Handers();
        binding.setWeatherInfo(weatherInfo);
        binding.setHanders(handers);
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        String binPic = prefs.getString("bing_pic",null);
        if (binPic != null){
            Glide.with(this).load(binPic).into(binding.bingPicImg);
        }else {
            loadBingPic();
        }
        String weatherId;
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Gson gson = new Gson();
            HeWeather heWeather = gson.fromJson(weatherString,HeWeather.class);
            showWeatherInfo(heWeather);
        } else {
            // 无缓存时去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            binding.weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this);
                String weatherString = prefs.getString("weather", null);
                Gson gson = new Gson();
                HeWeather heWeather = gson.fromJson(weatherString,HeWeather.class);
                String weatherId = heWeather.basic.weatherId;
                requestWeather(weatherId);
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId) {
        String apiKey = "a52f1791bae84198a717cf47d6d802c5";
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://guolin.tech/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        QueryArea queryWeather = retrofit.create(QueryArea.class);
        queryWeather.queryWeather(weatherId,apiKey)
                .subscribeOn(Schedulers.io())
                .doAfterNext(new Consumer<Weather>() {
                    @Override
                    public void accept(Weather weather) throws Exception {
                        Gson gson = new Gson();
                        HeWeather heWeather = weather.HeWeather.get(0);
                        String responseText = gson.toJson(heWeather);
                        if (heWeather != null && "ok".equals(heWeather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Weather>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Weather weather) {
                        HeWeather heWeather = weather.HeWeather.get(0);
                        if (heWeather != null && "ok".equals(heWeather.status)) {
                            showWeatherInfo(heWeather);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        binding.swipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onComplete() {
                        binding.swipeRefresh.setRefreshing(false);
                    }
                });
        loadBingPic();
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(HeWeather heWeather) {
        weatherInfo.cityName.set(heWeather.basic.cityName);
        weatherInfo.updateTime.set(heWeather.basic.update.updateTime.split(" ")[1]);
        weatherInfo.degree.set(heWeather.now.temperature + "℃");
        weatherInfo.info.set(heWeather.now.more.info);
        LinearLayout forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        forecastLayout.removeAllViews();
        for (Forecast forecast : heWeather.forecastList) {
            ForcastInfo  forcastInfo = new ForcastInfo
                    (forecast.date,forecast.more.info,forecast.temperature.max,forecast.temperature.min);
            ForecastItemBinding fBinding = DataBindingUtil
                    .inflate(getLayoutInflater(),R.layout.forecast_item,forecastLayout,false);
            fBinding.setForcastInfo(forcastInfo);
            forecastLayout.addView(fBinding.getRoot());
        }
        if (heWeather.aqi != null) {
            weatherInfo.aqi.set(heWeather.aqi.city.aqi);
            weatherInfo.pm25.set(heWeather.aqi.city.pm25);
        }
        weatherInfo.comfort.set("舒适度：" + heWeather.suggestion.comfort.info);
        weatherInfo.carWash.set("洗车指数：" + heWeather.suggestion.carWash.info);
        weatherInfo.sport.set("运行建议：" + heWeather.suggestion.sport.info);
        binding.weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /**加载bing每日一图**/
    private void loadBingPic(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://guolin.tech/api/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        QueryArea queryBing = retrofit.create(QueryArea.class);
        queryBing.queryBingPic()
                .subscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, String>() {
                    @Override
                    public String apply(ResponseBody responseBody) throws Exception {
                        String responseText = responseBody.string();
                        responseBody.close();
                        return responseText;
                    }
                })
                .doAfterNext(new Consumer<String>() {
                    @Override
                    public void accept(String bingUrl) throws Exception {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("bing_pic",bingUrl);
                        editor.apply();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(String bingUrl) {
                        Glide.with(WeatherActivity.this).load(bingUrl).into(binding.bingPicImg);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Toast.makeText(WeatherActivity.this, "加载必应图片失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
