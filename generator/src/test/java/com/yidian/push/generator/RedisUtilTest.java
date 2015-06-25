package com.yidian.push.generator;

import org.testng.annotations.*;
import org.testng.annotations.Test;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/6/25.
 */
public class RedisUtilTest {
    private Jedis jedis = null;

    @BeforeMethod
    public void setUp() throws Exception {
        if (null == jedis) {
            jedis = new Jedis("10.111.0.108");
        }
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (null != jedis) {
            jedis.close();
        }
    }

    @Test
    public void testGet() throws Exception {
        RedisUtil.set(jedis, "key_get", "value_get");
        System.out.println(RedisUtil.get(jedis, "key_get"));
    }

    @Test
    public void testSet() throws Exception {
        RedisUtil.set(jedis, "key_set", "value_set");
        System.out.println(RedisUtil.get(jedis, "key_set"));
    }

    @Test
    public void testGetBatch() throws Exception {
        String[] keys = {"k1", "k2", "k3", "k4", "k5"};
        String[] values = {"v1", "v2", "v3", "v4", "v5"};
        for (int i = 0; i < keys.length; i ++) {
            RedisUtil.set(jedis, keys[i], values[i]);
        }
        System.out.println(RedisUtil.getBatch(jedis, Arrays.asList(keys)));
        System.out.println(RedisUtil.getBatch(jedis, Arrays.asList(keys), 2));

    }

    @Test
    public void testSetRedisBatchList() throws Exception {
        String[] keys = {"lk1", "lk2", "lk3", "lk4", "lk5"};
        String[] values = {"lv1", "lv2", "lv3", "lv4", "lv5"};
        RedisUtil.setRedisBatch(jedis, Arrays.asList(keys), Arrays.asList(values));
        RedisUtil.setRedisBatch(jedis, Arrays.asList(keys), Arrays.asList(values), 2);
        System.out.println(RedisUtil.getBatch(jedis, Arrays.asList(keys)));
    }

    @Test
    public void testSetRedisBatchMap() throws Exception {
        String[] keys = {"mk1", "mk2", "mk3", "mk4", "mk5"};
        String[] values = {"mv1", "mv2", "mv3", "mv4", "mv5"};
        Map<String, String> map = new HashMap<>(keys.length);
        for (int i = 0; i < keys.length; i ++) {
            map.put(keys[i], values[i]);
        }
        RedisUtil.setRedisBatch(jedis, map);
        RedisUtil.setRedisBatch(jedis, map, 2);
        System.out.println(RedisUtil.getBatch(jedis, Arrays.asList(keys)));

    }
}