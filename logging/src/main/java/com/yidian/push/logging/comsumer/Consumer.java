package com.yidian.push.logging.comsumer;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tianyuzhi on 15/12/23.
 */
public class Consumer {
    public void run(){
//        "serializer.class": "kafka.serializer.StringEncoder",
//                // "compression.codec": "2",
//                // "compressed.topics": "relatedchannel",
//                "zookeeper.connect": "hadoop2-13.lg-4-e10.yidian.com:2181,hadoop2-14.lg-4-e10.yidian.com:2181,hadoop2-2.lg-4-e9.yidian.com:2181",
//                //"auto.commit.enable": "true",
//                //"auto.commit.interval.ms": "1000",
//                "zookeeper.session.timeout.ms": "30000",
//                //"rebalance.backoff.ms": "30000",
//                //"auto.offset.reset": "largest",
//                //"auto.offset.reset": "smallest",
//                "group.id": "relatedchannel_group_id"
        // Create the connection to the cluster
        Properties properties = new Properties();


        String topicName = "relatedchannel";
        int consumerNum = 1;
        String zk = "hadoop2-13.lg-4-e10.yidian.com:2181,hadoop2-14.lg-4-e10.yidian.com:2181,hadoop2-2.lg-4-e9.yidian.com:2181";

        String groupId = "relatedchannel_group_id";
        properties.put("group.id", groupId);
        properties.put( "zookeeper.connect", zk);
        // ZkUtils.maybeDeletePath(zk, "/consumers/" + groupId);

        ConsumerConfig consumerConfig = new ConsumerConfig(properties);
        ConsumerConnector consumerConnector = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);

        Map<String, Integer> map = new HashMap<>();
        map.put(topicName, consumerNum);

        Map<String, List<KafkaStream<byte[], byte[]>>> topicMessageStreams =
                consumerConnector.createMessageStreams(map);

        List<KafkaStream<byte[], byte[]>> streams = topicMessageStreams.get(topicName);

        // create list of # threads to consume from each of the partitions
        ExecutorService executor = Executors.newFixedThreadPool(consumerNum);

        // consume the messages in the threads
        for(final KafkaStream<byte[], byte[]> stream: streams) {
//            executor.submit(new Runnable() {
//                public void run() {
//                    try {
//                        for (MessageAndMetadata<byte[], byte[]> msgAndMetadata : stream) {
//                            // process message (msgAndMetadata.message())
//                            System.out.println(msgAndMetadata.message());
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
            ConsumerIterator<byte[], byte[]> it = stream.iterator();
            while (it.hasNext()) {
                MessageAndMetadata<byte[], byte[]> msg = it.next();
                byte[] bytes = msg.message();
                System.out.println(bytes);
                System.out.println(new String(bytes));
            }
        }
    }

    public static void main(String[] args) {
        new Consumer().run();
    }
}
