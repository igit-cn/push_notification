package com.yidian.push;

import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.push_request.PushRequest;
import com.yidian.push.push_request.PushRequestManager;
import com.yidian.push.push_request.PushRequestStatus;
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
                    currentThread.join();
                    System.out.println("shutdown the thread pools");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        ExecutorService iPhonePool = Executors.newFixedThreadPool(processorConfig.getIPhonePoolSize());
        ExecutorService androidPool = Executors.newFixedThreadPool(processorConfig.getAndroidPoolSize());
        while (keepRunning) {
            List<PushRequest> pushRequests;
            try {
                pushRequests = PushRequestManager.getInstance().getRequests(PushRequestStatus.PREPARING);
                for (PushRequest pushRequest : pushRequests) {
                    String table = pushRequest.getTable();
                    if ("PUSH".equals(table)) {
                       // iPhonePool.submit();
                    }
                    else if ("PUSH_FOR_ANDROID".equals(table)) {

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

        }
        System.out.println("try to shutdown the thread pools");
        iPhonePool.shutdown();
        androidPool.shutdown();
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
