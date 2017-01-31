package com.coolweather.android.util;

import com.coolweather.android.gson.Weather;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by 令子 on 2017/1/30.
 */

public interface QueryArea {

    @GET("china")
    Call<ResponseBody> queryProvince();

    @GET("china/{provinceCode}")
    Call<ResponseBody> queryCity(@Path("provinceCode") int provinceCode);

    @GET("china/{provinceCode}/{cityCode}")
    Call<ResponseBody> queryCounty(@Path("provinceCode") int provinceCode, @Path("cityCode") int cityCode);

    @GET("weather")
    Call<Weather> queryWeather(@Query("cityid") String cityId,@Query("key") String key);

    @GET("bing_pic")
    Call<ResponseBody> queryBingPic();
}
