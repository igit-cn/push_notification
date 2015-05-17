package com.yidian.push.generator;

import com.yidian.push.config.Config;
import com.yidian.push.config.GeneratorConfig;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.generator.request.Request;
import com.yidian.push.generator.request.RequestManager;
import com.yidian.push.generator.request.RequestStatus;
import com.yidian.push.utils.FileLock;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.List;

/**
 * Created by yidianadmin on 15-3-5.
 */
@Log4j
public class Generator {
    private static volatile boolean keepRunning = false;

    public void process() {
        GeneratorConfig generatorConfig = null;
        try {
            generatorConfig = Config.getInstance().getGeneratorConfig();
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
                log.info("receive kill signal ...");
                try {
                    currentThread.join();
                    log.info("shutdown the thread pools");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        while(keepRunning) {
            try {
                List<Request> requests = RequestManager.getInstance().getRequests(RequestStatus.READY);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("could not get the request");
            }

        }
    }

    public static void main(String[] args) {
        String lockFile = "push_request_generator.lock";
        if (!FileLock.lockInstance(lockFile)) {
            System.out.println("One instance is already running, just quit.");
            System.exit(1);
        }
    }
}
