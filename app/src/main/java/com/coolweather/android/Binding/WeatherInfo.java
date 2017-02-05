package com.coolweather.android.Binding;

import android.databinding.ObservableField;

import com.coolweather.android.gson.HeWeather;


/**
 * Created by 令子 on 2017/2/3.
 */

public class WeatherInfo {

    public final ObservableField<String> cityName = new ObservableField<>();

    public final ObservableField<String> updateTime = new ObservableField<>();

    public final ObservableField<String> degree = new ObservableField<>();

    public final ObservableField<String> info = new ObservableField<>();

    public final ObservableField<String> aqi = new ObservableField<>();

    public final ObservableField<String> pm25 = new ObservableField<>();

    public final ObservableField<String> comfort = new ObservableField<>();

    public final ObservableField<String> carWash = new ObservableField<>();

    public final ObservableField<String> sport = new ObservableField<>();

}
