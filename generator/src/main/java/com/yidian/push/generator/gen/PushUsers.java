package com.yidian.push.generator.gen;

import com.yidian.push.config.Config;
import com.yidian.push.config.GeneratorConfig;
import com.yidian.push.data.HostPortDB;
import com.yidian.push.data.Platform;
import com.yidian.push.data.PushType;
import com.yidian.push.generator.cache.CacheUtil;
import com.yidian.push.generator.data.*;
import com.yidian.push.generator.util.OutServiceUtil;
import com.yidian.push.push_request.PushRecord;
import com.yidian.push.utils.GsonFactory;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by tianyuzhi on 15/6/18.
 */
@Log4j
public class PushUsers {

    public static List<List<Long>> splitUsersIntoSeparateList(List<Long> users, int batchSize) {
        int listSize = users.size() / batchSize + 1;
        List<List<Long>> res = new ArrayList<>(listSize);
        List<Long> tmpUsers = null;
        int tmpUserSize = Math.min(batchSize, users.size());
        int index = 0;
        for (Long uid : users) {
            if (index == 0) {
                tmpUsers = new ArrayList<>(tmpUserSize);
            }
            tmpUsers.add(uid);
            index = (index + 1) % batchSize;
            if (index == 0) {
                res.add(tmpUsers);
                tmpUsers = null;
            }
        }
        if (tmpUsers != null) {
            res.add(tmpUsers);
        }
        return res;
    }

