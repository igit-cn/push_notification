package com.yidian.push.config;

import com.yidian.push.data.HostPort;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by yidianadmin on 15-2-2.
 */
@Getter
@Setter
public class WeatherPushConfig {
    private String lockFile = "/tmp/push_notification_weather_push.lck";

    private List<HostPort> hostPortList = Arrays.asList(new HostPort("localhost", 8080));
    private List<HostPort> httpsHostPortList = Arrays.asList(new HostPort("localhost", 8081));
    private int jettyMinThreads = 50;
    private int jettyMaxThreads = 100;
    private int jettyMaxFormContentSize = 20 * 1024 * 1024; // 20M

    private String mongoHost = "10.101.2.22";
    private int mongoPort = 27017;
    private String mongoDBName = "instant_weather_alarm";
    private String mongoWeatherCollName = "weather_alarm";
    private String mongoPushCounterCollName = "push_counter";
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
    private boolean isDebug = true;
    private String debugChannels = "u_faked";
    private int dayPushThreshold = 1;

    private String alarmPushLevel = "03";
    private String alarmGuangdongPushLevel = "04";





}
