package com.yidian.push.generator.gen;

import com.yidian.push.config.Config;
import com.yidian.push.config.GeneratorConfig;
import com.yidian.push.data.HostPortDB;
import com.yidian.push.data.Platform;
import com.yidian.push.data.PushType;
import com.yidian.push.generator.cache.CacheUtil;
import com.yidian.push.generator.data.*;
import com.yidian.push.push_request.PushRecord;
import com.yidian.push.utils.GsonFactory;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.Channel;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by tianyuzhi on 15/6/18.
 */
@Log4j
public class PushAuto {



    public static void processTaskWithFile(Task task) throws IOException {
        GeneratorConfig generatorConfig = Config.getInstance().getGeneratorConfig();
        int poolSize = generatorConfig.getPoolSize(task.getTable());
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        PushIndex latestPushIndex = RefreshTokens.getInstance().getLatestIndex();
        String latestPushDataPath = latestPushIndex.getDataPath();

        try {
            latestPushIndex.markAsUsing();
            for (HostPortDB hostPortDB : generatorConfig.getMYSQL_HOSTS()) {
                long firstDayUserId = GetFirstUserId.getTodayFirstUserId(generatorConfig.getMinUserFilePath(),
                        generatorConfig.getMinUserFilePrefix(),
                        hostPortDB.getHost(),
                        hostPortDB.getPort(),
                        task.getTable());
                String path = RefreshTokens.getPathForHostTable(latestPushDataPath, hostPortDB, task.getTable());
                Map<Long, String> userIdChannelMap = CacheUtil.getUserIdChannelMapping(task.getTable(), task.getPushChannel());
                File[] files = new File(path).listFiles();
                if (null == files || files.length == 0) {
                    continue;
                }
                for (File file : files) {
                    final PushAutoConfig pushAutoConfig = new PushAutoConfig();
                    pushAutoConfig.setHostPortDB(hostPortDB);
                    pushAutoConfig.setTask(task);
                    pushAutoConfig.setUserIdChannelMapping(userIdChannelMap);
                    pushAutoConfig.setTodayFirstUserId(firstDayUserId);
                    pushAutoConfig.setFile(file.getAbsolutePath());

                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                processPushAllWithFile(pushAutoConfig);
                            } catch (Exception e) {
                                log.error("push all[" + GsonFactory.getNonPrettyGson().toJson(pushAutoConfig)
                                        + "] failed with exception : " + ExceptionUtils.getFullStackTrace(e));
                            }
                        }
                    });
                }
            }
        } finally {
            latestPushIndex.markAsNoneUsing();
        }
        try {
            executor.shutdown();
            executor.awaitTermination(generatorConfig.getSecondsToWaitThreadPoolShutDownTimeout(), TimeUnit.SECONDS);
            log.info("DONE one task : " + task.getTable());
        } catch (InterruptedException e) {
            log.info("shut down the thread pool failed with exception " + ExceptionUtils.getFullStackTrace(e));
        }

    }

    private static void processPushAllWithFile(PushAutoConfig config) throws IOException, SQLException {

        String table = config.getTask().getTable();
        BufferedReader bufferedReader = null;
        try {
            long firstDayUserId = config.getTodayFirstUserId();
            long lastUserId = -1;
            int localTime = DateTime.now().getMinuteOfDay();
            int startTime = config.getTask().getStartTime();
            int endTime = config.getTask().getEndTime();
            boolean isIPhone = Platform.isIPhone(table);
            int batchSize = config.getBatchSize();

            String pushDocId = config.getTask().getPushDocId();
            String pushTitle = config.getTask().getPushTitle();
            String pushHead = config.getTask().getPushHead();
            Set<String> validAppIdSet = new HashSet<>(config.getTask().getAppIdInclude());
            String pushChannel = "";
            PushType pushType = config.getTask().getPushType();
            int intPushType = pushType.getInt();
            int redisLength = Config.getInstance().getGeneratorConfig().getREDIS_HOSTS().size();
            List<Map<String, PushRecord>> pushRecordList = new ArrayList<>(redisLength);
            for (int i = 0; i < redisLength; i++) {
                pushRecordList.add(new HashMap<String, PushRecord>(config.getBatchSize()));
            }

            bufferedReader = new BufferedReader(new FileReader(config.getFile()));
            String line;
            //userid, token, push_level, appid, enable, time_zone, version
            while ((line = bufferedReader.readLine()) != null ) {
                String arr[] = line.split(RefreshTokens.FILED_SEPARATOR);
                if (arr.length < 7) {continue;}
                long curUserId = Long.parseLong(arr[0]);
                String token = arr[1];
                int pushLevel = Integer.parseInt(arr[2]);
                String appId = arr[3];
                int enable = Integer.parseInt(arr[4]);
                int timezone = Integer.parseInt(arr[5]);
                int version = Integer.parseInt(arr[6]);
                int bucketId = Bucket.getBucketId(curUserId);

                if (enable == 1 && firstDayUserId != -1 && curUserId > firstDayUserId) {
                    continue;
                }
                if (enable > 1 && (enable & intPushType) != intPushType) {
                    continue;
                }
                if (config.getBucketIds() != null && !config.getBucketIds().contains(bucketId)) {
                    continue;
                }
                if (null != validAppIdSet && !validAppIdSet.contains(appId)) {
                    continue;
                }

                int userLocalTime = (localTime + timezone + 1440) % 1440;
                if (userLocalTime < startTime || userLocalTime > endTime) {
                    continue;
                }
                String tokenLevel = new StringBuilder(token).append(PushRecord.TOKEN_ITEM_SEPARATOR).append(pushLevel).toString();

                PushRecord pushRecord;
                if (isIPhone) {
                    pushRecord = new PushRecord.Builder().setUid(curUserId).setAppId(appId)
                            .setDocId(pushDocId).setTitle(pushTitle)
                            .setNewsType(pushType.getInt()).addToken(tokenLevel)
                            .setNid(version).build();
                } else {
                    pushRecord = new PushRecord.Builder().setUid(curUserId).setAppId(appId)
                            .setDocId(pushDocId).setTitle(pushTitle)
                            .setHead(pushHead)
                            .setNewsType(pushType.getInt()).addToken(tokenLevel).build();
                }
                int redisId = (int)(curUserId % redisLength);
                Map<String, PushRecord> map = pushRecordList.get(redisId);
                String userIdAppId = new StringBuilder().append(curUserId).append(",").append(appId).toString();
                if (lastUserId == curUserId) {
                    if (map.containsKey(userIdAppId)) {
                        map.get(userIdAppId).addToken(tokenLevel);
                    } else {
                        map.put(userIdAppId, pushRecord);
                    }
                } else {
                    if (map.size() >= batchSize) {
                        Collection<PushRecord> collection = map.values();
                        try {
                            GenerateRequestFile.generateRequestFile(config.getHostPortDB().getHost(), config.getHostPortDB().getPort(),
                                    table, redisId, config.getTask().getPushType().getString(),
                                    collection, config.getBatchSize(), config.getTask().getProtectMinutes());
                        } catch (IOException e) {
                            log.info("gen request file failed with exception : " + ExceptionUtils.getFullStackTrace(e));
                        }
                        map.clear();
                    }
                    map.put(userIdAppId, pushRecord);
                }
                lastUserId = curUserId;
            }
            for (int i = 0; i < pushRecordList.size(); i++) {
                int redisId = i;
                Map<String, PushRecord> map = pushRecordList.get(i);
                if (map.size() > 0) {
                    Collection<PushRecord> collection = map.values();
                    try {
                        GenerateRequestFile.generateRequestFile(config.getHostPortDB().getHost(), config.getHostPortDB().getPort(),
                                table, redisId, config.getTask().getPushType().getString(),
                                collection, config.getBatchSize(), config.getTask().getProtectMinutes());
                    } catch (IOException e) {
                        log.info("gen request file failed with exception : " + ExceptionUtils.getFullStackTrace(e));
                    }
                    map.clear();
                }
            }
        } finally {
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {}
            }
        }
    }

}
