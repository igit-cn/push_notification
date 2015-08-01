package com.yidian.push.services;

import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.Platform;
import com.yidian.push.processor.Processor;
import com.yidian.push.push_request.PushRequest;
import com.yidian.push.push_request.PushRequestManager;
import com.yidian.push.push_request.PushRequestStatus;
import com.yidian.push.utils.GetuiPush;
import com.yidian.push.utils.HttpConnectionUtils;
import com.yidian.push.utils.UmengPush;
import com.yidian.push.utils.XiaomiPush;
import lombok.extern.log4j.Log4j;

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

        while (keepRunning) {
            Processor.process();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.error("sleep failed...");
            }
        }
        System.out.println("try to shutdown the thread pools");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: Processor <config.json>");
            return;
        }
        String configFile = args[0];
        Config.setCONFIG_FILE(configFile);
        ProcessService processor = new ProcessService();
        processor.run();
    }
}
