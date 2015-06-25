package com.yidian.push.config;

import com.yidian.push.utils.GsonFactory;
import org.testng.annotations.Test;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by tianyuzhi on 15/6/16.
 */
public class GeneratorConfigTest {

    @Test
    public void testJedisConfigLoading() {
        String str = "{" +
                "            \"maxTotal\" : 10,\n" +
                "            \"maxIdle\" : 5,\n" +
                "            \"minIdle\" : 3,\n" +
                "            \"testOnBorrow\" : true,\n" +
                "            \"testOnReturn\" : true,\n" +
                "            \"testWhileIdle\" : true\n" +
                "        }";
        JedisPoolConfig jedisPoolConfig = GsonFactory.getDefaultGson().fromJson(str, JedisPoolConfig.class);
        System.out.println(GsonFactory.getPrettyGson().toJson(jedisPoolConfig));
        System.out.println();
    }
}