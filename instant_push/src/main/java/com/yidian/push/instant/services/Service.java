package com.yidian.push.instant.services;

import com.yidian.push.config.Config;
import com.yidian.push.config.InstantPushConfig;
import com.yidian.push.instant.ChannelConsumer;

import com.yidian.push.utils.FileLock;
import com.yidian.push.utils.GsonFactory;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

/**
 * Created by yidianadmin on 15-3-5.
 */
@Log4j
public class Service implements Runnable {
    private static volatile boolean keepRunning = false;

    @Override
    public void run() {
        InstantPushConfig config = null;
        try {
            config = Config.getInstance().getInstantPushConfig();
            log.info("Logging config is " + GsonFactory.getPrettyGson().toJson(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            keepRunning = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            log.error("push notification logging init ...");
//            throw new RuntimeException(e);
//        }

        final Thread currentThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                keepRunning = false;
                log.info("receive kill signal ...");
                try {
                    currentThread.join();
                    log.info("shutdown the thread pools");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        int sleepTime = config.getRequestScanIntervalInSeconds() * 1000;

        new ChannelConsumer(config).run2();

    }

    public static void main(String[] args) throws IOException {
//        Logger.getRootLogger().setLevel(Level.DEBUG);
//        Config.setCONFIG_FILE("instant_push/src/main/resources/config/config.json");

        //new ChannelConsumer(Config.getInstance().getInstantPushConfig()).run2();;

        System.out.println(args.length);
        if (args.length >= 1) {
            String configFile = args[0];
            System.out.println("User specified config file " + configFile);
            Config.setCONFIG_FILE(configFile);
        } else {
            Config.setCONFIG_FILE("instant_push/src/main/resources/config/config.json");
            System.setProperty("log4j.configuration", "src/main/resources/config/log4j_debug.properties");
            PropertyConfigurator.configure("instant_push/src/main/resources/config/log4j_debug.properties");
            Logger.getRootLogger().setLevel(Level.DEBUG);
        }
        String lockFile = Config.getInstance().getInstantPushConfig().getLockFile();
        if (!FileLock.lockInstance(lockFile)) {
            System.out.println("One instance is already running, just quit.");
            System.exit(1);
        }
        // new ChannelConsumer(null).run2();;
        Service service = new Service();
        service.run();

    }

}
