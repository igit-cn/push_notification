package com.yidian.push.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Properties;

/**
 * Created by yidianadmin on 15-2-2.
 */
@Getter
@Setter
public class WeatherPushConfig {
    private String lockFile = "/tmp/push_notification_weather_push.lck";

    private String mongoHost = "";
    private int mongoPort = 27017;
    private String mongoDBName = "";
    private String mongoCollName = "";
    private String opentsdbAddress = "http://dataplatform.yidian.com:4245/api/put";
    private Map<String, String> opentsdbTags = null;


}
