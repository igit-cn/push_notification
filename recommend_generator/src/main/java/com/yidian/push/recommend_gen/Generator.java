package com.yidian.push.recommend_gen;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.config.Config;
import com.yidian.push.config.RecommendGeneratorConfig;
import com.yidian.push.data.Platform;
import com.yidian.push.data.PushType;
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
public class Generator {
    private static ConcurrentHashMap<Integer, Generator> INSTANCES = new ConcurrentHashMap<>();
    public static int getRunningInstancesNumber() {
        return INSTANCES.size();
    }

    private LinkedBlockingQueue<RequestItem> requestItemLinkedBlockingQueue = new LinkedBlockingQueue<>();
    private ConcurrentMap<String, DocInfo> docIdInfoMapping = new ConcurrentHashMap<>();
    private ConcurrentMap<String, String> docIdDocIdMapping = new ConcurrentHashMap<>();
    private LinkedBlockingQueue<UserPushRecord> userPushRecordLinkedBlockingQueue = new LinkedBlockingQueue<>();
    private QPSGetter qpsGetter = null;
    private ExecutorService executorService = null;
    private RecommendGeneratorConfig config;
    private volatile boolean processHappened = false;
    private AtomicInteger recordToProcessNum = new AtomicInteger(0);
    private Timer consumerTimer = null;
    private Timer refreshTimer = new Timer("refreshTimer");
    private AtomicInteger totalValidToProcessNumber = new AtomicInteger(0);
    private AtomicInteger totalValidProcessedNumber = new AtomicInteger(0);


