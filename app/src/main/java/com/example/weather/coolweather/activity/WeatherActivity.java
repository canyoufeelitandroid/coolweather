package com.example.weather.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.weather.coolweather.R;
import com.example.weather.coolweather.service.AutoUpdateService;
import com.example.weather.coolweather.util.HttpCallbackListener;
import com.example.weather.coolweather.util.HttpUtil;
import com.example.weather.coolweather.util.Utility;

/**
 * Created by 64088 on 2017/3/17.
 */

public class WeatherActivity extends Activity implements View.OnClickListener{
    private LinearLayout weatherInfoLayout;

    private TextView cityNameText;
    private TextView publishText;
    private TextView weatherDespText;
    private TextView temp1Text;
    private TextView temp2Text;
    private TextView currentDataText;
    private Button switchCityBtn;
    private Button refreshWeatherBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        initUI();
        String countyCode=getIntent().getStringExtra("county_code");
        if(!TextUtils.isEmpty(countyCode)){
            //有县级代号就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else {
            showWeather();
        }
    }

    private void initUI(){
        weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
        cityNameText=(TextView)findViewById(R.id.city_name);
        publishText=(TextView)findViewById(R.id.publish_text);
        weatherDespText=(TextView)findViewById(R.id.weather_desp);
        temp1Text=(TextView)findViewById(R.id.temp1);
        temp2Text=(TextView)findViewById(R.id.temp2);
        currentDataText=(TextView)findViewById(R.id.current_data);
        switchCityBtn=(Button)findViewById(R.id.switch_city);
        refreshWeatherBtn=(Button)findViewById(R.id.refresh_weather);
        switchCityBtn.setOnClickListener(this);
        refreshWeatherBtn.setOnClickListener(this);
    }

    private void queryWeatherCode(String countyCode){
        String address="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
        queryFromServer(address,"countyCode");
    }

    private void queryWeatherInfo(String weatherCode){
        String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
        queryFromServer(address,"weatherCode");
    }

    private  void queryFromServer(final String address,final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if("countyCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        //从返回的额数据中解析出天气代号
                        String[] array=response.split("\\|");
                        if(array!=null&&array.length==2){
                            String weatherCode=array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if("weatherCode".equals(type)){
                    Log.i("data","处理服务器返回的天气信息");
                    //处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this,response);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                publishText.setText("同步失败");
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取天气信息，并显示在页面上
     */
    private void showWeather(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name",""));
        temp1Text.setText(prefs.getString("temp1",""));
        temp2Text.setText(prefs.getString("temp2",""));
        weatherDespText.setText(prefs.getString("weather_desp",""));
        currentDataText.setText(prefs.getString("current_data",""));
        publishText.setText(getText(R.string.today)+prefs.getString("publish_time","")+getText(R.string.publish));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        //启动服务，用于后台自动更新天气信息
        Intent serviceIntent=new Intent(this, AutoUpdateService.class);
        startService(serviceIntent);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.switch_city:
                Intent switchIntent=new Intent(WeatherActivity.this,ChooseActivity.class);
                switchIntent.putExtra("from_weather_activity",true);
                startActivity(switchIntent);
                finish();
                break;
            case R.id.refresh_weather:
                //从sharedPreference里获得weatherCode,然后重新获取刷新天气
                publishText.setText(getText(R.string.loading));
                SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode=sharedPreferences.getString("weather_code","");
                if(!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }

    }
}
