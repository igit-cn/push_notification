package com.yidian.push.recommend_gen;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.config.Config;
import com.yidian.push.config.RecommendGeneratorConfig;
import com.yidian.push.config.RecommendGeneratorOnlineConfig;
import com.yidian.push.data.Platform;
import com.yidian.push.data.PushType;
import com.yidian.push.utils.GsonFactory;
import com.yidian.push.utils.HttpConnectionUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tianyuzhi on 15/9/12.
 */
@Log4j
public class OnlineGenerator {
    private static HashMap<Integer, OnlineGenerator> INSTANCES = new HashMap<>();
    public static int getRunningInstancesNumber() {
        return INSTANCES.size();
    }
    private LinkedBlockingQueue<RequestItem> requestItemLinkedBlockingQueue = new LinkedBlockingQueue<>();
    private ConcurrentMap<String, DocInfo> docIdInfoMapping = new ConcurrentHashMap<>();
    private ConcurrentMap<String, String> docIdDocIdMapping = new ConcurrentHashMap<>();
    private LinkedBlockingQueue<UserPushRecord> userPushRecordLinkedBlockingQueue = new LinkedBlockingQueue<>();
    private QPSGetter qpsGetter = null;
    private ExecutorService consumerExecutorService = null;
    private ExecutorService pushExecutorServices = null;
    private RecommendGeneratorConfig config;
    private volatile boolean processHappened = false;
    private AtomicInteger recordToProcessNum = new AtomicInteger(0);
    private Timer consumerTimer = null;
    private Timer refreshTimer = new Timer("OnlineRefreshTimer");
    private Timer pushTimer = new Timer("OnlinePusTimer");
    private AtomicInteger totalValidToProcessNumber = new AtomicInteger(0);
    private AtomicInteger totalValidProcessedNumber = new AtomicInteger(0);
    private AtomicInteger totalPushedNumber = new AtomicInteger(0);


    private void startConsumer() {
        if (null == consumerTimer) {
            consumerTimer = new Timer("OnlineConsumerTimer");
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
            log.info("start the consumer");
        }
    }

    private void stopConsumer() {
        if (null != consumerTimer) {
            consumerTimer.cancel();
            consumerTimer = null;
        }
        log.info("stop the online consumer");
    }

    public static synchronized void sleep(int timeInSeconds) throws InterruptedException {
        for (OnlineGenerator generator : INSTANCES.values()) {
            generator.stopConsumer();
            log.info("Online SLEEP# sleep seconds : " + timeInSeconds);
            Thread.sleep(timeInSeconds * 1000);
            log.info("Online SLEEP# done the sleep : " + timeInSeconds);
            generator.startConsumer();
        }
    }

