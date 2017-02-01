package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.Service.AutoUpdateService;
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
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by 令子 on 2017/1/22.
 */

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;

    private Button switchCity;

    public SwipeRefreshLayout swipeRefresh;

    private ImageView bingPicImg;

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        // 初始化各控件
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        switchCity = (Button)findViewById(R.id.switch_city);
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        String binPic = prefs.getString("bing_pic",null);
        if (binPic != null){
            Glide.with(this).load(binPic).into(bingPicImg);
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
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        switchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
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
                        swipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onComplete() {
                        swipeRefresh.setRefreshing(false);
                    }
                });
        loadBingPic();
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(HeWeather heWeather) {
        String cityName = heWeather.basic.cityName;
        String updateTime = heWeather.basic.update.updateTime.split(" ")[1];
        String degree = heWeather.now.temperature + "℃";
        String heWeatherInfo = heWeather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(heWeatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : heWeather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (heWeather.aqi != null) {
            aqiText.setText(heWeather.aqi.city.aqi);
            pm25Text.setText(heWeather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + heWeather.suggestion.comfort.info;
        String carWash = "洗车指数：" + heWeather.suggestion.carWash.info;
        String sport = "运行建议：" + heWeather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try{
                            String bingUrl = responseBody.string();
                            Glide.with(WeatherActivity.this).load(bingUrl).into(bingPicImg);
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("bing_pic",bingUrl);
                            editor.apply();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
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
