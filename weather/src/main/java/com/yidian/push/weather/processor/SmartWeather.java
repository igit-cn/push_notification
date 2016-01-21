package com.yidian.push.weather.processor;

import com.yidian.push.config.Config;
import com.yidian.push.config.WeatherPushConfig;
import com.yidian.push.weather.data.Weather;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianyuzhi on 16/1/21.
 */
public class SmartWeather {
    private static boolean IsInitialized = false;
    private static WeatherPushConfig config = null;
    private static Weather weather = null;
    private static Map<String, String> locationToChannel = new HashMap<>();
    //private static Map<String, Article>

    public static void init() throws IOException {
        if (IsInitialized) return;
        synchronized (SmartWeather.class) {
            if (IsInitialized) return;
            config = Config.getInstance().getWeatherPushConfig();
            weather = new Weather(config.getWeatherConfig());
            IsInitialized = true;
        }
    }

    public static void destroy() {
    }

    public void process() {
    }




}
