package com.example.weather.coolweather.util;

import android.text.TextUtils;

import com.example.weather.coolweather.db.CoolWeatherDB;
import com.example.weather.coolweather.model.City;
import com.example.weather.coolweather.model.County;
import com.example.weather.coolweather.model.Province;

/**
 * Created by 64088 on 2017/3/17.
 */

public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */

    public synchronized static boolean handleProvinceResponse(CoolWeatherDB db,String response){
        if(!TextUtils.isEmpty(response)){
            String[] allProvince=response.split(",");
            if(allProvince!=null&&allProvince.length>0){
                for(String p:allProvince){
                    String[] array=p.split("\\|");
                    Province province=new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析出的数据存储到数据库
                    db.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */

    public synchronized static boolean handleCityResponse(CoolWeatherDB db,
           String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            String[] allCity=response.split(",");
            if(allCity!=null&&allCity.length>0){
                for(String p:allCity){
                    String[] array=p.split("\\|");
                    City city=new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    //将解析出的数据存储到数据库
                    db.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */

    public synchronized static boolean handleCountyResponse(CoolWeatherDB db,
        String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            String[] allCounty=response.split(",");
            if(allCounty!=null&&allCounty.length>0){
                for(String p:allCounty){
                    String[] array=p.split("\\|");
                    County county=new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    //将解析出的数据存储到数据库
                    db.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }
}
