package com.yidian.push.instant.consumer;

import com.yidian.push.config.Config;
import com.yidian.push.config.InstantPushConfig;
import com.yidian.push.instant.data.DocChannelInfo;
import com.yidian.push.utils.GsonFactory;
import com.yidian.serving.metrics.MetricsFactoryUtil;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import kafka.utils.ZkUtils;
import lombok.extern.log4j.Log4j;

/**
 * Created by tianyuzhi on 15/12/22.
 */
@Log4j
public class ChannelConsumer {
    public static final String INPUT_QPS = "push_notification.install_push.related_channel_in.qps";
    //public static final String CALL_LATENCY = "push_notification.online_generator_call.latency";
    private static final String GROUP_ID = "group.id";
    private static final String ZOOKEEPER = "zookeeper.connect";
    private InstantPushConfig config = null;
    private LinkedBlockingQueue<String> docInfoStringQueue = new LinkedBlockingQueue();
    private LinkedBlockingQueue<DocChannelInfo> docChannelInfoQueue = new LinkedBlockingQueue<>();
    private List<FilterThread> threadPool = null;
    private ConsumerConnector consumerConnector = null;
    private InsertMongoThread insertMongoThread = null;
    private Timer getMongoQueueSizeTimer = null;

    public ChannelConsumer(InstantPushConfig config) {
        this.config = config;
        int threadNum = config.getProcessNum();
        int fetchSize = config.getProcessFetchSize();
        String queryFile = config.getQueryFile();
        threadPool = new ArrayList<>(threadNum);
        for (int i = 0 ; i < threadNum; i ++) {
            String name = "channel_consumer-" + i;
            FilterThread thread = new FilterThread(name, docInfoStringQueue,
                    docChannelInfoQueue,
                    fetchSize, queryFile);
            threadPool.add(thread);
            thread.start();
            log.info("start thread " + name);
        }
        insertMongoThread = new InsertMongoThread("insert_mongo", docChannelInfoQueue,fetchSize);
        insertMongoThread.start();
        log.info("start insert_mongo");
        getMongoQueueSizeTimer = new Timer("consumerTimer");
        getMongoQueueSizeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    log.info("docChannelInfoQueue size is :" + docChannelInfoQueue.size());
                } catch (Exception e) {
                    log.error("consumer Timer failed.");
                }
            }
        }, 0, 1000);
        log.info("start the consumer");
    }


    public synchronized void destroy() throws InterruptedException {
        if (null == threadPool) {
            return;
        }
        if (null != consumerConnector) {
            consumerConnector.shutdown();
            log.info("shutdown the consumer connector");
        }
        for (FilterThread thread : threadPool) {
            thread.interrupt();
            log.info("interrupt thread num : " + thread.getThreadName());
        }
        for (FilterThread thread : threadPool)
        {
            thread.interrupt();
            thread.join(5000);
            log.info("join thread num : " + thread.getThreadName());
        }
        getMongoQueueSizeTimer.cancel();
        insertMongoThread.interrupt();
        insertMongoThread.join();
        log.info("join insertMongoThread");
    }

    public void run2() {
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

        log.info("start ... ");

        String topicName = "relatedchannel";
        int consumerNum = 1;
        String zk = "hadoop2-13.lg-4-e10.yidian.com:2181,hadoop2-14.lg-4-e10.yidian.com:2181,hadoop2-2.lg-4-e9.yidian.com:2181";

        String groupId = "relatedchannel_group_id";
        properties.put("group.id", groupId);
        properties.put("zookeeper.connect", zk);
        properties.put("auto.offset.reset", "smallest");
        ZkUtils.maybeDeletePath(zk, "/consumers/" + groupId);

        ConsumerConfig consumerConfig = new ConsumerConfig(properties);
        ConsumerConnector consumerConnector = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);
        Map<String, Integer> map = new HashMap<>();
        map.put(topicName, consumerNum);

        Map<String, List<KafkaStream<byte[], byte[]>>> topicMessageStreams =
                consumerConnector.createMessageStreams(map);

        List<KafkaStream<byte[], byte[]>> streams = topicMessageStreams.get(topicName);

        // create list of # threads to consume from each of the partitions
        ExecutorService executor = Executors.newFixedThreadPool(consumerNum);

        try {
            // consume the messages in the threads
            for (final KafkaStream<byte[], byte[]> stream : streams) {
                executor.submit(new Runnable() {
                    public void run() {
                        try {
                            for (MessageAndMetadata<byte[], byte[]> msgAndMetadata : stream) {
                                // process message (msgAndMetadata.message())
                                byte[] bytes = msgAndMetadata.message();
                                System.out.println(bytes);
                                System.out.println(new String(bytes));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
//                ConsumerIterator<byte[], byte[]> it = stream.iterator();
//                while (it.hasNext()) {
//                    MessageAndMetadata<byte[], byte[]> msg = it.next();
//                    byte[] bytes = msg.message();
//                    System.out.println(bytes);
//                    System.out.println(new String(bytes));
//                }
            }
        } finally {
            log.info("shutdown the connector ... ");
            //consumerConnector.shutdown();
        }
    }




    public void run() {
        // Create the connection to the cluster
        Properties properties = config.getConsumerProperties();
        String topicName = config.getConsumerTopicName();
        int consumerNum = config.getTopicConsumerNum();
        String zk = config.getConsumerProperties().getProperty(ZOOKEEPER);
        String groupId = config.getConsumerProperties().getProperty(GROUP_ID, config.getConsumerTopicName() + "-" + new Random().nextInt());
        properties.put(GROUP_ID, groupId);
        ZkUtils.maybeDeletePath(zk, "/consumers/" + groupId);

        log.info("Consumer Properties: " + GsonFactory.getDefaultGson().toJson(properties));

        ConsumerConfig consumerConfig = new ConsumerConfig(properties);
        consumerConnector = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);

        Map<String, Integer> map = new HashMap<>();
        map.put(topicName, consumerNum);
        Map<String, List<KafkaStream<byte[], byte[]>>> topicMessageStreams =
                consumerConnector.createMessageStreams(map);

        List<KafkaStream<byte[], byte[]>> streams = topicMessageStreams.get(topicName);

        // create list of # threads to consume from each of the partitions
        ExecutorService executor = Executors.newFixedThreadPool(consumerNum);

        // consume the messages in the threads
        for (final KafkaStream<byte[], byte[]> stream : streams) {
            executor.submit(new Runnable() {
                public void run() {
                    try {
                        for (MessageAndMetadata<byte[], byte[]> msgAndMetadata : stream) {
                            byte[] bytes = msgAndMetadata.message();
                            System.out.println(new String(bytes));
                            docInfoStringQueue.add(new String(bytes));
                            MetricsFactoryUtil.getRegisteredFactory().getMeter(INPUT_QPS).mark();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
//            ConsumerIterator<byte[], byte[]> it = stream.iterator();
//            while (it.hasNext()) {
//                MessageAndMetadata<byte[], byte[]> msg = it.next();
//                byte[] bytes = msg.message();
//                System.out.println(bytes);
//                System.out.println(new String(bytes));
//            }
        }
        log.info("start the consumer for kafka topic");
    }

    public static void main(String[] args) throws IOException {
        Config.setCONFIG_FILE("instant_push/src/main/resources/config/config.json");
        new ChannelConsumer(Config.getInstance().getInstantPushConfig()).run();
    }


}
