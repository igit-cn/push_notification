package com.yidian.push.generator;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/6/25.
 */
public class RedisUtil {
    public static final int BATCH_SIZE = 5000;

    public static String get(Jedis jedis, String key) {
        return jedis.get(key);
    }

    public static void set(Jedis jedis, String key, String value) {
        jedis.set(key, value);
    }

    public static Map<String, String> getBatch(Jedis jedis, List<String> keys) {
        return getBatch(jedis, keys, BATCH_SIZE);
    }

    public static Map<String, String> getBatch(Jedis jedis, List<String> keys, int batchQuerySize) {
        Map<String, String> res = new HashMap<>(keys.size());
        int numKeys = keys.size();
        int idx = 0;
        while (idx < numKeys) {
            Pipeline pipeline = jedis.pipelined();
            int subStart = idx;
            int subEnd = idx + batchQuerySize >= numKeys ? numKeys : idx + batchQuerySize;
            for (int i = subStart; i < subEnd; i++) {
                pipeline.get(keys.get(i));
            }
            List<Object> objects = pipeline.syncAndReturnAll();
            if (null != objects && objects.size() == subEnd - subStart) {
                int keyIdx = subStart;
                for (Object obj : objects) {
                    String value = (String) obj;
                    res.put(keys.get(keyIdx), value);
                    keyIdx++;
                }
            }
            idx = subEnd;
        }
        return res;
    }

    public static void setRedisBatch(Jedis jedis, List<String> keys, List<String> values) {
        setRedisBatch(jedis, keys, values, BATCH_SIZE);
    }

    /**
     * ArrayList would have a better performance than the LinkedList.
     *
     * @param jedis
     * @param keys
     * @param values
     * @param batchQuerySize
     */
    public static void setRedisBatch(Jedis jedis, List<String> keys, List<String> values, int batchQuerySize) {
        int numKeys = Math.min(keys.size(), values.size());
        int idx = 0;


        while (idx < numKeys) {
            Pipeline pipeline = jedis.pipelined();
            int subStart = idx;
            int subEnd = idx + batchQuerySize >= numKeys ? numKeys : idx + batchQuerySize;
            for (int i = subStart; i < subEnd; i++) {
                pipeline.set(keys.get(i), values.get(i));
            }
            pipeline.syncAndReturnAll();
            idx = subEnd;
        }
    }

    public static void setRedisBatch(Jedis jedis, Map<String, String> keyValues) {
        setRedisBatch(jedis, keyValues, BATCH_SIZE);
    }

    public static void setRedisBatch(Jedis jedis, Map<String, String> keyValues, int batchQuerySize) {
        if (null == keyValues || keyValues.size() == 0) {
            return;
        }
        int numKeys = keyValues.size();
        Pipeline pipeline = null;
        try {
            int idx = 0;
            for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                boolean isBatchStart = (idx == 0 || idx % batchQuerySize == 0);
                boolean isBatchEnd = (idx+1 == numKeys || (idx + 1) % batchQuerySize == 0);
                if (isBatchStart) {
                    pipeline = jedis.pipelined();
                }
                pipeline.set(key, value);
                if (isBatchEnd) {
                    pipeline.syncAndReturnAll();
                }
                idx++;
            }
        } finally {
            if (pipeline != null) {
                pipeline.syncAndReturnAll();
            }
        }
    }
}
