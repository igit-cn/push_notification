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
    private String mongoWeatherCollName = "";
    private String mongoPushCounterCollName = "";
    private String opentsdbAddress = "http://dataplatform.yidian.com:4245/api/put";
    private Map<String, String> opentsdbTags = null;

    private WeatherConfig weatherConfig = null;
    private int refreshIntervalInSeconds = 600;
    private int refreshFetchPoolSize = 20;
    private int localChannelRefreshIntervalInSeconds = 60 * 60;
    private int cleanCacheDays = 3;

    private String genDocUrl = "http://lc2.haproxy.yidian.com:9100/post/save?action=2&uid=12299265";
    private String genDocMediaId = "64485";
    private String getDocIdUrl = "http://lc2.haproxy.yidian.com:9100/post/get-post";
    private String getLocalChannelUrl = "http://lc1.haproxy.yidian.com:8701/push_service/cityToChannel";
    private String pushUrl = "http://lc1.haproxy.yidian.com:8703/push/add_task.php";
    private String pushKey = "2c6875aa781253ce445c450fd08e066e";
    private String pushUserIds = "auto";

    private String alarmPushLevel = "";
    private String alarmGuangdongPushLevel = "";




}
