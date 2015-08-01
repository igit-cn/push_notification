package com.yidian.push.config;

import com.yidian.push.data.Environment;
import com.yidian.push.data.HostPortDB;
import com.yidian.push.data.Platform;
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
    private int validMaxTitleLength = 15;
    private int iPhoneRangeSize = 20000000;
    private int androidRangeSize = 300000;
    private int startTime = 6 * 60;
    private int endTime = 24 * 60 - 1;
    private int requestScanIntervalInSeconds = 3;

    private int iPhoneThreadPoolSize = 10;
    private int androidThreadPoolSize = 40;
    private int secondsToWaitThreadPoolShutDownTimeout = 5 * 60;
    private String pushAllSqlFields = "userid, token, push_level, appid, enable, time_zone, version";
    private String pushAllIndexFile = "config/push_all_index";
    private String pushAllBaseDir = "/tmp/push_all";
    private int pushAllThreadPoolSize = 50;
    private int mysqlFetchSize = 30000;
    private int generateRequestBatchSize = 10000;
    // today first userid
    private String minUserFilePath = "/Users/tianyuzhi/work/push_notification/trunk/cache/min_new_user_uid";
    private String minUserFilePrefix = "host_table_userid";
    private int minUserLookBackDays = 4;
    // channel to users cache path :
    private String cacheBasePath = "/home/services/push_notification/cache";
    // local channel cache path
    private String localChannelCachePath = "/home/services/push_services/local_news/cache";
    private String localChannelMappingFile = "/home/services/push_services/local_news/cache/location_channel_cache.data";
    private int autoLocalCacheIndex = 4;
    private int autoRecommendCacheIndex = 2;
    private int maxCacheIndex = 3;
    private int pushToUsersBatchSize = 10000;
    // inactive users cache path
    private String inactiveUserFilePath = "/home/services/push_notification/cache/recommend_push_users";
    private String inactiveUserFilePrefix = "inactive_users";
    private int inactiveUserLookBackDays = 4;
    // refresh frequency
    private int refreshTokenFrequencyInSeconds = 6000;
    private boolean needSendNotification = false;



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

    public int getRangeSize(String table) {
        if (Platform.isIPhone(table)) {
            return iPhoneRangeSize;
        }
        else {
            return androidRangeSize;
        }
    }

    public int getPoolSize(String table) {
        if (Platform.isIPhone(table)) {
            return iPhoneThreadPoolSize;
        }
        else {
            return androidThreadPoolSize;
        }
    }
}
