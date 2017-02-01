package com.coolweather.android.util;

import com.coolweather.android.gson.Weather;


import io.reactivex.Flowable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by 令子 on 2017/1/30.
 */

public interface QueryArea {

    @GET("china")
    Flowable<ResponseBody> queryProvince();

    @GET("china/{provinceCode}")
    Flowable<ResponseBody> queryCity(@Path("provinceCode") int provinceCode);

    @GET("china/{provinceCode}/{cityCode}")
    Flowable<ResponseBody> queryCounty(@Path("provinceCode") int provinceCode, @Path("cityCode") int cityCode);

    @GET("weather")
    Flowable<Weather> queryWeather(@Query("cityid") String cityId,@Query("key") String key);

    @GET("bing_pic")
    Flowable<ResponseBody> queryBingPic();
}
