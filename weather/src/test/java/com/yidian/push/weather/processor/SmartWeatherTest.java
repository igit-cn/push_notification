package com.yidian.push.weather.processor;

import com.yidian.push.config.Config;
import com.yidian.push.config.WeatherConfig;
import com.yidian.push.weather.data.Alarm;
import org.testng.annotations.Test;


/**
 * Created by tianyuzhi on 16/2/29.
 */
public class SmartWeatherTest {

    @Test
    public void isShouldPush() {
        assert !SmartWeather.isShouldPush("01", "");
        assert SmartWeather.isShouldPush("02", "01");
        assert SmartWeather.isShouldPush("03", "03");
    }

    @Test
    public void isShouldPush2() {
        System.out.println(System.getProperty("user.dir"));
        Config.setCONFIG_FILE("src/main/resources/config/config.json");
        SmartWeather smartWeather = SmartWeather.getInstance();
        Alarm alarm = new Alarm();
        String areaId = "101130101";//乌鲁木齐
        alarm.setLevelId("01");
        assert !smartWeather.shouldPush(areaId, alarm, 420);
        alarm.setLevelId("04");
        assert smartWeather.shouldPush(areaId, alarm, 420);
        alarm.setLevelId("01");
        assert !smartWeather.shouldPush(areaId, alarm, 480);
        alarm.setLevelId("04");
        assert smartWeather.shouldPush(areaId, alarm, 480);

        areaId = "101010100"; //北京
        alarm.setLevelId("03");
        assert smartWeather.shouldPush(areaId, alarm, 360);
        alarm.setLevelId("04");
        assert smartWeather.shouldPush(areaId, alarm, 360);
        alarm.setLevelId("03");
        assert smartWeather.shouldPush(areaId, alarm, 420);
        alarm.setLevelId("04");
        assert smartWeather.shouldPush(areaId, alarm, 420);

        areaId = "101280101"; //广州
        alarm.setLevelId("04");
        assert smartWeather.shouldPush(areaId, alarm, 350);
        alarm.setLevelId("05");
        assert smartWeather.shouldPush(areaId, alarm, 350);
        alarm.setLevelId("04");
        assert smartWeather.shouldPush(areaId, alarm, 420);
        alarm.setLevelId("05");
        assert smartWeather.shouldPush(areaId, alarm, 420);
    }


}