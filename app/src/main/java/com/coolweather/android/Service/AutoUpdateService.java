package com.coolweather.android.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;


import com.coolweather.android.gson.HeWeather;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.QueryArea;
import com.google.gson.Gson;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int eightHour = 8*60*60*1000;
        long triggerTime = SystemClock.elapsedRealtime() + eightHour;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerTime,pi);
        return super.onStartCommand(intent,flags,startId);
    }

    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        //有缓存时才有必要更新数据
        if (weatherString != null){
            Gson gson = new Gson();
            HeWeather weather = gson.fromJson(weatherString,HeWeather.class);
            String weatherId = weather.basic.weatherId;
            String apiKey = "&key=a52f1791bae84198a717cf47d6d802c5";
            Retrofit retrofit = new Retrofit.Builder().baseUrl("http://guolin.tech/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            QueryArea queryWeather = retrofit.create(QueryArea.class);
            queryWeather.queryWeather(weatherId,apiKey)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Weather>() {
                        @Override
                        public void accept(Weather weather) throws Exception {
                            Gson gson = new Gson();
                            HeWeather heWeather = weather.HeWeather.get(0);
                            String responseText = gson.toJson(heWeather);
                            if (heWeather != null && "ok".equals(heWeather.status)) {
                                SharedPreferences.Editor editor = PreferenceManager
                                        .getDefaultSharedPreferences(AutoUpdateService.this).edit();
                                editor.putString("weather", responseText);
                                editor.apply();
                            }
                        }
                    });
        }
    }

    private void updateBingPic(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://guolin.tech/api/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        QueryArea queryBing = retrofit.create(QueryArea.class);
        queryBing.queryBingPic()
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        String bingUrl = responseBody.string();
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                                (AutoUpdateService.this).edit();
                        editor.putString("bing_pic",bingUrl);
                        editor.apply();
                    }
                });
    }
}
