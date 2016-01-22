package com.yidian.push.weather.processor;

import com.yidian.push.config.WeatherPushConfig;
import com.yidian.push.weather.data.Alarm;
import com.yidian.push.weather.data.Document;
import com.yidian.push.weather.data.Pair;
import com.yidian.push.weather.data.Weather;
import com.yidian.push.weather.util.MongoUtil;
import com.yidian.push.weather.util.SmartWeatherUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by tianyuzhi on 16/1/21.
 */
@Log4j
@Getter
public class SmartWeather {
    private boolean IsInitialized = false;
    private WeatherPushConfig config = null;
    private Weather weather = null;
    private volatile Map<String, String> areaIdToChannel = new HashMap<>();
    private BlockingQueue<Pair<String, Alarm>> incomingAlarmQueue = new LinkedBlockingQueue<>();
    private List<Pair<String, Alarm>> incomingAlarmList = new LinkedList<>();

    private static Map<String, Document> cachedAlarmIdDocMapping = new HashMap<>();
    private Map<String, Document> incomingAlarmIdDocMapping = new HashMap<>();
    private Timer refreshLocalCacheTimer = new Timer("LocalChannelRefreshTimer");



    public SmartWeather(WeatherPushConfig config) {
        this.config = config;
        weather = new Weather(config.getWeatherConfig());
        refreshLocalCacheTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    getAllLocalChannels();
                } catch (Exception e) {
                    log.error("consumer Timer failed.");
                }
            }
        }, 0, config.getLocalChannelRefreshIntervalInSeconds() * 1000);
        log.info("start the consumer");
    }

    public void destroy() {
        refreshLocalCacheTimer.cancel();
        Thread.currentThread().interrupt();
    }

    public void getAllLocalChannels() {

        Map<String, String> newAreaIdToChannel = new HashMap<>();
        Map<String, String> idToAreaMapping = weather.getIdToAreaMapping();

        boolean errorHappened = false;
        for (String areaId : idToAreaMapping.keySet()) {
            String area = idToAreaMapping.get(areaId);
            try {
                String fromId = SmartWeatherUtil.getLocalChannel(config.getGetLocalChannelUrl(), area);
                if (StringUtils.isEmpty(fromId)) {
                    newAreaIdToChannel.put(areaId, fromId);
                }
            } catch (Exception e) {
                log.error("get local channel failed with exception: " + ExceptionUtils.getFullStackTrace(e));
                errorHappened = true;
                break;
            }
        }
        if (!errorHappened) {
            areaIdToChannel = newAreaIdToChannel;
        }
    }


    public void process() {
        while (!Thread.currentThread().isInterrupted()) {
            task();
            try {
                Thread.sleep(config.getRefreshIntervalInSeconds() * 1000);
            } catch (InterruptedException e) {
                break;
            }
        }
        log.info("finish refresh");
    }

    public void reset() {
        incomingAlarmQueue.clear();
        incomingAlarmList.clear();
        incomingAlarmIdDocMapping.clear();

    }

    public void getAlarms() {
        Map<String, String> idToAreaMapping = weather.getIdToAreaMapping();
        ExecutorService executor = Executors.newFixedThreadPool(config.getRefreshFetchPoolSize());
        final CountDownLatch latch = new CountDownLatch(idToAreaMapping.keySet().size());
        for (final String areaId : idToAreaMapping.keySet()) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Alarm> alarmList = weather.getAreaIdAlarms(areaId);
                        for (Alarm alarm : alarmList) {
                            incomingAlarmQueue.add(Pair.of(areaId, alarm));
                        }
                    } catch (Exception e) {
                        log.error("could not get alarm for areaId:" + areaId
                                + " with exception: " + ExceptionUtils.getFullStackTrace(e));
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        try {
            latch.await(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("interrupt failed.");
        }
        incomingAlarmQueue.drainTo(incomingAlarmList);
    }

    public void cleanCache() {
        DateTime now = DateTime.now();
        DateTime timeToClean = now.minusDays(config.getCleanCacheDays());
        List<String> alarmIdList = new ArrayList<>();
        for(String alarmId : cachedAlarmIdDocMapping.keySet()) {
            Document document = cachedAlarmIdDocMapping.get(alarmId);
            String publishDate = document.getPublishDate();
            if (StringUtils.isEmpty(publishDate)) {
                document.setPublishDate(now.toString("yyyy-MM-dd HH:mm:ss"));
                continue;
            }
            DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            DateTime publishTime = DateTime.parse(publishDate,format);
            if (publishTime.isBefore(timeToClean)) {
                alarmIdList.add(alarmId);
            }
        }
        for (String alarmId : alarmIdList) {
            log.info("clean out alarmId:" + alarmId);
            cachedAlarmIdDocMapping.remove(alarmId);
        }
    }

    public boolean generateDocIfNeeded() {
        Set<String> alarmIdSet = new HashSet<>();
        for (Pair<String, Alarm> pair : incomingAlarmList) {
            String alarmId = pair.getSecond().getId();
            if (!cachedAlarmIdDocMapping.containsKey(alarmId)) {
                alarmIdSet.add(alarmId);
            }
        }
        List<Document> processedDocuments = new ArrayList<>(alarmIdSet.size());
        boolean status = MongoUtil.getProcessedDocuments(alarmIdSet, processedDocuments);
        if (!status) {
            // TODO:
        }
        for (Document document : processedDocuments) {
            cachedAlarmIdDocMapping.put(document.getAlamId(), document);
        }
        for (Pair<String, Alarm> pair : incomingAlarmList) {
            String areaId = pair.getFirst();
            Alarm alarm = pair.getSecond();
            String alarmId = alarm.getId();
            boolean docGenerated = false;
            if (cachedAlarmIdDocMapping.containsKey(alarmId)) {
                docGenerated = true;
            }

            String channel = areaIdToChannel.getOrDefault(areaId, "");
            if (!docGenerated) {
                Document document = new Document(alarm);
                String docId = SmartWeatherUtil.genDocAndGetDocId(
                        config.getGenDocUrl(),
                        document.getTitle(),
                        document.getContent(),
                        config.getGenDocMediaId(),
                        config.getGetDocIdUrl()
                );
                if (StringUtils.isNotEmpty(docId)) {
                    document.setDocId(docId);
                    cachedAlarmIdDocMapping.put(alarmId, document);
                }
            }
            cachedAlarmIdDocMapping.get(alarmId).addFromId(channel);
        }
        return true;
    }

    public void task() {
        reset();
        getAlarms();
        generateDocIfNeeded();
        cleanCache();

    }


}
