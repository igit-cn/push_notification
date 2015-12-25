package com.yidian.push.instant.services;

import com.google.common.collect.ImmutableMap;
import com.yidian.push.config.InstantPushConfig;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.utils.ZkUtils;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tianyuzhi on 15/12/22.
 */
@Log4j
public class ChannelConsumer2 {
    private static final String GROUP_ID = "group.id";
    private static final String ZOOKEEPER = "zookeeper.connect";
    private InstantPushConfig config = null;

    public ChannelConsumer2(InstantPushConfig config) {
        this.config = config;
    }

    public void run(){
        // Create the connection to the cluster
        Properties properties = config.getConsumerProperties();
        String topicName = config.getConsumerTopicName();
        int consumerNum = config.getTopicConsumerNum();
        String zk = config.getConsumerProperties().getProperty(ZOOKEEPER);
        String groupId = config.getConsumerProperties().getProperty(GROUP_ID, config.getConsumerTopicName() + "-" + new Random().nextInt());
        properties.put(GROUP_ID, groupId);
        ConsumerConfig consumerConfig = new ConsumerConfig(properties);
        ConsumerConnector consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);

        ZkUtils.maybeDeletePath(zk, "/consumers/" + groupId);

        Map<String, List<KafkaStream<byte[], byte[]>>> topicMessageStreams =
                consumerConnector.createMessageStreams(ImmutableMap.of(topicName, consumerNum));

        List<KafkaStream<byte[], byte[]>> streams = topicMessageStreams.get(topicName);

        // create list of # threads to consume from each of the partitions
        ExecutorService executor = Executors.newFixedThreadPool(consumerNum);

        // consume the messages in the threads
        for(final KafkaStream<byte[], byte[]> stream: streams) {
            executor.submit(new Runnable() {
                public void run() {
                    try {
                        for (MessageAndMetadata<byte[], byte[]> msgAndMetadata : stream) {
                            // process message (msgAndMetadata.message())
                            System.out.println(msgAndMetadata.message());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