    public OnlineGenerator() throws IOException {
        config = Config.getInstance().getRecommendGeneratorConfig();
        consumerExecutorService = Executors.newFixedThreadPool(config.getConsumerThreadPoolSize());
        pushExecutorServices = Executors.newFixedThreadPool(config.getPushThreadPoolSize());

        qpsGetter = new QPSGetter(config.getQpsURL());
        startConsumer();
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
        pushTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    //pushRecords();
                    push();
                }
                catch (Exception e) {
                    log.error("push failed with exception " + ExceptionUtils.getFullStackTrace(e));
                }

            }
        }, 0, 1000);
        INSTANCES.put(this.hashCode(), this);
        RunningInstance.incRunningNumber();
    }


    public PushType getPushTye(String factor) {
        // fromid/fromid-news 64, tier1-news/tier1 512, coke/coke-news 256
        if (StringUtils.isNotEmpty(factor)) {
            if ("fromid".equals(factor) || "fromid-news".equals(factor)) {
                return PushType.RECOMMEND;
            }
            else if ("coke".equals(factor) || "coke-news".equals(factor)) {
                return PushType.RECOMMEND_2;
            }
            else if ("tier1".equals(factor) || "tier1-news".equals(factor)) {
                return PushType.RECOMMEND_3;
            }
            else if ("circle".equals(factor) || "circle-news".equals(factor)) {
                return PushType.RECOMMEND_3;
            }
        }

        return PushType.RECOMMEND_1;
    }

    public void consumer() {
        log.info("consumer recordToProcessNum size is " + requestItemLinkedBlockingQueue.size());
        if (!requestItemLinkedBlockingQueue.isEmpty()) {
            int runningInstances = RunningInstance.getRunningNumber();
            int availableQps = (config.getMaxQPS() - qpsGetter.getQps()) / runningInstances;
            log.info("current available qps is :" + availableQps + ", current running instances is:" + runningInstances);
            if (availableQps <= 0) {
                return;
            }
            List<RequestItem> list = new ArrayList<>(availableQps);
            int num = requestItemLinkedBlockingQueue.drainTo(list, availableQps);
            if (num > 0) {
                log.info("get # of request to process: " + num);
                for (final RequestItem item : list) {
                    consumerExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            boolean hasResult = false;
                            try {
                                recordToProcessNum.decrementAndGet();
                                String url = config.getRecommendURL();
                                Map<String, Object> params = new HashMap<>(5);
                                params.put("userid", item.getUserId());
                                params.put("num", item.getNum());
                                String jsonStr = HttpConnectionUtils.getGetResult(url, params, config.getRequestConfig());
                                //System.out.println(GsonFactory.getDefaultGson().toJson(item) + "; response:" + jsonStr);
                                // TODO: parse the reply
                                // and add it to the  userPushRecordLinkedBlockingQueue
                                JSONObject jsonObject = JSON.parseObject(jsonStr);
                                if (null != jsonObject
                                        && "success".equals(jsonObject.getString("status"))
                                        && jsonObject.containsKey("results")) {
                                    JSONArray results = jsonObject.getJSONArray("results");
                                    List<UserPushRecord.DocId_PushType> list = new ArrayList<>(results.size());
                                    for (Object item : results) {
                                        JSONObject jsonItem = (JSONObject) item;
                                        String docId = jsonItem.getString("docid");
                                        String title = jsonItem.getString("title");
                                        String score = jsonItem.getString("score");
                                        String fromId = null;
                                        String factor = jsonItem.getString("factor");
                                        String point = jsonItem.getString("point");
                                        if ("fromid".equals(factor)) {
                                            fromId = point;
                                        }
                                        if (!docIdDocIdMapping.containsKey(docId)) {
                                            docIdDocIdMapping.putIfAbsent(docId, docId);
                                        } else {
                                            docId = docIdDocIdMapping.get(docId);
                                        }

                                        DocInfo docInfo = new DocInfo(docId, fromId, title);
                                        PushType pushType = getPushTye(factor);

                                        if (!docIdInfoMapping.containsKey(docId)) {
                                            docIdInfoMapping.putIfAbsent(docId, docInfo);
                                        }
                                        list.add(new UserPushRecord.DocId_PushType(docId, pushType));
                                    }
                                    Collections.sort(list, new Comparator<UserPushRecord.DocId_PushType>() {
                                        @Override
                                        public int compare(UserPushRecord.DocId_PushType o1, UserPushRecord.DocId_PushType o2) {
                                            return o1.pushType.getInt() - o2.pushType.getInt();
                                        }
                                    });
                                    UserPushRecord userPushRecord = new UserPushRecord(item.getUserId(), item.getPlatform(), item.getAppId(), list);
                                    userPushRecordLinkedBlockingQueue.add(userPushRecord);
                                    hasResult = true;
                                } else {
                                    log.info("NO RECOMMEND DOC for user: " + item.getUserId());
                                }
                            } catch (Exception e) {
                                log.error("failed..." + ExceptionUtils.getFullStackTrace(e));
                            } finally {
                                if (!hasResult) {
                                    totalPushedNumber.incrementAndGet();
                                }
                                totalValidProcessedNumber.incrementAndGet();
                            }
                        }
                    });
                }
            }

        }

    }


    private RequestItem parseLine2(String line) {
        if (null == line) {
            return null;
        }
        String[] arr = line.split(",", 4);
        if (4 == arr.length) {
            String userId = arr[0];
            Platform platform = Platform.IPHONE;
            if ("Android".equals(arr[2])) {
                platform = Platform.ANDROID;
            }

            String appId = arr[1];
            String model = arr[3];
            model = model.trim().replaceAll(" +", "+");
            int number = 20;
            return new RequestItem(userId, model, number, platform, appId);
        }
        return null;
    }

    private RequestItem parseLine(String line) {
        if (null == line) {
            return null;
        }
        String[] arr = line.split(",", 4);
        if (2 == arr.length) {
            String userId = arr[0];
            String appId = arr[1];
            int number = 20;
            return new RequestItem(userId, "", number, null, appId);
        }
        return null;
    }

    public boolean shouldProcess(RequestItem item) {
        if (null == item || !item.isValid()) {
            return false;
        }
        Set<Integer> buckets = config.getBuckets();
        int bucketId = 0;
        try {
            bucketId = (int)(Long.parseLong(item.getUserId()) % 10);
        } catch (Exception e) {
            log.error("invalid userId : " + item.getUserId());
            return false;
        }
        if (buckets == null || buckets.contains(bucketId)) {
            return true;
        }
        return false;
    }

    // this method can process one file at one time.

    public void processFile(String file) throws InterruptedException, IOException {
        if (processHappened) {
            throw new RuntimeException("process happened, new one instance to process.");
        }
        processHappened = true;
        Charset UTF_8 = StandardCharsets.UTF_8;
        Path filePath = new File(file).toPath();
        BufferedReader reader = null;
        log.info("start to process : " + file);
        try {
            reader = Files.newBufferedReader(filePath, UTF_8);
            String line = null;
            int num = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                RequestItem item = parseLine(line);
                boolean needProcess = shouldProcess(item);
                if (needProcess) {
                    recordToProcessNum.incrementAndGet();
                    num ++;
//                    if (num % 1000 == 0) {
//                        log.info("got 1000 valid records to process");
//                    }
                    requestItemLinkedBlockingQueue.offer(item);
                }
                else {
                    //log.info(line + " filtered ... ");
                }
            }
            totalValidToProcessNumber.set(num);

            log.info("finish reading the file: " + file + ", and got # of records to process : " + num);

        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("cur recordToProcessNum size is " + recordToProcessNum.get());
        log.info("totalValidProcessedNumber : " + totalValidProcessedNumber +
                ", totalValidToProcessNumber : " + totalValidToProcessNumber +
                ", totalPushedNumber :" + totalPushedNumber);
        while (true) {
            if (totalPushedNumber.get() != totalValidToProcessNumber.get()) {
                Thread.sleep(1000);
                log.info("totalValidProcessedNumber : " + totalValidProcessedNumber +
                        ", totalValidToProcessNumber : " + totalValidToProcessNumber +
                        ", totalPushedNumber :" + totalPushedNumber);
                continue;
            }
            // done
            log.info("get all the user records");
            break;
        }
        // clean up
        //clear();
    }

    public void clear() throws InterruptedException {
        log.info("clear the generator");
        log.info(" before shutdown the services : totalValidProcessedNumber : "
                + totalValidProcessedNumber +
                ", totalValidToProcessNumber : " + totalValidToProcessNumber +
                ", totalPushedNumber :" + totalPushedNumber);

        consumerExecutorService.shutdown();
        consumerExecutorService.awaitTermination(3000, TimeUnit.SECONDS);
        pushExecutorServices.shutdown();
        pushExecutorServices.awaitTermination(3000, TimeUnit.SECONDS);
        stopConsumer();
        refreshTimer.cancel();
        pushTimer.cancel();

        userPushRecordLinkedBlockingQueue.clear();
        requestItemLinkedBlockingQueue.clear();
        docIdDocIdMapping.clear();
        docIdInfoMapping.clear();

        if (INSTANCES.containsKey(this.hashCode())) {
            INSTANCES.remove(this.hashCode());
        }
        RunningInstance.decRunningNumber();
    }

    public void safeClose(BufferedWriter bw) {
        if (null != bw) {
            try {
                bw.close();
            }
            catch (IOException e) {
                // ignore
            }

        }
    }

    public boolean isAppx(String appId, Set<String> appIdExcludingSet) {
        if (appIdExcludingSet != null && appIdExcludingSet.contains(appId)) {
            return false;
        }
        return true;
    }

    public boolean isMain(String appId, Set<String> appIdIncludingSet) {
        if (appIdIncludingSet != null && appIdIncludingSet.contains(appId)) {
            return true;
        }
        return false;
    }

    public String toLine(UserPushRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getUserId()).append(Constants.CTR_A);
        if (null != record.getDocIdPushTypeList()) {
            boolean isFirst = true;
            for (UserPushRecord.DocId_PushType docId_pushType : record.getDocIdPushTypeList()) {
                String docId = docId_pushType.docId;
                if (!docIdInfoMapping.containsKey(docId)) {
                    log.info("FILTER BY NO DOCID");
                    continue;
                }
                else {
                    String title = docIdInfoMapping.get(docId).getTitle();
                    if (StringUtils.isEmpty(title) || title.length() < config.getTitleMinLength()) {
                        log.info("filter the short title docid : " + docId);
                        continue;
                    }
                }
                if (!isFirst) {
                    sb.append(Constants.CTR_C);
                }
                isFirst = false;
                sb.append(docId_pushType.docId).append(Constants.CTR_B).append(docId_pushType.pushType.getString());
            }
        }
        return sb.toString();
    }

    public void push() {
        if (userPushRecordLinkedBlockingQueue.isEmpty()) {
            return;
        }
        int total = userPushRecordLinkedBlockingQueue.size();
        int recordToPushInOneSecond = config.getRecordToPushInOneSecond();
        int threads = total / recordToPushInOneSecond + 1;
        for (int i = 0; i < threads; i ++) {
            pushExecutorServices.submit(new Runnable() {
                @Override
                public void run() {
                    pushRecords();
                }
            });
        }

    }

    private void pushRecords() {
        int recordToPushInOneSecond = config.getRecordToPushInOneSecond();
        int batch = config.getRecordPushBatchSize();
        List<UserPushRecord> list  = new ArrayList<>(recordToPushInOneSecond);
        int length = userPushRecordLinkedBlockingQueue.drainTo(list, recordToPushInOneSecond);
        if (length <= 0) {
            return;
        }

        log.info("got " + length + " of records to push");
        List<JSONObject> recordsToPushList = new ArrayList<>(length);
        for (UserPushRecord pushRecord : list) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userid", pushRecord.getUserId());
            jsonObject.put("appid", pushRecord.getAppId());
            jsonObject.put("platfrom", pushRecord.getPlatform());
            JSONArray docs = new JSONArray(pushRecord.getDocIdPushTypeList().size());
            for (UserPushRecord.DocId_PushType docId_pushType : pushRecord.getDocIdPushTypeList()) {
                JSONObject item = new JSONObject();
                item.put("id", docId_pushType.getDocId());
                item.put("pt", docId_pushType.getPushType());
                docs.add(item);
            }
            jsonObject.put("docs", docs);
            recordsToPushList.add(jsonObject);
        }

        int index = 0;
        while (index < length) {
            int start = index;
            int end = (index + batch) >= length ? length : index + batch;
            index += batch;
            List<JSONObject> subList = recordsToPushList.subList(start, end);
            Map<String, Object> params = new HashMap<>();
            //type=real_time&recommend_doc_type=yidian&sound=&key=acf6dbe50dfa2c572f7fe13b699495d7&test=
            params.put("type", "real_time");
            params.put("key", config.getPushKey());
            if (config.isTest()) {
                params.put("test", "true");
            }
            params.put("messages", GsonFactory.getNonPrettyGson().toJson(subList));

            int timesToRetry = config.getRetryTimes();
            while (timesToRetry > 0) {
                try {
                    String response = HttpConnectionUtils.getPostResult(config.getOnlineAddTaskUrl(), params, config.getRequestConfig());
                    JSONObject json = JSON.parseObject(response);
                    if (null != json && "0".equals(json.getString("code"))) {
                        log.info(response);
                        break;
                    } else {
                        if (null != json) {
                            log.error("failed with reason " + json.getString("reason"));
                        }
                        timesToRetry --;
                    }
                } catch (IOException e) {
                    log.error("off line push failed with Exception " + ExceptionUtils.getFullStackTrace(e));
                    timesToRetry --;
                }
            }
            log.info("pushed " + length + " of records");
            totalPushedNumber.addAndGet(end-start);
        }
    }


}
