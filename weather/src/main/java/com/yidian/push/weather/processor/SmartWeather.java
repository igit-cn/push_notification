package com.yidian.push.weather.processor;

import com.yidian.push.config.Config;
import com.yidian.push.config.WeatherPushConfig;
import com.yidian.push.utils.GsonFactory;
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

import java.io.IOException;
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
    private BlockingQueue<Pair<String, Alarm>> incomingAlarmQueue = new LinkedBlockingQueue<>();
    private List<Pair<String, Alarm>> incomingAlarmList = new LinkedList<>();

    // TODO: save cache in local file system, load the file when started
    private Map<String, Document> cachedAlarmIdDocMapping = new HashMap<>();
    private Map<String, Integer> todayFromIdPushedTimes = new HashMap<>();
    private List<String> newlyIncomingAlarmIds = new ArrayList<>();
    private Timer refreshLocalCacheTimer = new Timer("LocalChannelRefreshTimer");
    private volatile Map<String, String> areaIdToChannel = new HashMap<>();

    private volatile static SmartWeather singleton;
    public static SmartWeather getInstance() {
        if (singleton == null) {
            synchronized (SmartWeather.class) {
                if (singleton == null) {
                    try {
                        singleton = new SmartWeather(Config.getInstance().getWeatherPushConfig());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return singleton;
    }

    private SmartWeather(WeatherPushConfig config) {
        this.config = config;
        weather = new Weather(config.getWeatherConfig());
        getAllLocalChannels();
        refreshLocalCacheTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    getAllLocalChannels();
                } catch (Exception e) {
                    log.error("refresh Timer failed.");
                }
            }
        }, 0, config.getLocalChannelRefreshIntervalInSeconds() * 1000);
        log.info("start the refresh timer");
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
                if (!StringUtils.isEmpty(fromId)) {
                    newAreaIdToChannel.put(areaId, fromId);
                }
            } catch (Exception e) {
                log.error("get local channel failed with exception: " + ExceptionUtils.getFullStackTrace(e));
                errorHappened = true;
                break;
            }
        }
        if (!errorHappened) {
            log.info("finish refresh");
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
    }

    public void task() {
        reset();
        getAlarms();
        generateDocIfNeeded();
        push();
        cleanCache();
    }

    public boolean push() {
        if (newlyIncomingAlarmIds.isEmpty()) {
            return true;
        }
        String day = DateTime.now().toString("yyyy-MM-dd");
        if (!MongoUtil.getFromIdPushCounter(day, todayFromIdPushedTimes) ) {
            return false;
        }
        for (String alarmId : newlyIncomingAlarmIds) {
            Document document = cachedAlarmIdDocMapping.get(alarmId);
            if (config.isDebug()) {
                String channels = config.getDebugChannels();
                log.info("try to push document: " + GsonFactory.getDefaultGson().toJson(document) + " to " + channels);
                SmartWeatherUtil.pushDocument(document, config.getPushUrl(),
                        config.getPushKey(), config.getPushUserIds(), channels);
                document.markFromIdAsPushed(channels);
                todayFromIdPushedTimes.put(channels, todayFromIdPushedTimes.getOrDefault(channels, 0) + 1);
                document.markAsPushed();
            }
            else if (document.isShouldPush() && !document.isPushed()) {
                log.info("try to push document: " + GsonFactory.getDefaultGson().toJson(document));
                pushDocument(document);
            }
            MongoUtil.saveOrUpdateDocuments(Arrays.asList(document));
        }
        log.info("save push counters");
        MongoUtil.saveOrUpdateDocuments(day, todayFromIdPushedTimes);
        return true;
    }


    public void reset() {
        incomingAlarmQueue.clear();
        incomingAlarmList.clear();
        newlyIncomingAlarmIds.clear();
        log.info("reset");
    }

    public boolean pushDocument(Document document) {

        List<String> channelList = new ArrayList<>();
        for (String channel : document.getFromIdPushed().keySet()) {
            boolean channelPushed = document.getFromIdPushed().getOrDefault(channel, true);
            if (!channelPushed) {
                int curPushCount = todayFromIdPushedTimes.getOrDefault(channel, 0);
                if (curPushCount < config.getDayPushThreshold()) {
                    channelList.add(channel);
                    todayFromIdPushedTimes.put(channel, curPushCount + 1);
                }
                else {
                    log.info("filter channel[" + channel + "] due to threshold.");
                }
            }
        }

        if (channelList.size() == 0) {
            return true;
        }

        String channels = StringUtils.join(channelList, ",");
        boolean pushed = SmartWeatherUtil.pushDocument(document,
                config.getPushUrl(), config.getPushKey(),
                config.getPushUserIds(), channels);
        if (pushed) {
            for (String channel : channelList) {
                document.markFromIdAsPushed(channel);
            }
            document.markAsPushed();
        }
        return pushed;
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
        log.info("totally got " + incomingAlarmList.size() + " alarms");
    }

    public void cleanCache() {
        DateTime now = DateTime.now();
        DateTime timeToClean = now.minusDays(config.getCleanCacheDays());
        List<String> alarmIdList = new ArrayList<>();
        for (String alarmId : cachedAlarmIdDocMapping.keySet()) {
            Document document = cachedAlarmIdDocMapping.get(alarmId);
            String publishDate = document.getPublishTime();
            if (StringUtils.isEmpty(publishDate)) {
                document.setPublishTime(now.toString("yyyy-MM-dd HH:mm"));
                continue;
            }
            try {
                DateTimeFormatter format1 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
                DateTimeFormatter format2 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                DateTime publishTime;
                if (publishDate.length() == 16) {
                    publishTime = DateTime.parse(publishDate, format1);
                }
                else {
                    publishTime = DateTime.parse(publishDate, format2);
                }
                if (publishTime.isBefore(timeToClean)) {
                    alarmIdList.add(alarmId);
                }
            } catch (Exception e) {
                log.error("bad time format for publishDate: " + publishDate);
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
            log.error("could not get processed docs from mongodb, and weather push will not happen");
            return false;
        }
        for (Document document : processedDocuments) {
            cachedAlarmIdDocMapping.put(document.getAlarmId(), document);
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
            String area = weather.getArea(areaId);
            if (!docGenerated) {
                Document document = new Document(alarm);
                boolean shouldPush = shouldPush(areaId, alarm);
                document.setShouldPush(shouldPush);
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
                    newlyIncomingAlarmIds.add(alarmId);
                }
            }
            if (StringUtils.isNotEmpty(channel)) {
                cachedAlarmIdDocMapping.get(alarmId).addFromId(channel);
            }
            else {
                log.error("NO_CHANNEL for area: " + area);
            }
            cachedAlarmIdDocMapping.get(alarmId).addArea(area);
        }
        log.info("totally got " + newlyIncomingAlarmIds.size() + " new alarm(s)");
        for (String alarmId : newlyIncomingAlarmIds) {
            Document document = cachedAlarmIdDocMapping.get(alarmId);
            log.info("insert doc into mongo, alarmId [" + alarmId + "], docId:[" + document.getDocId() );
            MongoUtil.saveOrUpdateDocuments(Arrays.asList(document));
        }
        return true;
    }


    public boolean shouldPush(String areaId, Alarm alarm) {
        String alarmLevel = alarm.getLevelId();
        if (weather.isInGuangdong(areaId)) {
            if (StringUtils.isNotEmpty(config.getAlarmGuangdongPushLevel())
                    && config.getAlarmGuangdongPushLevel().compareTo(alarmLevel) <= 0) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            if (StringUtils.isNotEmpty(config.getAlarmPushLevel())
                    && config.getAlarmPushLevel().compareTo(alarmLevel) <= 0) {
                return true;
            }
            else {
                return false;
            }
        }
    }

}
