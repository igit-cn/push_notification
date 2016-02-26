package com.yidian.push.config;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
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
    private int topicConsumerNum = 1;
    private int processNum = 1;
    private int processFetchSize = 1;
    private String queryFile = "";
    private Map<String, String> producerProperties = null;
    private Map<String, String> consumerProperties = null;
    private String mongoHost = "";
    private int mongoPort = 27017;
    private String mongoDBName = "";
    private String mongoCollName = "";
    private String opentsdbAddress = "http://dataplatform.yidian.com:4245/api/put";
    private Map<String, String> opentsdbTags = null;
    private double relevanceThreshold = 0.8;
    private long filterDocTimeInSeconds = 86400;// 1 day



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
