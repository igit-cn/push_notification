package com.yidian.push.config;

import com.yidian.push.data.HostPort;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.config.RequestConfig;

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

    private String genDocAndGetDocIdUrl = "http://10.101.0.150:9999/service/inject_self_news";
    private String genDocUid = "tianzy";
    private String docSource = "中国气象局";
    private String docFromUrl = "http://openweather.weather.com.cn/Home/Help/Product.html";

    private String getLocalChannelUrl = "http://lc1.haproxy.yidian.com:8701/push_service/cityToChannel";
    private String pushUrl = "http://lc1.haproxy.yidian.com:8703/push/add_task.php";
    private String pushKey = "2c6875aa781253ce445c450fd08e066e";
    private String pushUserIds = "auto";
    private boolean isDebug = true;
    private String debugChannels = "u_faked";
    private int dayPushThreshold = 1;

    private int socketConnectTimeout = 10;
    private int socketReadTimeout = 3;

    private String dayAlarmPushLevel = "03";
    private String dayAlarmGuangdongPushLevel = "04";
    private String nightAlarmPushLevel = "04";
    private String nightAlarmGuangdongPushLevel = "05";
    // add for protect time
    private int eastDayPushStartTimeInMinutes = 7 * 60;
    private int eastDayPushEndTimeInMinutes = 24 * 60;
    private int westDayPushStartTimeInMinutes = 8 * 60;
    private int westDayPushEndTimeInMinutes = 24 * 60;


    public RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(socketConnectTimeout * 1000)
                .setConnectionRequestTimeout(socketConnectTimeout * 1000)
                .setSocketTimeout(socketReadTimeout * 1000).build();
    }
}
