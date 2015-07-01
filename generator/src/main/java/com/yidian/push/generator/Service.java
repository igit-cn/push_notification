package com.yidian.push.generator;

import com.yidian.push.config.Config;
import com.yidian.push.generator.gen.Generator;
import com.yidian.push.generator.request.Request;
import com.yidian.push.generator.request.RequestManager;
import com.yidian.push.generator.request.RequestStatus;
import com.yidian.push.utils.FileLock;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.List;

/**
 * Created by yidianadmin on 15-3-5.
 */
@Log4j
public class Service implements Runnable {
    private static volatile boolean keepRunning = false;

    @Override
    public void run() {
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

        int sleepTime = 0;
        try {
            sleepTime = Config.getInstance().getGeneratorConfig().getSleepSeconds() * 1000;
        } catch (IOException e) {
            log.error("get the sleep time failed. just use the default: 3s ");
            sleepTime = 3 * 1000;
        }
        while(keepRunning) {
            Generator.process();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.error("sleep failed...");
            }
        }
    }

    public static void main(String[] args) {
        String lockFile = "push_request_generator.lock";
        if (!FileLock.lockInstance(lockFile)) {
            System.out.println("One instance is already running, just quit.");
            System.exit(1);
        }

        System.out.println(args.length);
        if (args.length >= 1) {
            String configFile = args[0];
            System.out.println("User specified config file " + configFile);
            Config.setCONFIG_FILE(configFile);
        } else {
            Config.setCONFIG_FILE("generator/src/main/resources/config/config.json");
            System.setProperty("log4j.configuration", "src/main/resources/config/log4j_debug.properties");
            PropertyConfigurator.configure("generator/src/main/resources/config/log4j_debug.properties");
            Logger.getRootLogger().setLevel(Level.DEBUG);
        }
        Service service = new Service();
        service.run();
    }
}
