package com.coolweather.android.util;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by 令子 on 2017/2/5.
 */

public class HttpUtil {

    public static QueryArea retrofitConnection(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://guolin.tech/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(QueryArea.class);
    }

}
