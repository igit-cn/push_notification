package com.yidian.push.processor;

import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.Platform;
import com.yidian.push.push_request.PushRequest;
import com.yidian.push.push_request.PushRequestManager;
import com.yidian.push.push_request.PushRequestStatus;
import com.yidian.push.utils.GsonFactory;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by tianyuzhi on 15/7/31.
 */
@Log4j
public class Processor {
    private static ExecutorService iPhonePool;
    private static ExecutorService androidPool;
    private static volatile boolean isInitialized = false;

    public static void init() throws IOException {
        if (isInitialized) {
            return;
        }
        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        iPhonePool = Executors.newFixedThreadPool(config.getIPhonePoolSize());
        androidPool = Executors.newFixedThreadPool(config.getAndroidPoolSize());
        isInitialized = true;

    }

    public static void destroy() throws InterruptedException {
        if (isInitialized) {
            iPhonePool.shutdown();
            androidPool.shutdown();
            iPhonePool.awaitTermination(20, TimeUnit.SECONDS);
            androidPool.awaitTermination(20, TimeUnit.SECONDS);
        }

    }

    public static void process() {
        List<PushRequest> pushRequests;
        try {
            pushRequests = PushRequestManager.getInstance().getRequests(PushRequestStatus.READY);

            for (final PushRequest pushRequest : pushRequests) {
                try {
                    long startTime = System.currentTimeMillis();
                    log.info("start to process request:" + pushRequest.getFileName());
                    PushRequestManager.getInstance().markAsProcessing(pushRequest);
                    processOneRequest(pushRequest);
                    long endTime = System.currentTimeMillis();
                    log.info("end of processing request:" + pushRequest.getFileName() + ", cost time (seconds) : " + (endTime - startTime)/1000.0  );

                } catch (IOException e) {
                    log.error("failed to processing request : " + pushRequest.getFileName()
                            + " \n with exception " + ExceptionUtils.getFullStackTrace(e));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processOneRequest(final PushRequest pushRequest) throws IOException {
        String table = pushRequest.getTable();
        log.info("pushRequest : " + GsonFactory.getNonPrettyGson().toJson(pushRequest));
        if (Platform.isIPhone(table)) {
            iPhonePool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        IOSProcessor.processOne(pushRequest);
                    } catch (IOException e) {
                        log.error("got error when processing request: " + pushRequest.getFileName()
                                + "\n with exception: " + ExceptionUtils.getFullStackTrace(e));
                    }
                }
            });
        } else if (Platform.isAndroid(table)) {
            androidPool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        AndroidProcessor.processOne(pushRequest);
                    } catch (IOException e) {
                        log.error("got error when processing request: " + pushRequest.getFileName()
                                + "\n with exception: " + ExceptionUtils.getFullStackTrace(e));                            }
                }
            });

        } else {
            log.error("ignore bad request :" + pushRequest.getFileName());
        }
    }

}
