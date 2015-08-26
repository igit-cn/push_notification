package com.yidian.push.logging.producer;

import com.yidian.push.config.Config;
import com.yidian.push.config.LoggingConfig;
import com.yidian.push.push_request.PushRequest;
import com.yidian.push.push_request.PushRequestManager;
import com.yidian.push.push_request.PushRequestStatus;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by tianyuzhi on 15/8/25.
 */
@Log4j
public class LogProducer {
    /**
     * Here you can user the object pool (kafka.javaapi.producer.Producer).
     * Also you can use task request pool, and start several thread to consume the request pool.
     */
    private static LinkedBlockingQueue<PushRequest> requestQueue = new LinkedBlockingQueue();
    private static List<LogThread> threadPool = null;
    private static volatile boolean IsInitialized = false;
    public synchronized static void init() throws IOException {
        if (IsInitialized) {return;}
        LoggingConfig config = Config.getInstance().getLoggingConfig();
        int threadNumber = config.getProducerNumber();
        threadPool = new ArrayList<>(threadNumber);
        for (int i = 0; i < threadNumber; i ++) {
            LogThread thread = new LogThread(i, requestQueue, config.getProducerProperties(), config.getProducerTopicName());
            threadPool.add(thread);
            thread.start();
            log.info("start producer :" + i);
        }
        IsInitialized = true;
    }

    public synchronized  static void destroy() throws InterruptedException {
        if (!IsInitialized) {
            return;
        }
        for (LogThread thread : threadPool) {
            thread.interrupt();
            log.info("interrupt thread num : " + thread.getThreadId());
        }
        for (LogThread thread : threadPool)
        {
            thread.join(5000);
            log.info("join thread num : " + thread.getThreadId());
        }
    }

    public static void process() {
        try {
            List<PushRequest> list =  PushRequestManager.getInstance().getRequests(PushRequestStatus.PROCESSED);
            for (PushRequest pushRequest : list) {
                requestQueue.put(pushRequest);
            }
        } catch (IOException e) {
            log.error("process failed with exception : " + ExceptionUtils.getFullStackTrace(e));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
