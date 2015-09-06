package com.yidian.push.utils;

import com.data.client.zion.Zionpool;
import com.yidian.push.config.Config;
import com.yidian.push.config.PushHistoryConfig;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/9/2.
 */
@Log4j
public class ZionPoolUtil {
    private static final String SEPARATOR = "\u0001";

    private static volatile boolean isInitialized = false;
    private static Zionpool zionpool = null;
    private static PushHistoryConfig config = null;

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
                }
                else {
                    zionpool = new Zionpool(serviceName, zooKeeperAddress, jedisConfig);
                }
                log.info("start the ZionPool");
                isInitialized = true;
            }
        }
    }

    public synchronized void destroy() {
        if (isInitialized) {
            // TODO : clean work here
        }
    }

    public static void addPushHistoryRecords(List<String> uidRecordList) throws IOException {
        if (!isInitialized) {
            init();
        }
        int batchSize = config.getRedisUpdateBatchSize();
        int keepRecordSize = config.getRedisKeepRecordSize();
        addPushHistoryRecords(uidRecordList, batchSize, keepRecordSize);
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
        }
        finally {
            if (jedis != null) {
                try {
                    jedis.close();
                }
                catch (Exception e) {
                    //ignore
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
            for (int idx = 0; idx < numKeys; idx ++) {
                String key = keyList.get(idx);
                String value = valueList.get(idx);

                boolean isBatchStart = (idx == 0 || idx % batchQuerySize == 0);
                boolean isBatchEnd = (idx+1 == numKeys || (idx + 1) % batchQuerySize == 0);
                if (isBatchStart) {
                    pipeline = jedis.pipelined();
                }
                pipeline.lpush(key, value);
                long curRecordNum = keyRecordLengthMap.get(key) + 1;
                if (curRecordNum > keepRecordSize) {
                    while (curRecordNum > keepRecordSize) {
                        pipeline.rpop(key);
                        curRecordNum --;
                    }
                }
                if (isBatchEnd) {
                    List<Object> res = pipeline.syncAndReturnAll();
                    //System.out.println(GsonFactory.getNonPrettyGson().toJson(res));
                }
            }
        } catch (Exception e) {
            log.error("write push history failed with exception: " + ExceptionUtils.getFullStackTrace(e));
        }
        finally {
            if (pipeline != null) {
                pipeline.syncAndReturnAll();
            }
            if (jedis != null) {
                jedis.close();
            }
        }

    }
}
