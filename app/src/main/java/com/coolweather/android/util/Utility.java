package com.coolweather.android.util;

import android.text.TextUtils;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.HeWeather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 令子 on 2017/1/21.
 */

public class Utility {

    /**解析和处理服务器返回的省级数据**/
    public static void handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0;i < allProvinces.length();i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    /**解析和处理服务器返回的市级数据**/
    public static void handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0;i < allCities.length();i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    /**解析和处理服务器返回的县级数据**/
    public static void handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0;i < allCounties.length();i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    /**将json数据转换成HeWeather对象**/
    public static HeWeather jsonToHeWeather(String weatherString){
        Gson gson = new Gson();
        HeWeather heWeather = gson.fromJson(weatherString,HeWeather.class);
        return heWeather;
    }

    public static String heWeatherToJson(HeWeather heWeather){
        Gson gson = new Gson();
        String weatherString = gson.toJson(heWeather);
        return weatherString;
    }

}
