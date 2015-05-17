package com.yidian.push.config;

import com.yidian.push.data.Environment;
import com.yidian.push.data.HostPortPair;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by yidianadmin on 15-2-2.
 */
@Getter
@Setter
public class GeneratorConfig {
    private Environment environment = Environment.TEST;
    private String[] APPID_YIDIAN;
    private String[] APPID_XIAOMI;
    private String MYSQL_USER;
    private String MYSQL_PASSWORD;
    private String MYSQL_DB;
    private HostPortPair[] MYSQL_HOSTS;
    private HostPortPair[] REDIS_HOSTS;

    public int getMysqlId(HostPortPair hostPortPair) {
        if (null == MYSQL_HOSTS || MYSQL_HOSTS.length == 0) {
            return -1;
        }
        for (int i = 0; i < MYSQL_HOSTS.length; i ++) {
            HostPortPair anHostPair = MYSQL_HOSTS[i];
            if (anHostPair.getHost().equals(hostPortPair.getHost())
                    && anHostPair.getPort() == hostPortPair.getPort()) {
                return i;
            }
        }
        return -1;
    }



}
