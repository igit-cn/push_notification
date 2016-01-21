package com.yidian.push.weather.data;

import com.alibaba.fastjson.JSONObject;
import com.yidian.push.config.WeatherConfig;
import com.yidian.push.utils.GsonFactory;
import com.yidian.push.utils.HttpConnectionUtils;
import com.yidian.push.weather.exception.UrlGenerationException;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 16/1/19.
 */
public class WeatherTest {

    @Test
    public void testGenUrl() throws Exception, UrlGenerationException {
        WeatherConfig config = new WeatherConfig();
        Weather weather = new Weather(config);
        String dateTime = DateTime.now().toString("yyyyMMddHHmm");
        dateTime = "201601191212";

        for (WeatherType weatherType : WeatherType.values()) {
            System.out.println(weatherType + ":" + weather.genUrl("101280101", weatherType, dateTime));
        }
    }

    @Test
    public void testGetAreaAlarms() throws IOException, UrlGenerationException {
        WeatherConfig config = new WeatherConfig();
        Weather weather = new Weather(config);
        String[] areas = {"呼和浩特","北京市", "上海", "南宁"};
        for (String area : areas) {
            List<Alarm> alarmList = weather.getAreaAlarms(area);
            System.out.println(area + ":" + GsonFactory.getDefaultGson().toJson(alarmList));
        }
    }

    @Test
    public void test() throws IOException {


    }

}