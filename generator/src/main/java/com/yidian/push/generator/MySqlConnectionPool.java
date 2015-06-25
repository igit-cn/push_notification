package com.yidian.push.generator;

import com.yidian.push.config.Config;
import com.yidian.push.config.GeneratorConfig;
import com.yidian.push.data.HostPortDB;
import lombok.extern.log4j.Log4j;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by tianyuzhi on 15/6/15.
 */
@Log4j
public class MySqlConnectionPool {
    private static volatile  boolean isInitialized = false;
    private static HashMap<String, DataSource> CachedDataSources = new HashMap<>(3);

    /**
     * @ Not thread safe here
     * @throws IOException
     */
    public static void init() throws IOException {
        if (isInitialized) {
            return;
        }
        GeneratorConfig generatorConfig = Config.getInstance().getGeneratorConfig();
        PoolProperties poolProperties = generatorConfig.getTomcatDBCPProperties();
        for (HostPortDB hostPortDB : generatorConfig.getMYSQL_HOSTS()) {
            String key = hostPortDB.getMysqlUrl();
            if (CachedDataSources.containsKey(key)) {
                log.error(key + " already initialized..");
                continue;
            }
            DataSource dataSource = createDataSource(hostPortDB, poolProperties);
            if (dataSource != null) {
                CachedDataSources.put(key, dataSource);
                log.info("init the connection pool for " + key);
            }
        }
    }

    private static DataSource getCachedDataSource(String key) {
        DataSource dataSource = CachedDataSources.get(key);
        return dataSource;
    }

    public static DataSource createDataSource(HostPortDB hostPortDB, PoolProperties poolProperties) {
        poolProperties.setUrl(hostPortDB.getMysqlUrl());
        DataSource dataSource = new DataSource();
        dataSource.setPoolProperties(poolProperties);
        return dataSource;
    }

    public static Connection getConnection(HostPortDB hostPortDB) throws IOException, SQLException {
        String key = hostPortDB.getMysqlUrl();
        DataSource dataSource = getCachedDataSource(key);
        if (null == dataSource) {
            synchronized (key.intern()) {
                dataSource = getCachedDataSource(key);
                if (null == dataSource) {
                    dataSource = createDataSource(hostPortDB, Config.getInstance().getGeneratorConfig().getTomcatDBCPProperties());
                    CachedDataSources.put(key, dataSource);
                }
            }
        }
        return dataSource.getConnection();
    }

    public static void close(){
        if (null != CachedDataSources && CachedDataSources.size() > 0) {
            for (String key : CachedDataSources.keySet()) {
                DataSource dataSource = CachedDataSources.get(key);
                if (null != dataSource) {
                    dataSource.close();
                }
                log.info("close the connection pool for " + key);
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        HostPortDB hostPortDB = new HostPortDB("10.111.2.53", 3306, "account");
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setPassword("`c2#^@j1T)oX:_");
        poolProperties.setUsername("account");
        poolProperties.setDriverClassName("com.mysql.jdbc.Driver");
;
        DataSource  dataSource = createDataSource(hostPortDB, poolProperties);
        Connection connection = dataSource.getConnection();
        System.out.println(connection.getNetworkTimeout());
        System.out.println(dataSource.getPoolSize());

        DataSource dataSource2 = createDataSource(hostPortDB, poolProperties);
        Connection connection2 = dataSource2.getConnection();
        System.out.println(connection2.getNetworkTimeout());
        System.out.println(dataSource2.getPoolSize());

        HostPortDB hostPortDB2 = new HostPortDB("account2.yidian.com", 3306, "account");
        DataSource dataSource3 = createDataSource(hostPortDB2, poolProperties);
        Connection connection3 = dataSource3.getConnection();
        System.out.println(connection3.getNetworkTimeout());
        System.out.println(dataSource3.getPoolSize());

        dataSource.close();
        dataSource2.close();
        dataSource3.close();;
        System.out.println("done");
    }

}