    public static void processTask(Task task, List<Long> users) throws IOException {
        GeneratorConfig generatorConfig = Config.getInstance().getGeneratorConfig();
        int poolSize = generatorConfig.getPoolSize(task.getTable());
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<Future<Long>> results = new ArrayList<>(10);
        List<List<Long>> uidsList = splitUsersIntoSeparateList(users, generatorConfig.getPushToUsersBatchSize());

        for (HostPortDB hostPortDB : generatorConfig.getMYSQL_HOSTS()) {
            long firstDayUserId = GetFirstUserId.getTodayFirstUserId(generatorConfig.getMinUserFilePath(),
                    generatorConfig.getMinUserFilePrefix(),
                    hostPortDB.getHost(),
                    hostPortDB.getPort(),
                    task.getTable());
            for (List<Long> uids : uidsList) {
                final PushUsersConfig pushUsersConfig = new PushUsersConfig();
                pushUsersConfig.setHostPortDB(hostPortDB);
                pushUsersConfig.setTask(task);
                pushUsersConfig.setUsers(uids);
                pushUsersConfig.setTodayFirstUserId(firstDayUserId);

                Future<Long> future = executor.submit(new Callable<Long>() {
                    @Override
                    public Long call() throws Exception {
                        return processPushUsers(pushUsersConfig);
                    }
                });
                results.add(future);
            }
        }
        try {
            executor.shutdown();
            executor.awaitTermination(generatorConfig.getSecondsToWaitThreadPoolShutDownTimeout(), TimeUnit.SECONDS);
            log.info("DONE one task : " + task.getTable());
        } catch (InterruptedException e) {
            log.info("shut down the thread pool failed with exception " + ExceptionUtils.getFullStackTrace(e));
        }
        int totalPushUsers = 0;
        for (Future<Long> future : results) {
            try {
                if (future.isDone()) {
                    totalPushUsers += future.get(1, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                log.error("get the push number failed.");
            }
        }
        task.setTotalPushUsers(totalPushUsers);
    }

    private static long processPushUsers(PushUsersConfig config) throws IOException, SQLException {
        long totalPushUsers = 0;
        Connection connection = MySqlConnectionPool.getConnection(config.getHostPortDB());
        Statement st = null;
        ResultSet rs = null;
        String sql = config.genSql();
        String table = config.getTask().getTable();
        System.out.println(sql);
        try {
            st = connection.createStatement();
            rs = st.executeQuery(sql);
            long firstDayUserId = config.getTodayFirstUserId();
            long lastUserId = -1;
            int localTime = new DateTime(DateTimeZone.UTC).getSecondOfDay();
            ;
            int startTime = config.getTask().getStartTime();
            int endTime = config.getTask().getEndTime();
            boolean isIPhone = Platform.isIPhone(table);
            int batchSize = config.getBatchSize();

            String pushDocId = config.getTask().getPushDocId();
            String pushTitle = config.getTask().getPushTitle();
            String pushHead = config.getTask().getPushHead();
            String pushChannel = "";
            PushType pushType = config.getTask().getPushType();
            int redisLength = Config.getInstance().getGeneratorConfig().getREDIS_HOSTS().size();
            List<Map<String, PushRecord>> pushRecordList = new ArrayList<>(redisLength);
            for (int i = 0; i < redisLength; i++) {
                pushRecordList.add(new HashMap<String, PushRecord>(config.getBatchSize()));
            }

            while (rs.next()) {

                long curUserId = rs.getInt(1);
                String token = rs.getString(2);
                int pushLevel = rs.getInt(3);
                String appId = rs.getString(4);
                int enable = rs.getInt(5);
                int timezone = rs.getInt(6);
                int bucketId = Bucket.getBucketId(curUserId);

                if (enable == 1 && firstDayUserId != -1 && curUserId > firstDayUserId) {
                    continue;
                }
                if (config.getBucketIds() != null && !config.getBucketIds().contains(bucketId)) {
                    continue;
                }
                // to minute : timezone is in seconds
                // 1h has 86400(24 * 60 * 60) seconds
                int userLocalTime = ((localTime + timezone + 86400) % 86400) / 60;
                if (userLocalTime < startTime || userLocalTime > endTime) {
                    continue;
                }
                String tokenLevel = new StringBuilder(token).append(PushRecord.TOKEN_ITEM_SEPARATOR).append(pushLevel).toString();

                PushRecord pushRecord;
                if (isIPhone) {
                    int version = rs.getInt(7);
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
                int redisId = (int) (curUserId % redisLength);
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
                            totalPushUsers += GenerateRequestFile.generateRequestFile(config.getHostPortDB().getHost(), config.getHostPortDB().getPort(),
                                    table, redisId, config.getTask().getPushType().getString(),
                                    collection, config.getBatchSize(), config.getTask().getProtectMinutes());
                        } catch (IOException e) {
                            log.error("gen request file failed with exception : " + ExceptionUtils.getFullStackTrace(e));
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
                        totalPushUsers += GenerateRequestFile.generateRequestFile(config.getHostPortDB().getHost(), config.getHostPortDB().getPort(),
                                table, redisId, config.getTask().getPushType().getString(),
                                collection, config.getBatchSize(), config.getTask().getProtectMinutes());
                    } catch (IOException e) {
                        log.info("gen request file failed with exception : " + ExceptionUtils.getFullStackTrace(e));
                    }
                    map.clear();
                }
            }
        } finally {
            if (null != rs) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (null != st) {
                try {
                    st.close();
                } catch (Exception e) {
                }
            }
            if (null != connection) {
                try {
                    connection.close();
                } catch (Exception e) {
                }
            }
        }
        return totalPushUsers;
    }

    public static void processTaskWithFile(Task task, List<Long> users) throws IOException {
        GeneratorConfig generatorConfig = Config.getInstance().getGeneratorConfig();
        int poolSize = generatorConfig.getPoolSize(task.getTable());
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<Future<Long>> resultList = new ArrayList<>(10000);
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
                log.info(task.getTable() + " today first userid : " + firstDayUserId);
                File[] files = new File(path).listFiles();
                if (null == files || files.length == 0) {
                    continue;
                }
                for (File file : files) {
                    final PushUsersConfig pushUsersConfig = new PushUsersConfig();
                    pushUsersConfig.setHostPortDB(hostPortDB);
                    pushUsersConfig.setTask(task);
                    pushUsersConfig.setUsers(users);
                    pushUsersConfig.setTodayFirstUserId(firstDayUserId);
                    pushUsersConfig.setFile(file.getAbsolutePath());

                    Future<Long> future = executor.submit(new Callable<Long>() {
                        @Override
                        public Long call() throws Exception {
                            return processPushUserWithFile(pushUsersConfig);
                        }
                    });
                    resultList.add(future);
                }
            }
            try {
                executor.shutdown();
                executor.awaitTermination(generatorConfig.getSecondsToWaitThreadPoolShutDownTimeout(), TimeUnit.SECONDS);
                log.info("DONE one task : " + task.getTable());
            } catch (InterruptedException e) {
                log.info("shut down the thread pool failed with exception " + ExceptionUtils.getFullStackTrace(e));
            }
            int totalPushUsers = 0;
            for (Future<Long> future : resultList) {
                try {
                    if (future.isDone()) {
                        totalPushUsers += future.get(1, TimeUnit.SECONDS);
                    }
                } catch (Exception e) {
                    log.error("get the push number failed.");
                }
            }
            task.setTotalPushUsers(totalPushUsers);
        } finally {
            latestPushIndex.markAsNoneUsing();
        }
    }

    private static long processPushUserWithFile(PushUsersConfig config) throws IOException, SQLException {
        long totalPushUsers = 0;
        String table = config.getTask().getTable();
        BufferedReader bufferedReader = null;
        try {
            long firstDayUserId = config.getTodayFirstUserId();
            long lastUserId = -1;
            int localTime = new DateTime(DateTimeZone.UTC).getSecondOfDay();
            int startTime = config.getTask().getStartTime();
            int endTime = config.getTask().getEndTime();
            boolean isIPhone = Platform.isIPhone(table);
            int batchSize = config.getBatchSize();

            String pushDocId = config.getTask().getPushDocId();
            String pushTitle = config.getTask().getPushTitle();
            String pushHead = config.getTask().getPushHead();
            Set<Long> usersToPush = new HashSet<>(config.getUsers());
            Set<String> validAppIdSet = new HashSet<>();
            if (config.getTask().getAppIdInclude() != null) {
                validAppIdSet.addAll(config.getTask().getAppIdInclude());
            }
            PushType pushType = config.getTask().getPushType();
            String newsChannel = "";
            int intPushType = pushType.getInt();
            int redisLength = Config.getInstance().getGeneratorConfig().getREDIS_HOSTS().size();
            List<Map<String, PushRecord>> pushRecordList = new ArrayList<>(redisLength);
            for (int i = 0; i < redisLength; i++) {
                pushRecordList.add(new HashMap<String, PushRecord>(config.getBatchSize()));
            }
           // log.info("local time : " + localTime + "; push users config : " + GsonFactory.getNonPrettyGson().toJson(config));

            bufferedReader = new BufferedReader(new FileReader(config.getFile()));
            String line;
            //userid, token, push_level, appid, enable, time_zone, version
            while ((line = bufferedReader.readLine()) != null) {
                String arr[] = line.split(RefreshTokens.FILED_SEPARATOR);
                if (arr.length < 7) {
                    continue;
                }
                long curUserId = Long.parseLong(arr[0]);
                String token = arr[1];
                int pushLevel = Integer.parseInt(arr[2]);
                String appId = arr[3];
                int enable = Integer.parseInt(arr[4]);
                int timezone = Integer.parseInt(arr[5]);
                int version = Integer.parseInt(arr[6]);
                int bucketId = Bucket.getBucketId(curUserId);

                if (null == usersToPush || !usersToPush.contains(curUserId)) {
                    continue;
                }
                if (enable == 1 && firstDayUserId != -1 && curUserId > firstDayUserId) {
                    log.debug("filter by firstDayUserId, line :" + line);
                    continue;
                }
                if (enable > 1 && (enable & intPushType) != intPushType) {
                    log.debug("filter by enable, line :" + line);
                    continue;
                }
                if (config.getBucketIds() != null && !config.getBucketIds().contains(bucketId)) {
                    log.debug("filter by bucketid, line :" + line);
                    continue;
                }
//                if (null == validAppIdSet || !validAppIdSet.contains(appId)) {
//                    log.info("filter by appid, line :" + line);
//                    continue;
//                }

                // to minute : timezone is in seconds
                // 1h has 86400(24 * 60 * 60) seconds
                int userLocalTime = ((localTime + timezone + 86400) % 86400) / 60;
                if (userLocalTime < startTime || endTime < userLocalTime) {
                    log.debug("filter by user local time, line :" + line);
                    continue;
                }
                String tokenLevel = new StringBuilder(token).append(PushRecord.TOKEN_ITEM_SEPARATOR).append(pushLevel).toString();

                PushRecord pushRecord;
                if (isIPhone) {
                    pushRecord = new PushRecord.Builder().setUid(curUserId).setAppId(appId)
                            .setDocId(pushDocId).setTitle(pushTitle)
                            .setNewsChannel(newsChannel)
                            .setNewsType(pushType.getInt()).addToken(tokenLevel)
                            .setNid(version).build();
                } else {
                    pushRecord = new PushRecord.Builder().setUid(curUserId).setAppId(appId)
                            .setDocId(pushDocId).setTitle(pushTitle)
                            .setHead(pushHead)
                            .setNewsChannel(newsChannel)
                            .setNewsType(pushType.getInt()).addToken(tokenLevel).build();
                }
                int redisId = (int) (curUserId % redisLength);
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
                            totalPushUsers += GenerateRequestFile.generateRequestFile(config.getHostPortDB().getHost(), config.getHostPortDB().getPort(),
                                    table, redisId, config.getTask().getPushType().getString(),
                                    collection, config.getBatchSize(), config.getTask().getProtectMinutes());
                        } catch (IOException e) {
                            log.error("gen request file failed with exception : " + ExceptionUtils.getFullStackTrace(e));
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
                        totalPushUsers += GenerateRequestFile.generateRequestFile(config.getHostPortDB().getHost(), config.getHostPortDB().getPort(),
                                table, redisId, config.getTask().getPushType().getString(),
                                collection, config.getBatchSize(), config.getTask().getProtectMinutes());
                    } catch (IOException e) {
                        log.error("gen request file failed with exception : " + ExceptionUtils.getFullStackTrace(e));
                    }
                    map.clear();
                }
            }
        } finally {
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
            }
        }
        return totalPushUsers;
    }


}
