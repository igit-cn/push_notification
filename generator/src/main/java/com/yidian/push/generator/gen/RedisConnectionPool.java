package com.yidian.push.generator.gen;

import com.yidian.push.config.Config;
import com.yidian.push.config.GeneratorConfig;
import com.yidian.push.data.RedisHostPort;
import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by tianyuzhi on 15/6/16.
 */
@Log4j
public class RedisConnectionPool {
    private static HashMap<Integer, JedisPool> CachedJedisPool = new HashMap<>(5);
    private static HashMap<Integer, RedisHostPort> CachedRedisHostPort = new HashMap<>(5);
    private static volatile boolean isInitialized = false;
    private static volatile JedisPoolConfig jedisPoolConfig = null;
    public static void init() throws IOException {
        if (isInitialized) {
            return;
        }
        GeneratorConfig config = Config.getInstance().getGeneratorConfig();
        jedisPoolConfig = config.getJedisPoolConfig();
        for (RedisHostPort redisHostPort : config.getREDIS_HOSTS()) {
            JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisHostPort.getHost(), redisHostPort.getPort());
            CachedJedisPool.put(redisHostPort.getId(), jedisPool);
            CachedRedisHostPort.put(redisHostPort.getId(), redisHostPort);
            log.info("init the jedis connection pool " + redisHostPort.toJson());
        }
        isInitialized = true;
    }



    public static void close() {
        if (isInitialized && null != CachedJedisPool) {
            for (Integer id : CachedJedisPool.keySet()) {
                JedisPool jedisPool = CachedJedisPool.get(id);
                if (null != jedisPool) {
                    jedisPool.close();
                    log.info("close connection pool" + CachedRedisHostPort.get(id).toJson());
                }
            }
        }
    }

    public static JedisPool getConnectionPool(int id) throws IOException {
        if (!isInitialized) {
            init();
        }
        if (CachedJedisPool.containsKey(id)) {
            JedisPool pool = CachedJedisPool.get(id);
            return pool;
        }
        return null;
    }
}
