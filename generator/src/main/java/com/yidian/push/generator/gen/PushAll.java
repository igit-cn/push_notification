package com.yidian.push.generator.gen;

import com.yidian.push.config.Config;
import com.yidian.push.config.GeneratorConfig;
import com.yidian.push.data.HostPortDB;
import com.yidian.push.data.PushType;
import com.yidian.push.generator.MySqlConnectionPool;
import com.yidian.push.generator.Table;
import com.yidian.push.generator.Task;
import com.yidian.push.generator.gen.config.PushAllConfig;
import com.yidian.push.generator.gen.config.Range;
import com.yidian.push.push_request.PushRecord;
import com.yidian.push.utils.GsonFactory;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tianyuzhi on 15/6/18.
 */
@Log4j
public class PushAll {
    private static ExecutorService executor = Executors.newFixedThreadPool(100);
    public static void processTask(Task task) throws IOException {

        GeneratorConfig generatorConfig = Config.getInstance().getGeneratorConfig();

        int rangeSize = 0;
        if ("PUSH".equals(task.getTable())) {
            rangeSize  = generatorConfig.getIPhoneRangeSize();
        } else {
            rangeSize = generatorConfig.getAndroidRangeSize();
        }
        int maxUserId= generatorConfig.getMaxUserId();
        int index = maxUserId;
        while (index > 0) {
            for (HostPortDB hostPortDB : generatorConfig.getMYSQL_HOSTS()) {
                final PushAllConfig pushAllConfig = new PushAllConfig();
                pushAllConfig.setUserRange(new Range(index - rangeSize, index));
                pushAllConfig.setHostPortDB(hostPortDB);
                index -= rangeSize;
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            processPushAll(pushAllConfig);
                        } catch (Exception e) {
                            log.error("push all[" + GsonFactory.getNonPrettyGson().toJson(pushAllConfig)
                                    + "] failed with exception : " + ExceptionUtils.getFullStackTrace(e));
                        }
                    }
                });
            }
        }
    }


    public static void processPushAll(PushAllConfig config) throws IOException, SQLException {
        Connection connection = MySqlConnectionPool.getConnection(config.getHostPortDB());
        Statement st = null;
        ResultSet rs = null;
        String sql = config.genSql();
        try {
            st = connection.createStatement();
            rs = st.executeQuery(sql);
            int firstDayUserId = config.getTodayFirstUserId();
            int lastUserId = -1;
            int localTime = DateTime.now().getMinuteOfDay();
            int startTime = config.getTask().getStartTime();
            int endTime = config.getTask().getEndTime();
            boolean isIPhone = Table.isIPhone(config.getTable());
            int batchSize = config.getBatchSize();

            String pushDocId = config.getTask().getPushDocId();
            String pushTitle = config.getTask().getPushTitle();
            String pushHead = config.getTask().getPushHead();
            String pushChannel = "";
            PushType pushType = config.getTask().getPushType();
            int redisLength = Config.getInstance().getGeneratorConfig().getREDIS_HOSTS().size();
            List<Map<String, PushRecord>> pushRecordDict = new ArrayList<>(redisLength);
            for (Map<String, PushRecord> map : pushRecordDict) {
                map = new HashMap<>();
            }
//        uid   	= int(r[0])
//        token 	= r[1]
//        push_level = int(r[2])
//        appid 	= r[3]
//        enable 	= r[4]
//        timezone= r[5]

            while (rs.next()) {
                int curUserId = rs.getInt(1);
                String token = rs.getString(2);
                int pushLevel = rs.getInt(3);
                String appId = rs.getString(4);
                int enable = rs.getInt(5);
                int timezone = rs.getInt(6);

                if (enable == 1 && firstDayUserId != -1 && curUserId > firstDayUserId) {
                    continue;
                }
                if (config.getBucketIds() != null && !config.getBucketIds().contains(curUserId % 10)) {
                    continue;
                }
                int userLocalTime = (localTime + timezone + 1440) % 1440;
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
                int redisId = curUserId % redisLength;
                Map<String, PushRecord> map = pushRecordDict.get(redisId);
                String userIdAppId = new StringBuilder().append(curUserId).append(",").append(appId).toString();
                if (lastUserId == curUserId) {
                    if (map.containsKey(userIdAppId)) {
                        map.get(userIdAppId).addToken(tokenLevel);
                    } else {
                        map.put(userIdAppId, pushRecord);
                    }
                } else {
                    if (map.size() >= batchSize) {
                        //TODO: generate the request file from the records;
                        Collection<PushRecord> collection = map.values();
                        try {
                            GenerateRequestFile.generateRequestFile(config.getHostPortDB().getHost(), config.getHostPortDB().getPort(),
                                    config.getTable(), redisId, config.getTask().getPushType().toString(),
                                    collection, config.getBatchSize(), config.getTask().getProtectMinutes());
                        } catch (IOException e) {
                            log.info("gen request file failed with exception : " + ExceptionUtils.getFullStackTrace(e));
                        }
                        map.clear();
                    }
                }
                lastUserId = curUserId;
            }
            for (Map<String, PushRecord> map : pushRecordDict) {
                if (map.size() > 0) {
                    // TODO : generate the request file from the records
                }
            }
        } finally {
            if (null != rs) {
                try {
                    rs.close();
                } catch (Exception e ){}
            }
            if (null != st) {
                try { st.close();} catch (Exception e) {}
            }
            if (null != connection) {
                try {connection.close();} catch (Exception e) {}
            }
        }


    }

}
