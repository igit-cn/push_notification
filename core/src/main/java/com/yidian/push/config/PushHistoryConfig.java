package com.yidian.push.config;

import com.yidian.push.data.HostPort;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by yidianadmin on 15-2-2.
 */
@Getter
@Setter
public class PushHistoryConfig {
    private String lockFile = "/tmp/push_notification_push_history.lck";
    private String yClusterServiceName = "zion_push_history";
    private String zookeeperAddress = "hadoop2-13.lg-4-e10.yidian.com:2181,hadoop2-14.lg-4-e10.yidian.com:2181,hadoop2-2.lg-4-e9.yidian.com:2181";

    private List<HostPort> hostPortList = Arrays.asList(new HostPort("localhost", 8080));
    private List<HostPort> httpsHostPortList = Arrays.asList(new HostPort("localhost", 8081));
    private int jettyMinThreads = 50;
    private int jettyMaxThreads = 100;
    private int jettyMaxFormContentSize = 40 * 1024 * 1024; // 40M

    private int redisUpdateBatchSize = 1000;
    private int redisKeepRecordSize = 100;
    private GenericObjectPoolConfig jedisConfig = null;
    private int producerFetchSize = 1000;
    private int consumerNum = 20;
}
