package com.yidian.push.utils;

import com.data.client.zion.Zionpool;
import com.yidian.push.config.Config;
import com.yidian.push.config.PushHistoryConfig;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.eclipse.jetty.util.StringUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by tianyuzhi on 15/9/2.
 */
@Log4j
public class ZionPoolUtil {
    @Getter
    public static class ConsumerThread extends Thread {
        private String threadName;
        private int threadId;
        private BlockingQueue<String> pushLogQueue;
        private int fetchSize = 1000;
        private List<String> recordList;

        public ConsumerThread(String threadName, int threadId, BlockingQueue<String> pushLogQueue, int fetchSize) {
            super(threadName);
            this.threadName = threadName;
            this.threadId = threadId;
            this.pushLogQueue = pushLogQueue;
            this.fetchSize = fetchSize;
            this.recordList = new ArrayList<>(fetchSize);
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (pushLogQueue.isEmpty()) {
                        try {
                            Thread.sleep(1 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        log.debug(threadName + " queue is empty");
                        continue;
                    }
                    recordList.clear();
                    int num = pushLogQueue.drainTo(recordList, fetchSize);
                    if (num > 0) {
                        try {
                            log.debug(threadName + " : got # of records to push: " + recordList.size());
                            ZionPoolUtil.addPushHistoryRecords(recordList);
                        } catch (Exception e) {
                            log.error(threadName + " consume record failed..." + ExceptionUtils.getFullStackTrace(e));
                        }
                    }
                } catch (Exception e) {
                    log.error(threadName + " : run method failed withe exception " + ExceptionUtils.getFullStackTrace(e));
                }
            }

        }
    }


    private static final String SEPARATOR = "\u0001";

    private static volatile boolean isInitialized = false;
    private static Zionpool zionpool = null;
    private static PushHistoryConfig config = null;
    private static BlockingQueue blockingQueue = new LinkedBlockingQueue();
    private static List<ConsumerThread> threadPool = null;

