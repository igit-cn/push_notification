package com.yidian.push.config;

import com.yidian.push.data.Environment;
import com.yidian.push.data.HostPortDB;
import com.yidian.push.data.RedisHostPort;
import lombok.Getter;
import lombok.Setter;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * Created by yidianadmin on 15-2-2.
 */
@Getter
@Setter
public class GeneratorConfig {
    private String lockFile = "/tmp/push_notification_generator.lck";
    private String basePath = null;
    private Environment environment = Environment.TEST;
    private List<String> APPID_YIDIAN;
    private List<String> APPID_XIAOMI;
    private String MYSQL_USER;
    private String MYSQL_PASSWORD;
    private String MYSQL_DB;
    private List<HostPortDB> MYSQL_HOSTS;
    private List<RedisHostPort> REDIS_HOSTS;
    private PoolProperties tomcatDBCPProperties;
    private JedisPoolConfig jedisPoolConfig;
    private int maxUserId = 0;
    private int validMaxHeadLength = 15;
    private int iPhoneRangeSize = 20000000;
    private int androidRangeSize = 300000;
    private int startTime = 6 * 60;
    private int endTime = 24 * 60 - 1;

    public int getMysqlId(HostPortDB hostPortDB) {
        if (null == MYSQL_HOSTS || MYSQL_HOSTS.size() == 0) {
            return -1;
        }
        for (int i = 0; i < MYSQL_HOSTS.size(); i ++) {
            HostPortDB anHostPair = MYSQL_HOSTS.get(i);
            if (anHostPair.getHost().equals(hostPortDB.getHost())
                    && anHostPair.getPort() == hostPortDB.getPort()) {
                return i;
            }
        }
        return -1;
    }
}
