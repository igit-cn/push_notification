package com.yidian.push.recommend_gen;

import com.yidian.push.config.Config;
import com.yidian.push.config.RecommendGeneratorConfig;
import com.yidian.push.data.Platform;
import com.yidian.push.utils.HttpConnectionUtils;
import lombok.extern.log4j.Log4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tianyuzhi on 15/9/12.
 */
@Log4j
public class Generator {
    private LinkedBlockingQueue<RequestItem> requestItemLinkedBlockingQueue = new LinkedBlockingQueue<>();
    private Map<String, DocInfo> docIdInfoMapping = new HashMap<>();
    private LinkedBlockingQueue<UserPushRecord> userPushRecordLinkedBlockingQueue = new LinkedBlockingQueue<>();
    private QPSGetter qpsGetter = null;
    private ExecutorService executorService = null;
    private RecommendGeneratorConfig config ;
    private volatile boolean readFinished = false;
    private volatile boolean processHappened = false;
    private AtomicInteger recordToProcessNum = new AtomicInteger(0);


    public Generator() throws IOException {
        config = Config.getInstance().getRecommendGeneratorConfig();
        executorService = Executors.newFixedThreadPool(config.getThreadPoolSize());

        qpsGetter = new QPSGetter(config.getQpsURL());

        final Timer consumerTimer = new Timer("consumerTimer");
        consumerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    consumer();
                } catch (Exception e) {
                    log.error("consumer Timer failed.");
                }
            }
        }, 0, 1000);

        Timer refreshTimer = new Timer("refreshTimer");
        refreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    qpsGetter.refresh();
                    log.info("current qps is :" + qpsGetter.getQps());
                } catch (Exception e) {
                    log.error("qps refresh failed: " + qpsGetter.getUrl());
                }
            }
        }, 0, config.getQpsRefreshFrequencyInSeconds() * 1000);
    }


    public void consumer() {
        if (!requestItemLinkedBlockingQueue.isEmpty()) {
            int availableQps = config.getMaxQPS() - qpsGetter.getQps();
            if (availableQps <= 0) {
                return;
            }
            List<RequestItem> list = new ArrayList<>(availableQps);
            int num = requestItemLinkedBlockingQueue.drainTo(list, availableQps);
            if (num > 0) {
                for (final RequestItem item : list) {
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                recordToProcessNum.decrementAndGet();
                                String url = "";
                                String jsonStr = HttpConnectionUtils.getGetContent(url, null);
                                // TODO: parse the reply
                                // and add it to the  userPushRecordLinkedBlockingQueue

                            } catch (Exception e) {

                            }
                        }
                    });
                }
            }

        }

    }

    private RequestItem parseLine(String line) {
        if (null == line) {
            return null;
        }
        String[] arr = line.split(",");
        if (4 == arr.length) {
            String userId = arr[0];
            Platform platform = Platform.valueOf(arr[1]);
            String appId = arr[2];
            String model = arr[3];
            int number = 20;
            return new RequestItem(userId, model, number, platform, appId);
        }
        return null;
    }

    // this method can process one file at one time.

    public void processFile(String file) throws InterruptedException {
        if (processHappened) {
            throw new RuntimeException("process happened, new one instance to process.");
        }
        processHappened = true;
        readFinished = false;
        Charset UTF_8 = StandardCharsets.UTF_8;
        Path filePath = new File(file).toPath();
        BufferedReader reader = null;
        try {
            reader = Files.newBufferedReader(filePath, UTF_8);
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                RequestItem item = parseLine(line);
                if (item.isValid()) {
                    recordToProcessNum.incrementAndGet();
                    requestItemLinkedBlockingQueue.offer(item);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        readFinished = true;

        if (recordToProcessNum.get() == 0) {
            // done
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
            // TODO : generate the files.

        }


    }
}
