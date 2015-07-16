package com.yidian.push.generator.services;

import com.yidian.push.config.Config;
import com.yidian.push.config.GeneratorConfig;
import com.yidian.push.generator.gen.MySqlConnectionPool;
import com.yidian.push.generator.gen.RedisConnectionPool;
import com.yidian.push.generator.gen.Generator;
import com.yidian.push.generator.gen.RefreshTokens;
import com.yidian.push.utils.FileLock;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by yidianadmin on 15-3-5.
 */
@Log4j
public class Service implements Runnable {
    private static volatile boolean keepRunning = false;

    @Override
    public void run() {
        GeneratorConfig generatorConfig = null;
        try {
            generatorConfig = Config.getInstance().getGeneratorConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            MySqlConnectionPool.init();
            RedisConnectionPool.init();
            keepRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("push notification generator init ...");
            throw new RuntimeException(e);
        }

        final Thread currentThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                keepRunning = false;
                log.info("receive kill signal ...");
                try {
                    currentThread.join();
                    MySqlConnectionPool.close();
                    RedisConnectionPool.close();
                    log.info("shutdown the thread pools");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    RefreshTokens.getInstance().refresh();
                } catch (Exception e) {
                    log.error("refresh tokens failed with exception : " + ExceptionUtils.getFullStackTrace(e));
                }
            }
        }, 0, generatorConfig.getRefreshTokenFrequencyInSeconds(), TimeUnit.SECONDS);

        int sleepTime = generatorConfig.getRequestScanIntervalInSeconds() * 1000;

        while(keepRunning) {
            Generator.process();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.error("sleep failed...");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(args.length);
        if (args.length >= 1) {
            String configFile = args[0];
            System.out.println("User specified config file " + configFile);
            Config.setCONFIG_FILE(configFile);
        } else {
           // Config.setCONFIG_FILE("generator/src/main/resources/config/prod_config.json");
            Config.setCONFIG_FILE("generator/src/main/resources/config/config2.json");
            System.setProperty("log4j.configuration", "src/main/resources/config/log4j_debug.properties");
            PropertyConfigurator.configure("generator/src/main/resources/config/log4j_debug.properties");
            Logger.getRootLogger().setLevel(Level.DEBUG);
        }
        String lockFile = Config.getInstance().getGeneratorConfig().getLockFile();
        if (!FileLock.lockInstance(lockFile)) {
            System.out.println("One instance is already running, just quit.");
            System.exit(1);
        }
        Service service = new Service();
        service.run();
    }
}
