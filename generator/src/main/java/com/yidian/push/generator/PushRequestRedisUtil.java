package com.yidian.push.generator;

import com.yidian.push.data.PushChannel;
import com.yidian.push.push_request.PushRecord;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.*;

/**
 * Created by tianyuzhi on 15/6/25.
 */
@Log4j
public class PushRequestRedisUtil {

    public static String genNidLastPushTimeKey(PushRecord pushRecord, int tableId) {
        return new StringBuilder().append(pushRecord.getUid()).append(',')
                .append(pushRecord.getAppId()).append(',')
                .append(tableId).toString();
    }



    public static List<String> genNidAndLastPushTimeKeys(Collection<PushRecord> pushRecords, int tableId) {
        if (null == pushRecords || pushRecords.size() == 0) {
            return new ArrayList<>();
        }
        List<String> keys = new ArrayList<>(pushRecords.size());
        for (PushRecord pushRecord : pushRecords) {
            String key = new StringBuilder().append(pushRecord.getUid()).append(',')
                    .append(pushRecord.getAppId()).append(',')
                    .append(tableId).toString();
            keys.add(key);
        }
        return keys;
    }

    public static Map<String, NidLastPushTimePair> getNidAndLastPushTime(int redisId, String table, Collection<PushRecord> pushRecords) {
        int tableId = Table.getTableId(table);
        Map<String, NidLastPushTimePair> result = new HashMap<>(pushRecords.size());
        List<String> queryKeys = new ArrayList<>(pushRecords.size());
        for (PushRecord pushRecord : pushRecords) {
            String key = genNidLastPushTimeKey(pushRecord, tableId);
            queryKeys.add(key);
            result.put(key, new NidLastPushTimePair(0, 0));
        }
        JedisPool jedisPool = null;
        Jedis jedis = null;
        try {
            jedisPool = RedisConnectionPool.getConnectionPool(redisId);
            jedis = jedisPool.getResource();
            Map<String, String> queryResult = RedisUtil.getBatch(jedis, queryKeys);
            for (String key : queryResult.keySet()) {
                String uidPushTime = queryResult.get(key);
                if (null == uidPushTime) {continue;}
                String[] arr = uidPushTime.split(",");
                if (arr.length == 2) {
                    int nid = 0;
                    long lastPushTime = 0;
                    try {
                        nid = Integer.parseInt(arr[0]);
                        lastPushTime = Long.parseLong(arr[1]);
                    } catch (Exception e) {
                        log.error("parse int err for key :" + key);
                    }
                    result.put(key, new NidLastPushTimePair(nid, lastPushTime));
                }
            }
        } catch (IOException e) {
           log.error("get nid and last push time failed, just use the random " + ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (jedis != null && jedisPool != null) {
                try {
                    jedisPool.returnResourceObject(jedis);
                } catch (Exception e) {
                    log.error("jedisPool return jedis connection failed.. with exception " + ExceptionUtils.getFullStackTrace(e));
                }
            }

        }
        return result;
    }

    public static String genPushChannelKey(PushRecord pushRecord, int tableId) {
        return new StringBuilder().append(pushRecord.getUid()).append(',')
                .append(pushRecord.getAppId()).append(',')
                .append(tableId).append(',')
                .append('p').toString();
    }


    private static String getUidAppIdKeyFromPushChannelKey(String pushChannelKey) {
        String[] arr = pushChannelKey.split(",");
        return new StringBuilder().append(arr[0]).append(',').append(arr[1]).toString();

    }

    public static List<String> genPushChannelKeys(Collection<PushRecord> pushRecords, int tableId) {
        if (null == pushRecords || pushRecords.size() == 0) {
            return new ArrayList<>();
        }
        List<String> keys = new ArrayList<>(pushRecords.size());
        for (PushRecord pushRecord : pushRecords) {
            String key = new StringBuilder().append(pushRecord.getUid()).append(',')
                    .append(pushRecord.getAppId()).append(',')
                    .append(tableId).append(',')
                    .append("p").toString();
            keys.add(key);
        }
        return keys;
    }

    public static Map<String, PushChannel> getPushChannel(int redisId, String table, Collection<PushRecord> pushRecords) {
        int tableId = Table.getTableId(table);
        Map<String, PushChannel> result = new HashMap<>(pushRecords.size());
        PushChannel defaultChannel = Table.isIPhone(table) ? PushChannel.IOS : PushChannel.XIAOMI;

        List<String> queryKeys = new ArrayList<>(pushRecords.size());
        for (PushRecord pushRecord : pushRecords) {
            String key = genPushChannelKey(pushRecord, tableId);
            queryKeys.add(key);
            String resultKey = pushRecord.getKey();
            result.put(resultKey, defaultChannel);
        }

        JedisPool jedisPool = null;
        Jedis jedis = null;
        try {
            jedisPool = RedisConnectionPool.getConnectionPool(redisId);
            jedis = jedisPool.getResource();
            Map<String, String> queryResult = RedisUtil.getBatch(jedis, queryKeys);
            for (String key : queryResult.keySet()) {
                String channelStr = queryResult.get(key);
                PushChannel pushChannel = PushChannel.findChannel(channelStr);
                if (null != pushChannel) {
                    result.put(getUidAppIdKeyFromPushChannelKey(key), pushChannel);
                }
            }
        } catch (IOException e) {
            log.error("get the push channel failed.  " + ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (jedis != null && jedisPool != null) {
                try {
                    jedisPool.returnResourceObject(jedis);
                } catch (Exception e) {
                    log.error("jedisPool return jedis connection failed.. with exception " + ExceptionUtils.getFullStackTrace(e));
                }
            }

        }
        return result;
    }

    public static void updateRedis(int redisId, Map<String, String> map) {
        JedisPool jedisPool = null;
        Jedis jedis = null;
        try {
            jedisPool = RedisConnectionPool.getConnectionPool(redisId);
            jedis = jedisPool.getResource();
            RedisUtil.setRedisBatch(jedis, map);
        } catch (IOException e) {
            log.error("update the redis failed.  " + ExceptionUtils.getFullStackTrace(e));
        } finally {
            if (null != jedis && null != jedisPool) {
                try {
                    jedisPool.returnResourceObject(jedis);
                } catch (Exception e) {
                    log.error("jedisPool return jedis connection failed.. with exception " + ExceptionUtils.getFullStackTrace(e));
                }
            }
        }
    }
}
