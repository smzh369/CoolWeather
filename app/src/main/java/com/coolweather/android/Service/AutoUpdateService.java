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
                    .addConverterFactory(GsonConverterFactory.create()).build();
            QueryArea queryWeather = retrofit.create(QueryArea.class);
            retrofit2.Call<Weather> call = queryWeather.queryWeather(weatherId,apiKey);
            call.enqueue(new retrofit2.Callback<Weather>() {
                @Override
                public void onResponse(retrofit2.Call<Weather> call, retrofit2.Response<Weather> response) {
                    Gson gson = new Gson();
                    HeWeather weather = response.body().HeWeather.get(0);
                    String responseText = gson.toJson(weather);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                                (AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<Weather> call, Throwable t) {

                }
            });
        }
    }

    private void updateBingPic(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://guolin.tech/api/").build();
        QueryArea queryBing = retrofit.create(QueryArea.class);
        retrofit2.Call<ResponseBody> call = queryBing.queryBingPic();
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                try{
                    String bingUrl = response.body().string();
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                            (AutoUpdateService.this).edit();
                    editor.putString("bing_pic",bingUrl);
                    editor.apply();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
