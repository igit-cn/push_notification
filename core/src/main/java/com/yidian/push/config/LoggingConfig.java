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
import java.util.Map;
import java.util.Properties;

/**
 * Created by yidianadmin on 15-2-2.
 */
@Getter
@Setter
public class LoggingConfig {
    private String lockFile = "/tmp/push_notification_logging.lck";
    private int requestScanIntervalInSeconds = 3;
    private String producerTopicName = "rawlog_str_push_log";
    private int producerNumber = 5;
    private Map<String, String> producerProperties = null;
    private Map<String, String> consumerProperties = null;



    public Properties getProducerProperties() {
        Properties properties = new Properties();
        if (null != producerProperties) {
            for (String key : producerProperties.keySet()) {
                properties.setProperty(key, producerProperties.get(key));
            }
        }
        return properties;
    }

    public Properties getConsumerProperties() {
        Properties properties = new Properties();
        if (null != consumerProperties) {
            for (String key : consumerProperties.keySet()) {
                properties.setProperty(key, consumerProperties.get(key));
            }
        }
        return properties;
    }
}
