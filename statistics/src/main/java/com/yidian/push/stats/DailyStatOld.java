package com.yidian.push.stats;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.yidian.push.data.Platform;
import com.yidian.push.data.PushLog;
import com.yidian.push.utils.DateUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


/**
 * Created by yidianadmin on 15-1-14.
 */
@Log4j
public class DailyStatOld {
    private String logBase = "/home/services/push_notification/log/";
    private static final int PUSH_TYPE_PERSONALIZATION = 64;
    private static final String[] APPID_YIDIAN = {"hipu","yidian","yddk","xiaomi","zxpad","haowai","weixinwen","hot","ydtxz","ydtp"};
    private static Set<String> APPID_YIDIAN_SET = new HashSet<>(Arrays.asList(APPID_YIDIAN));
    private static final String DAILY_STAT_APPID_TOP_KEY = "by_appid";
    private static final String DAILY_STAT_APPID_APPX = "appx_exclude_yidian";
    private static final String MONGO_HOST = "10.111.0.42";
    private static final int MONGO_PORT = 27017;


    public static int getBucketId(int userId) {return userId%10;}
    public static List<String> genDailyStatKeys(Platform platform, int userId, Map<Integer, String> userIdToAppId, String key) {
        List<String> list = new LinkedList<>();
        int bucketId = getBucketId(userId);
        String appId = userIdToAppId.get(userId);
        list.add(String.format("%s.%s", platform, key));
        list.add(String.format("%s.%s_%d", platform, key, bucketId));
        if (StringUtils.isNotEmpty(appId)) {
            list.add(String.format("%s.%s.%s.%s", DAILY_STAT_APPID_TOP_KEY, platform, appId, key));
            list.add(String.format("%s.%s.%s.%s_%d", DAILY_STAT_APPID_TOP_KEY, platform, appId, key, bucketId));
            if (!APPID_YIDIAN_SET.contains(appId)) {
                list.add(String.format("%s.%s.%s.%s", DAILY_STAT_APPID_TOP_KEY, platform, DAILY_STAT_APPID_APPX, key));
                list.add(String.format("%s.%s.%s.%s_%d", DAILY_STAT_APPID_TOP_KEY, platform, DAILY_STAT_APPID_APPX, key, bucketId));
            }
        }
        return list;
    }

    public static List<String> genPushKeys(Platform platform, String day, int userId, String appId) {
        List<String> list = new LinkedList<>();
        int userBucketId = getBucketId(userId);
        Platform[] ps = {platform, Platform.ALL};
        for (Platform p : ps) {
            list.add(String.format("%s.daily_push.%s.push", p, day));
            list.add(String.format("%s.daily_push.%s.push_%d", p, day, userBucketId));
            if (StringUtils.isNotEmpty(appId)) {
                list.add(String.format("%s.daily_push.%s.%s.push", p, day, appId));
                list.add(String.format("%s.daily_push.%s.%s.push_%d", p, day, appId, userBucketId));
            }
        }
        return list;
    }

    public static boolean update(String key, Object value, BasicDBObject basicDBObject) {
        String[] arr = key.split("\\.");
        BasicDBObject cur = basicDBObject;
        for (int i = 0; i < arr.length-1;i ++) {
            String subKey = arr[i];
            if (cur.containsField(subKey)) {
                cur = (BasicDBObject)cur.get(subKey);
            }
            else {
                BasicDBObject tmp = new BasicDBObject();
                cur.put(subKey, tmp);
                cur = tmp;
            }
        }
        cur.put(arr[arr.length-1], value);
        return true;
    }


    public static String getLogFile(String logBase, String day, Platform platform) {
        return String.format("%s/%s/%s.data", logBase, platform.toString(), day);
    }

    public static String getMappingFile(String mappingBase, String day) {
        return String.format("%s/uid2appid.%s", mappingBase, day);
    }

    public static String getLatestAvailableMappingFile(String mappingBase, int lookBackDays) {
        Date today = new Date();
        for (int i = 0; i < lookBackDays; i ++) {
            String day = DateUtil.dateToYYYY_MM_DD(DateUtil.incrDate(today, -1 * i));
            String file = getMappingFile(mappingBase, day);
            if (new File(file).isFile()) {
                return file;
            }
        }
        return null;
    }

