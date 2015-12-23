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
public class InstantPushConfig {
    private String lockFile = "/tmp/push_notification_instant_push.lck";
    private int requestScanIntervalInSeconds = 3;
    private String consumerTopicName = "relatedchannel";
    private int consumerNum = 1;
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
