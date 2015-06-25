package com.yidian.push.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by tianyuzhi on 15/6/15.
 */
@Getter
@Setter
public class TomcatDBCPConfig {
    private String db = "account";
    private String driverClassName = "com.mysql.jdbc.Driver";
    private String userName = "account";
    private String password = "&t8DFylw3(r";
    private boolean isJmsEnabled = true;
    private boolean isTestWhileIdle = false;
    private boolean isTestOnBorrow = true;
    private String validationQuery = "SELECT 1";
    private boolean isTestOnReturn = false;
    private int validationInterval = 30000;
    private int timeBetweenEvictionRunsMillis = 30000;
    private int maxActive = 100;
    private int initialSize = 10;
    private int maxWait = 10000;
    private int removeAbandonedTimeout = 60;
    private int minEvictableIdleTimeMillis = 30000;
    private int minIdle = 10;
    private boolean logAbandoned = true;
    private boolean removeAbandoned = true;
    private String JdbcInterceptors = "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
            "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer";

}
