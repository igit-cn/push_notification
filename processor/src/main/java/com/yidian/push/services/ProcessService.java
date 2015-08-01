package com.yidian.push.services;

import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.Platform;
import com.yidian.push.processor.Processor;
import com.yidian.push.push_request.PushRequest;
import com.yidian.push.push_request.PushRequestManager;
import com.yidian.push.push_request.PushRequestStatus;
import com.yidian.push.utils.*;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yidianadmin on 15-2-2.
 */
@Log4j
public class ProcessService implements Runnable {
    private static volatile boolean keepRunning = false;
    @Override
    public void run() {
        ProcessorConfig processorConfig = null;
        try {
            processorConfig = Config.getInstance().getProcessorConfig();
            HttpConnectionUtils.init();
            GetuiPush.init();
            XiaomiPush.init();
            Processor.init();
            keepRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("get processor config failed...");
            throw new RuntimeException(e);
        }

        final Thread currentThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                keepRunning = false;
                System.out.println("receive kill signal ...");
                try {
                    Processor.destroy();
                    HttpConnectionUtils.release();
                    currentThread.join();
                    System.out.println("shutdown the thread pools");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        int sleepTime = processorConfig.getRequestScanIntervalInSeconds() * 1000;

        log.info("start to process the requests");
        while (keepRunning) {


            Processor.process();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.error("sleep failed...");
            }
        }
        log.info("done...");
    }

    public static void main(String[] args) throws IOException {
        System.out.println(args.length);
        if (args.length >= 1) {
            String configFile = args[0];
            System.out.println("User specified config file " + configFile);
            Config.setCONFIG_FILE(configFile);
        } else {
            // Config.setCONFIG_FILE("generator/src/main/resources/config/prod_config.json");
            Config.setCONFIG_FILE("processor/src/main/resources/config/config.json");
            System.setProperty("log4j.configuration", "src/main/resources/config/log4j_debug.properties");
            PropertyConfigurator.configure("generator/src/main/resources/config/log4j_debug.properties");
            Logger.getRootLogger().setLevel(Level.DEBUG);
        }
        String lockFile = Config.getInstance().getProcessorConfig().getLockFile();
        if (!FileLock.lockInstance(lockFile)) {
            System.out.println("One instance is already running, just quit.");
            System.exit(1);
        }
        ProcessService service = new ProcessService();
        service.run();
    }
}
