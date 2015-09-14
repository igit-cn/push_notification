package com.yidian.push.config;

import com.yidian.push.data.Environment;
import com.yidian.push.data.HostPortDB;
import com.yidian.push.data.Platform;
import com.yidian.push.data.RedisHostPort;
import lombok.Getter;
import lombok.Setter;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

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
    private String recommendURL = "";

    private int httpConnectionDefaultMaxPerRoute = 200;
    private int httpConnectionMaxTotal = 2000;
    private int retryTimes = 3;

    int threadPoolSize = 500;

}
