package com.yidian.push.config;

import com.yidian.push.data.Environment;
import com.yidian.push.data.HostPort;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.config.RequestConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yidianadmin on 15-2-2.
 */
@Getter
@Setter
public class RecommendGeneratorOnlineConfig {
    private String lockFile = "/tmp/push_notification_recommend_generator.lck";
    private List<HostPort> hostPortList = Arrays.asList(new HostPort("localhost", 8080));

    private String qpsURL = "http://dataplatform.yidian.com:4242/api/query?start=3m-ago&m=sum:prediction.default.qps.m1";
    private int qpsRefreshFrequencyInSeconds = 1;
    private int maxQPS = 800;
    private String recommendURL = "http://lc1.haproxy.yidian.com:8017/NewsRecommender/OfflineRecommendNewsHandler";
    private String docIdInfoURL = "http://a1.go2yd.com/Website/contents/content";
    private int docIdInfoBatchSize = 1000;

    private int httpConnectionDefaultMaxPerRoute = 200;
    private int httpConnectionMaxTotal = 2000;
    private int retryTimes = 3;
    private int socketConnectTimeout = 10;
    private int socketReadTimeout = 3;

    private Set<String> APP_MAIN = new HashSet<>(Arrays.asList("hipu","yidian","yddk","xiaomi","zxpad","haowai","weixinwen","hot","ydtxz","ydtp","kxw","lastmile"));
    private Set<String> NON_APP_X = new HashSet<>(Arrays.asList("hipu","yidian","yddk","xiaomi","zxpad","haowai","weixinwen","hot","ydtxz","ydtp","kxw","lastmile"));

    private int consumerThreadPoolSize = 500;
    private int pushThreadPoolSize = 50;
    private String inputDataPath = "";
    private String outputDataPath = "";
    private int inputLookBackDays = 7;
    private int outputLookBackTimes = 3;


    private int titleMinLength = 7;
    private Set<Integer> buckets;
    private int sleepTimeInSeconds = 10;
    private String onlineAddTaskUrl = "http://push.yidian.com/task/add-task";
    private String pushKey = "acf6dbe50dfa2c572f7fe13b699495d7";
    private int recordToPushInOneSecond = 10000;
    private int recordPushBatchSize = 50;
    private Environment environment = Environment.RRODUCTION;


    public boolean isTest() {
        if (null!=environment && environment == Environment.RRODUCTION) {
            return false;
        }
        return true;
    }


    public RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(socketConnectTimeout * 1000)
                .setConnectionRequestTimeout(socketConnectTimeout * 1000)
                .setSocketTimeout(socketReadTimeout * 1000).build();
    }

}
