package com.yidian.push.utils;

import com.data.client.zion.Zionpool;
import com.yidian.push.config.Config;
import com.yidian.push.config.PushHistoryConfig;
import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.List;

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
                zionpool = new Zionpool(config.getYClusterServiceName(), config.getZookeeperAddress());
                log.info("start the ZionPool");
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

    public static void addPushHistoryRecords(List<String> uidRecordList, int batchQuerySize, int keepRecordSize) throws IOException {
        if (!isInitialized) {
            init();
        }
        Jedis jedis = null;

        int numKeys = uidRecordList.size();
        Pipeline pipeline = null;
        try {
            int idx = 0;
            for (String uidRecord : uidRecordList) {
                String[] arr = uidRecord.split(SEPARATOR);
                if (arr.length != 2) {
                    continue;
                }
                String key = arr[0];
                String value = arr[1];

                boolean isBatchStart = (idx == 0 || idx % batchQuerySize == 0);
                boolean isBatchEnd = (idx+1 == numKeys || (idx + 1) % batchQuerySize == 0);
                if (isBatchStart) {
                    pipeline = jedis.pipelined();
                }
                pipeline.set(key, value);
                pipeline.lpush(key, value);
                pipeline.ltrim(key, 0, keepRecordSize-1);
                if (isBatchEnd) {
                    pipeline.syncAndReturnAll();
                }
                idx++;
            }
        } finally {
            if (pipeline != null) {
                pipeline.syncAndReturnAll();
            }
            if (jedis != null) {
                jedis.close();
            }
        }

    }
}