    private void startConsumer() {
        if (null == consumerTimer) {
            consumerTimer = new Timer("consumerTimer");
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
        log.info("stop the consumer");
    }

    public static synchronized void sleep(int timeInSeconds) throws InterruptedException {
        for (Generator generator : INSTANCES.values()) {
            generator.stopConsumer();
            log.info("SLEEP# sleep seconds : " + timeInSeconds);
            Thread.sleep(timeInSeconds * 1000);
            log.info("SLEEP# done the sleep : " + timeInSeconds);
            generator.startConsumer();
        }
    }

    public Generator() throws IOException {
        config = Config.getInstance().getRecommendGeneratorConfig();
        executorService = Executors.newFixedThreadPool(config.getConsumerThreadPoolSize());

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
        INSTANCES.putIfAbsent(this.hashCode(), this);
        RunningInstance.incRunningNumber();
    }
//
//    private UserPushRecord parseResponse(JSONArray docList) {
//        if (null == docList)
//
//    }

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
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                recordToProcessNum.decrementAndGet();
                                String url = config.getRecommendURL();
                                Map<String, Object> params = new HashMap<>(5);
                                params.put("userid", item.getUserId());
                                params.put("num", item.getNum());
                                params.put("model", item.getModel());
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
                                        JSONObject jsonItem = (JSONObject)item;
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
                                        }
                                        else {
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
                                }
                                else {
                                    log.info("NO RECOMMEND DOC for user: " + item.getUserId());
                                }
                            } catch (Exception e) {
                                log.error("failed..." + ExceptionUtils.getFullStackTrace(e));
                            } finally {
                                totalValidProcessedNumber.incrementAndGet();
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

    public void processFile(String file, String outputPath) throws InterruptedException, IOException {
        if (processHappened) {
            throw new RuntimeException("process happened, new one instance to process.");
        }
        File outputPathFile = new File(outputPath);
        if (!outputPathFile.isDirectory()) {
            FileUtils.forceMkdir(new File(outputPath));
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
        log.info("totalValidProcessedNumber : " + totalValidProcessedNumber + ", totalValidToProcessNumber : " + totalValidToProcessNumber);
        while (true) {
            if (totalValidProcessedNumber.get() != totalValidToProcessNumber.get()) {
                Thread.sleep(1000);
                log.info("totalValidProcessedNumber : " + totalValidProcessedNumber + ", totalValidToProcessNumber : " + totalValidToProcessNumber);
                continue;
            }
            // done
            log.info("before shutdown the recordToProcessNum size is " + recordToProcessNum.get());

            executorService.shutdown();
            executorService.awaitTermination(3000, TimeUnit.SECONDS);
            consumerTimer.cancel();
            refreshTimer.cancel();
            break;
        }
        log.info("get all the user records");
        String mappingFile = outputPath + "/recommend_push.map";
        String appxIOSDataFile = outputPath + "/recommend_push4x.data.IOS";
        String appxAndroidDataFile = outputPath + "/recommend_push4x.data.Android";
        String mainIOSDataFile = outputPath + "/recommend_push.data.IOS";
        String mainAndroidFile = outputPath + "/recommend_push.data.Android";
        String successFile = outputPath + "/_SUCCESS";
        // gen map file
        genMappingFile(mappingFile);
        log.info("mapping file ready:" + mappingFile);
        // gen push file
        genDataFile(appxIOSDataFile, appxAndroidDataFile, mainIOSDataFile, mainAndroidFile);
        log.info("data file ready.");
        FileUtils.touch(new File(successFile));
        log.info("touch success file");
        // clean up
        //clear();
    }

    public void clear() {
        log.info("clear the generator");
        userPushRecordLinkedBlockingQueue.clear();
        requestItemLinkedBlockingQueue.clear();
        docIdDocIdMapping.clear();
        docIdInfoMapping.clear();
        stopConsumer();
        refreshTimer.cancel();
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


    public void genDataFile(String appxIOSDataFile, String appxAndroidDataFile, String mainIOSDataFile, String mainAndroidFile) {

        BufferedWriter appxIOSWriter = null;
        BufferedWriter appxAndroidWriter = null;
        BufferedWriter mainIOSWriter = null;
        BufferedWriter mainAndroidWriter = null;

        try {
            appxIOSWriter = new BufferedWriter(new FileWriter(appxIOSDataFile));
            appxAndroidWriter = new BufferedWriter(new FileWriter(appxAndroidDataFile));
            mainIOSWriter = new BufferedWriter(new FileWriter(mainIOSDataFile));
            mainAndroidWriter = new BufferedWriter(new FileWriter(mainAndroidFile));

            for (UserPushRecord userPushRecord : userPushRecordLinkedBlockingQueue) {
                boolean isMainAppId = isMain(userPushRecord.getAppId(), config.getAPP_MAIN());
                boolean isAppXAppId = isAppx(userPushRecord.getAppId(), config.getNON_APP_X());
                if (isMainAppId) {
                    if (Platform.ANDROID == userPushRecord.getPlatform()) {
                        mainAndroidWriter.write(userPushRecord.toString());
                        mainAndroidWriter.newLine();
                    } else {
                        mainIOSWriter.write(userPushRecord.toString());
                        mainIOSWriter.newLine();
                    }
                }
                if (isAppXAppId) {
                    if (Platform.ANDROID == userPushRecord.getPlatform()) {
                        appxAndroidWriter.write(userPushRecord.toString());
                        appxAndroidWriter.newLine();
                    } else {
                        appxIOSWriter.write(userPushRecord.toString());
                        appxIOSWriter.newLine();
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            safeClose(appxIOSWriter);
            safeClose(appxAndroidWriter);
            safeClose(mainIOSWriter);
            safeClose(mainAndroidWriter);
        }

    }

    public void genMappingFile(String file) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            log.info("try to get titles for # of docids " +  docIdInfoMapping.keySet().size());
            Map<String, String> docIdTitleMapping = DocIdTitleGetter.getTitles(config.getDocIdInfoURL(), config.getDocIdInfoBatchSize(), docIdInfoMapping.keySet());
            log.info("finally get titles for # of docids " +  docIdInfoMapping.keySet().size());
            for (String docId : docIdInfoMapping.keySet()) {
                String title = docIdTitleMapping.get(docId);
                if (StringUtils.isNotEmpty(title)) {
                    docIdInfoMapping.get(docId).setTitle(title);
                }
                else {
                    // remove the docid if it is not a valid one
                    docIdInfoMapping.remove(docId);
                }
            }
            for (String docId : docIdInfoMapping.keySet()) {
                DocInfo docInfo = docIdInfoMapping.get(docId);
                String line = new StringBuilder().append(docId)
                        .append("\t").append(null == docInfo.getFromId() ? "" : docInfo.getFromId())
                        .append("\t").append(docInfo.getTitle())
                        .toString();
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
                if (null != bw) {
                    try {bw.close();} catch (IOException e) {
                        //ignore
                    }
                }
        }

    }
}