    public static void init() throws IOException {
        if (!isInitialized) {
            synchronized (ZionPoolUtil.class) {
                if (isInitialized) {
                    return;
                }
                config = Config.getInstance().getPushHistoryConfig();
                String serviceName = config.getYClusterServiceName();
                String zooKeeperAddress = config.getZookeeperAddress();
                GenericObjectPoolConfig jedisConfig = config.getJedisConfig();

                if (null == jedisConfig) {
                    zionpool = new Zionpool(serviceName, zooKeeperAddress);
                } else {
                    zionpool = new Zionpool(serviceName, zooKeeperAddress, jedisConfig);
                }
                log.info("start the ZionPool");
                int consumerNum = config.getConsumerNum();
                threadPool = new ArrayList<>(consumerNum);
                for (int i = 0; i < consumerNum; i++) {
                    String threadName = "consumer: " + i;
                    ConsumerThread thread = new ConsumerThread(threadName, i, blockingQueue, config.getProducerFetchSize());
                    threadPool.add(thread);
                    thread.start();
                    log.info("start producer :" + i);
                }

                isInitialized = true;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        log.info("queue size : " + blockingQueue.size());
                    }
                }, 0, 10000);

            }
        }
    }

    public synchronized static void destroy() throws InterruptedException {
        if (!isInitialized) {
            return;
        }
        for (ConsumerThread thread : threadPool) {
            thread.interrupt();
            log.info("interrupt thread num : " + thread.getThreadId());
        }
        for (ConsumerThread thread : threadPool) {
            thread.join(5000);
            log.info("join thread num : " + thread.getThreadId());
        }
        isInitialized = false;
    }

    public static void producer(List<String> uidRecordList) {
        if (null == uidRecordList || uidRecordList.size() == 0) {
            return;
        }
        for (String record : uidRecordList) {
            try {
                blockingQueue.put(record);
            } catch (InterruptedException e) {
                log.error("could not produce the record");
            }
        }
    }


    public static void addPushHistoryRecords(List<String> uidRecordList) throws IOException {
        if (!isInitialized) {
            init();
        }
        int batchSize = config.getRedisUpdateBatchSize();
        int keepRecordSize = config.getRedisKeepRecordSize();
        //addPushHistoryRecords(uidRecordList, batchSize, keepRecordSize);
        addPushHistoryRecords2(uidRecordList, batchSize, keepRecordSize);
    }

    public static Map<String, Long> getRecordLength(List<String> keyList, int batchQuerySize) throws IOException {
        if (!isInitialized) {
            init();
        }
        Jedis jedis = null;

        int numKeys = keyList.size();
        Map<String, Long> keyRecordLengthMap = new HashMap<>(numKeys);
        for (String key : keyList) {
            keyRecordLengthMap.put(key, 0L);
        }

        try {
            jedis = zionpool.getClient();
            int idx = 0;
            while (idx < numKeys) {
                Pipeline pipeline = jedis.pipelined();
                int subStart = idx;
                int subEnd = idx + batchQuerySize >= numKeys ? numKeys : idx + batchQuerySize;
                for (int i = subStart; i < subEnd; i++) {
                    pipeline.llen(keyList.get(i));
                }
                List<Object> objects = pipeline.syncAndReturnAll();
                if (null != objects && objects.size() == subEnd - subStart) {
                    int keyIdx = subStart;
                    for (Object obj : objects) {
                        Long value = (Long) obj;
                        keyRecordLengthMap.put(keyList.get(keyIdx), value);
                        keyIdx++;
                    }
                }
                idx = subEnd;
            }
        } catch (Exception e) {
            log.error("get push history length with exception: " + ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (jedis != null) {
                try {
                    log.debug("try to return the jedis client in getRecordLength " + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
                    jedis.close();
                } catch (Exception e) {
                    //ignore
                    log.error("failed to return the jedis client in getRecordLength " + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
                }
            }
        }
        return keyRecordLengthMap;
    }

    public static void addPushHistoryRecords(List<String> uidRecordList, int batchQuerySize, int keepRecordSize) throws IOException {
        if (!isInitialized) {
            init();
        }
        Jedis jedis = null;

        int numKeys = uidRecordList.size();
        Pipeline pipeline = null;
        try {
            List<String> keyList = new ArrayList<>(numKeys);
            List<String> valueList = new ArrayList<>(numKeys);
            for (String uidRecord : uidRecordList) {
                String[] arr = uidRecord.split(SEPARATOR);
                if (arr.length != 2) {
                    continue;
                }
                String key = arr[0];
                String value = arr[1];
                keyList.add(key);
                valueList.add(value);
            }
            // get push history length
            Map<String, Long> keyRecordLengthMap = getRecordLength(keyList, batchQuerySize);

            // update
            jedis = zionpool.getClient();
            for (int idx = 0; idx < numKeys; idx++) {
                String key = keyList.get(idx);
                String value = valueList.get(idx);

                boolean isBatchStart = (idx == 0 || idx % batchQuerySize == 0);
                boolean isBatchEnd = (idx + 1 == numKeys || (idx + 1) % batchQuerySize == 0);
                if (isBatchStart) {
                    pipeline = jedis.pipelined();
                }
                pipeline.lpush(key, value);
                long curRecordNum = keyRecordLengthMap.get(key) + 1;
                if (curRecordNum > keepRecordSize) {
                    while (curRecordNum > keepRecordSize) {
                        pipeline.rpop(key);
                        curRecordNum--;
                    }
                }
                if (isBatchEnd) {
                    pipeline.syncAndReturnAll();
                    //System.out.println(GsonFactory.getNonPrettyGson().toJson(res));
                }
            }
        } catch (Exception e) {
            log.error("write push history failed with exception: " + ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (jedis != null) {
                try {
                    log.debug("try to return the jedis client in addPushHistoryRecords " + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
                    jedis.close();
                } catch (Exception e) {
                    // ignore
                    log.error("failed to return the jedis client in addPushHistoryRecords "  + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
                }
            }
        }

    }

    public static Map<String, String> getRecords(List<String> keyList, int batchQuerySize) throws IOException {
        if (!isInitialized) {
            init();
        }
        Jedis jedis = null;

        int numKeys = keyList.size();
        Map<String, String> keyValueMap = new HashMap<>(numKeys);

        try {
            jedis = zionpool.getClient();
            int idx = 0;
            while (idx < numKeys) {
                Pipeline pipeline = jedis.pipelined();
                int subStart = idx;
                int subEnd = idx + batchQuerySize >= numKeys ? numKeys : idx + batchQuerySize;
                for (int i = subStart; i < subEnd; i++) {
                    pipeline.get(keyList.get(i));
                }
                List<Object> objects = pipeline.syncAndReturnAll();
                if (null != objects && objects.size() == subEnd - subStart) {
                    int keyIdx = subStart;
                    for (Object obj : objects) {
                        String value = (String) obj;
                        keyValueMap.put(keyList.get(keyIdx), value);
                        keyIdx++;
                    }
                }
                idx = subEnd;
            }
        } catch (Exception e) {
            log.error("get push history length with exception: " + ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (jedis != null) {
                try {
                    log.debug("try to return the jedis client in getRecordLength " + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
                    jedis.close();
                } catch (Exception e) {
                    //ignore
                    log.error("failed to return the jedis client in getRecordLength " + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
                }
            }
        }
        return keyValueMap;
    }

    public static void addPushHistoryRecords2(List<String> uidRecordList, int batchQuerySize, int keepRecordSize) throws IOException {
        if (!isInitialized) {
            init();
        }
        Jedis jedis = null;

        int numKeys = uidRecordList.size();
        Pipeline pipeline = null;
        try {
            List<String> keyList = new ArrayList<>(numKeys);
            List<String> valueList = new ArrayList<>(numKeys);
            for (String uidRecord : uidRecordList) {
                String[] arr = uidRecord.split(SEPARATOR);
                if (arr.length != 2) {
                    continue;
                }
                String key = arr[0];
                String value = arr[1];
                keyList.add(key);
                valueList.add(value);
            }
            // get push history length
            Map<String, String> curKeyValues = getRecords(keyList, batchQuerySize);

            // update
            jedis = zionpool.getClient();
            for (int idx = 0; idx < numKeys; idx++) {
                String key = keyList.get(idx);
                String value = valueList.get(idx);

                boolean isBatchStart = (idx == 0 || idx % batchQuerySize == 0);
                boolean isBatchEnd = (idx + 1 == numKeys || (idx + 1) % batchQuerySize == 0);
                if (isBatchStart) {
                    pipeline = jedis.pipelined();
                }
                String curValue = curKeyValues.get(key);
                if (StringUtils.isEmpty(curValue)) {
                    pipeline.set(key, value);
                } else {
                    List<String> arr = Arrays.asList(curValue.split(SEPARATOR));
                    String newVal;
                    if (arr.size() >= keepRecordSize) {
                        newVal = StringUtils.join(arr, SEPARATOR, 0, keepRecordSize - 1);
                    }
                    else {
                        newVal = curValue;
                    }
                    if (StringUtils.isEmpty(newVal)) {
                        newVal = value;
                    } else {
                        newVal = new StringBuilder(value).append(SEPARATOR).append(newVal).toString();
                    }
                    pipeline.set(key, newVal);
                }
                if (isBatchEnd) {
                    pipeline.syncAndReturnAll();
                    //System.out.println(GsonFactory.getNonPrettyGson().toJson(res));
                }
            }
        } catch (Exception e) {
            log.error("write push history failed with exception: " + ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (jedis != null) {
                try {
                    log.debug("try to return the jedis client in addPushHistoryRecords " + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
                    jedis.close();
                } catch (Exception e) {
                    // ignore
                    log.error("failed to return the jedis client in addPushHistoryRecords "  + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
                }
            }
        }

    }
}
