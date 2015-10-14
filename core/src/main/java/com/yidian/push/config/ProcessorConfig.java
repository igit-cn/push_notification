package com.yidian.push.config;

import com.yidian.push.data.HostPort;
import com.yidian.push.data.Platform;
import com.yidian.push.data.PushType;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.config.RequestConfig;

import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by yidianadmin on 15-2-2.
 */
@Getter
@Setter
public class ProcessorConfig {
    private String lockFile;
    private int requestScanIntervalInSeconds = 2;
    private int iPhonePoolSize = 20;
    private int androidPoolSize = 200;
    private List<HostPort> iPhoneLoggerList;
    private List<HostPort> androidLoggerList;
    private int socketConnectTimeout = 10;
    private int socketReadTimeout = 3;
    private List<String> APPID_XIAOMI;

    private boolean needWriteLog = true;
    private boolean needWriteHistory = false;
    private String pushHistoryUrl = "http://localhost:6266/push_service/add_history";
    private int pushHistoryBatchSize = 1000;

    // http connection settings
    private int httpConnectionDefaultMaxPerRoute = 200;
    private int httpConnectionMaxTotal = 2000;
    private int retryTimes = 3;
    private int messageExpireTimeInSeconds = 3 * 60 * 60; // 3 hours

    private int iosPushBatch = 100;
    private String iosPushBatchUrl = "http://10.111.0.57:5266/push_service/apns_multiple/";
    private String iosPushSingleUrl = "http://10.111.0.57:5266/push_service/apns_single/";

    private int umengPushBatch = 100;
    private String supportedUmengPushAppIdNameMapping = "/home/services/push_notification/data/umeng/supported_appid_name_mapping";
    private String umengPushBatchUrl = "http://10.111.0.57:5266/push_service/upns_multiple/";
    private String umengPushSingleUrl = "http://10.111.0.57:5266/push_service/upns_single/";

    private int getuiPushBatch = 100;
    private String getuiPemBase = "/home/services/push_notification/data/getui/";
    private String getuiPemPattern = "*.getui";
    private String getuiPushBatchUrl = "http://10.111.0.57:5266/push_service/getui_multiple/";
    private String getuiPushSinglUrl = "http://10.111.0.57:5266/push_service/getui_single/";

    private int xiaomiPushBatch = 1000;
    private String xiaomiPemBase = "/home/services/push_notification/data/xiaomi/";
    private String xiaomiPemPattern = "*.mmpp";
    private String xiaomiPushMultipleMessagesUrl = "https://api.xmpush.xiaomi.com/v2/multi_messages/aliases";
    private String xiaomiPushSingleMessageUrl = "https://api.xmpush.xiaomi.com/v2/message/alias";
    private HashSet<PushType> androidUseRecommendPushTypes = null;


    public RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(socketConnectTimeout * 1000)
                .setConnectionRequestTimeout(socketConnectTimeout * 1000)
                .setSocketTimeout(socketReadTimeout * 1000).build();
    }
    public boolean isXiaomi(String appId) {
        if (null == APPID_XIAOMI || APPID_XIAOMI.size() == 0) {
            return false;
        }
        for (String item : APPID_XIAOMI) {
            if (item.equals(appId)) {
                return true;
            }
        }
        return false;
    }

    public List<HostPort> getLoggerList(Platform platform) {
        if (platform == Platform.IPHONE) {
            return iPhoneLoggerList;
        }
        else {
            return androidLoggerList;
        }
    }

    public boolean shouldUseRecommendPush(PushType pushType) {
        if (null == androidUseRecommendPushTypes || !androidUseRecommendPushTypes.contains(pushType)) {
            return false;
        }
        return true;
    }
}
