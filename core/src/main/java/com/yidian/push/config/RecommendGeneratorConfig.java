package com.yidian.push.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Created by yidianadmin on 15-2-2.
 */
@Getter
@Setter
public class RecommendGeneratorConfig {
    private String lockFile = "/tmp/push_notification_recommend_generator.lck";
    private String qpsURL = "http://dataplatform.yidian.com:4242/api/query?start=3m-ago&m=sum:prediction.default.qps.m1";
    private int qpsRefreshFrequencyInSeconds = 1;
    private int maxQPS = 800;
    private String recommendURL = "http://lc1.haproxy.yidian.com:8017/NewsRecommender/OfflineRecommendNewsHandler";

    private int httpConnectionDefaultMaxPerRoute = 200;
    private int httpConnectionMaxTotal = 2000;
    private int retryTimes = 3;

    private Set<String> APP_MAIN;
    private Set<String> NON_APP_MAIN;

    int threadPoolSize = 500;

}