    public static void updateNewsCounters(String logBase, String day, Map<Integer, String> userIdToAppId) throws IOException {
        int dataSegmentSize = PushLog.getPushLogSegmentSize(day);
        byte[] bytes = new byte[dataSegmentSize];
        Map<String, Map<String, Integer>> newsPushRecordCount = new HashMap<>();
        Platform[] platforms = {Platform.ANDROID, Platform.IPHONE};
        for (Platform platform : Platform.values()) {
            String logFile = getLogFile(logBase, day, platform);
            log.info("updateNewsCounters: file " + logFile);
            DataInputStream in = new DataInputStream(new FileInputStream(logFile));
            while (in.available()>0) {
                in.read(bytes);
                List<PushLog> list = PushLog.decode(bytes);
                for (PushLog pushLog : list) {
                    if (StringUtils.isEmpty(pushLog.getDocId())) {
                        continue;
                    }
                    String docId = pushLog.getDocId();
                    int userId = pushLog.getUserId();
                    String appId = userIdToAppId.get(userId);
                    if (!newsPushRecordCount.containsKey(docId)) {
                        newsPushRecordCount.put(docId, new HashMap<String, Integer>());
                    }
                    List<String> docPushKeys = genPushKeys(platform, day, userId, appId);
                    Map<String, Integer> pushRecordCount = newsPushRecordCount.get(docId);
                    for (String key : docPushKeys) {
                        if (!pushRecordCount.containsKey(key)) {
                            pushRecordCount.put(key, pushRecordCount.get(key)+1);
                        }
                    }
                }
            }
        }
       // for (String docId : )
        log.info("finish reading the data");
        Mongo mongo = new Mongo(MONGO_HOST, MONGO_PORT);
        DB db = mongo.getDB("push");
        DBCollection dbCollection = db.getCollection("daily_stat_new");
        BasicDBObject query = new BasicDBObject();
        query.put("_id", day);
       // dbCollection.update(query, update, true, false);
        mongo.close();

    }

    public static void updateDailyCounters(String logBase, String day, Map<Integer,String> userIdToAppId) throws IOException {
        Map<Integer, Set<String>> pushTypeDocs = new HashMap<>();
        Map<String, Integer> pushDocCount = new HashMap<>();
        Map<String, Integer> pushRecordCount = new HashMap<>();
        int dataSegmentSize = PushLog.getPushLogSegmentSize(day);
        byte[] bytes = new byte[dataSegmentSize];
        Platform[] platforms = {Platform.ANDROID, Platform.IPHONE};
        for (Platform platform : platforms) {
            String logFile = getLogFile(logBase, day, platform);
            log.info("updateDailyCounters: file " + logFile);
            DataInputStream in = new DataInputStream(new FileInputStream(logFile));
            while (in.available()>0) {
                in.read(bytes);
                List<PushLog> list = PushLog.decode(bytes);
                for (PushLog pushLog : list) {
                    if (StringUtils.isEmpty(pushLog.getDocId())) {
                        continue;
                    }
                    int pushType = pushLog.getPushType();
                    String docId = pushLog.getDocId();
                    int userId = pushLog.getUserId();
                    if (pushLog.getPushType() != PUSH_TYPE_PERSONALIZATION) {
                        if (!pushTypeDocs.containsKey(pushType)) {
                            pushTypeDocs.put(pushType, new HashSet<String>());
                        }
                        pushTypeDocs.get(pushType).add(docId);
                        if (!pushDocCount.containsKey(docId)) {
                            pushDocCount.put(docId, 1);
                        } else {
                            pushDocCount.put(docId, pushDocCount.get(docId)+1);
                        }
                    }
                    // update the push number
                    List<String> keys = genDailyStatKeys(platform, userId, userIdToAppId, "push_record_count");
                    for (String key : keys) {
                        if (!pushRecordCount.containsKey(key)) {
                            pushRecordCount.put(key, 1);
                        } else {
                            pushRecordCount.put(key, pushRecordCount.get(key)+1);
                        }
                    }
                } // end of for
            } // end of while
        }
        BasicDBObject update = new BasicDBObject();
        update.put("expire_at", DateUtil.incrDate(new Date(), 60));
        for (Integer type : pushTypeDocs.keySet()) {
            String strType = type + "";
            BasicDBObject typeObject = new BasicDBObject();
            for (String docId : pushTypeDocs.get(type)) {
                typeObject.put(docId, pushDocCount.get(docId));
            }
            update.put(strType, typeObject);
        }
        for (String key : pushRecordCount.keySet()) {
            update(key, pushRecordCount.get(key), update);
        }
        System.out.println(update);
        Mongo mongo = new Mongo(MONGO_HOST, MONGO_PORT);
        DB db = mongo.getDB("push");
        DBCollection dbCollection = db.getCollection("daily_stat_new");
        BasicDBObject query = new BasicDBObject();
        query.put("_id", day);
        dbCollection.update(query, update, true, false);
        mongo.close();
        System.out.println(update);
        log.info("done for updateDailyCounters");
    }

    public static void main(String[] args) throws IOException {
//        BasicDBObject test = new BasicDBObject();
//        test.put("123", "2345");
//        test.put("23454", 234);
//        System.out.println(test);
//        return;
        String logBase = "/home/services/push_notification/log";
        String mappingBase = "/home/services/push_notification/cache/userid_appid_mapping";
        int lookBackDays = 7;
        String mappingFile = getLatestAvailableMappingFile(mappingBase, lookBackDays);
        if (StringUtils.isEmpty(mappingFile)) {
            System.out.println("UserId 2 AppId Mapping file doesn't exist...");
        }
        log.info("mapping file is :" + mappingFile);
        Map<Integer, String> userId2AppIdMapping = UserIdAppIdMapping.loadMapping(mappingFile);
        updateDailyCounters(logBase, "2015-01-13", userId2AppIdMapping);
    }


}
