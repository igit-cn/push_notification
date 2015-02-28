package com.yidian.push.stats;

import com.mongodb.*;
import com.yidian.push.data.Platform;
import com.yidian.push.utils.DateUtil;
import com.yidian.push.utils.GsonFactory;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;


/**
 * Created by yidianadmin on 15-2-27.
 */

@Log4j
public class UpdatePushAndRead2 {
    private static final String MONGO_HOST = "10.111.0.42";
    private static final int MONGO_PORT = 27017;
    private Map<String, List<Integer>> bucketPlatAppEventKeys;
    private Map<String, Integer> strToInt;
    private Map<Integer, String> intToStr;
    private int maxInt;

    public UpdatePushAndRead2() {
        bucketPlatAppEventKeys = new HashMap<>();
        strToInt = new HashMap<>();
        intToStr = new HashMap<>();
        maxInt = 0;
    }

    public static UserStat buildUserStat(DBObject dbObject)
    {
        if (dbObject == null) {
            return null;
        }
        UserStat userStat = new UserStat();
        if (dbObject.containsField("_id")) {
            userStat.setId(((Integer)dbObject.get("_id")));
        }
        if (dbObject.containsField("android")) {
            Map<String, List<String>> android = (Map<String, List<String>>)dbObject.get("android");
            if (android.containsKey("open")) {
                userStat.setAndroidOpen(android.get("open"));
            }
        }
        if (dbObject.containsField("iPhone")) {
            Map<String, List<String>> iPhone = (Map<String, List<String>>)dbObject.get("iPhone");
            if (iPhone.containsKey("open")) {
                userStat.setIPhoneOpen(iPhone.get("open"));
            }
        }
        if (dbObject.containsField("open")) {
            userStat.setOpen((List<String>)dbObject.get("open"));
        }
        if (dbObject.containsField("read")) {
            userStat.setRead((List<String>) dbObject.get("read"));
        }
        return userStat;
    }

    public void updateDailyCounters(Platform platform, String day, int userId, Map<Integer, String> userId2AppIdMapping, String event,  Map<String, Map<Integer, Integer>> dailyStat) {
        if (!dailyStat.containsKey(day)) {
            dailyStat.put(day, new HashMap<Integer, Integer>());
        }
        int bucket = DailyStatUtils.getBucketId(userId);
        String appId = userId2AppIdMapping.get(userId);
        if (appId == null) {
            appId = "";
        }
        String userBucketPlatEvent = String.format("%d_%s_%s_%s", bucket, platform.toString(), appId, event);
        List<Integer> keys = null;
        if (bucketPlatAppEventKeys.containsKey(userBucketPlatEvent)) {
            keys = bucketPlatAppEventKeys.get(userBucketPlatEvent);
        }
        else {
            List<String> strKeys = DailyStatUtils.genDailyStatKeys(platform, userId, userId2AppIdMapping, event);
            List<Integer> intKeys = new LinkedList<>();
            for (String str : strKeys) {
                if (strToInt.containsKey(str)) {
                    intKeys.add(strToInt.get(str));
                } else {
                    strToInt.put(str, maxInt);
                    intToStr.put(maxInt, str);
                    intKeys.add(maxInt);
                    maxInt ++;
                }
            }
            bucketPlatAppEventKeys.put(userBucketPlatEvent, intKeys);
            keys = intKeys;
        }

        Map<Integer, Integer> dayStat = dailyStat.get(day);
        for (Integer key : keys) {
            if (dayStat.containsKey(key)) {
                dayStat.put(key, dayStat.get(key) + 1);
            } else {
                dayStat.put(key, 1);
            }
        }
    }

    public void updateCounters(String logBase, List<String> days, Map<Integer, String> userId2AppIdMapping) throws IOException {
        Mongo mongo = null;
        Platform[] platforms = {Platform.ANDROID, Platform.IPHONE};
        try {
            //0. get day_plat => push users & update push stat
            Map<String, Set<Integer>> dayPlatPushUsers = new HashMap<>();
            Map<String, Map<Integer, Integer>> dailyStat = new HashMap<>();

            for (String day : days) {
                for (Platform platform : platforms) {
                    String indexFile = DailyStatUtils.getLogIndex(logBase, day, platform);
                    log.info("parsing index file : " + indexFile);
                    Set<Integer> pushUsers = DailyStatUtils.getPushUsers(indexFile);
                    String key = String.format("%s_%s", day, platform.toString());
                    if (null != pushUsers) {
                        log.info(key + " push users: " + pushUsers.size());
                        dayPlatPushUsers.put(key, pushUsers);
                        for (int userId : pushUsers) {
                            updateDailyCounters(platform, day, userId, userId2AppIdMapping, "push", dailyStat);
                        }
                    } else {
                        log.info(key + " push users: " + 0);
                    }
                }
            }

            // update push & open, push & read
            mongo = new Mongo(MONGO_HOST, MONGO_PORT);
            DB db = mongo.getDB("push_user_stat");
            DBCollection dbCollection = db.getCollection("user_stat");
            DB dailyDB = mongo.getDB("push_daily_stat");
            DBCollection dailyStatColl = dailyDB.getCollection("daily_stat");
            DBCursor cursor = dbCollection.find();
            log.info("user_stat size is : " + cursor.size());
            while (cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                UserStat userStat = buildUserStat(dbObject);
                for (String day : days) {
                    if (userStat.getAndroidOpen() != null && userStat.getAndroidOpen().contains(day)) {
                        updateDailyCounters(Platform.ANDROID, day, userStat.getId(), userId2AppIdMapping, "platform_open", dailyStat);
                    }
                    if (userStat.getIPhoneOpen() != null && userStat.getIPhoneOpen().contains(day)) {
                        updateDailyCounters(Platform.IPHONE, day, userStat.getId(), userId2AppIdMapping, "platform_open", dailyStat);
                    }
                    for (Platform platform : platforms) {
                        String key = String.format("%s_%s", day, platform.toString());
                        if (dayPlatPushUsers.containsKey(key) && dayPlatPushUsers.get(key).contains(userStat.getId())) {
                            if (userStat.getOpen() != null && userStat.getOpen().contains(day)) {
                                updateDailyCounters(platform, day, userStat.getId(), userId2AppIdMapping, "open", dailyStat);
                            }
                            if (userStat.getRead() != null && userStat.getRead().contains(day)) {
                                updateDailyCounters(platform, day, userStat.getId(), userId2AppIdMapping, "read", dailyStat);
                            }
                        }
                    }
                }
            }
            Map<String, Map<String, Integer>> realDailyStat = new HashMap<>();
            for (String day : dailyStat.keySet()) {
                Map<String, Integer> map = new HashMap<>();
                Map<Integer, Integer> dayStat = dailyStat.get(day);
                for (int i : dayStat.keySet()) {
                    map.put(intToStr.get(i), dayStat.get(i));
                }
                realDailyStat.put(day, map);
                BasicDBObject query = new BasicDBObject();
                query.append("_id", day);
                BasicDBObject set = new BasicDBObject();
                set.put("$set", map);
                dailyStatColl.update(query, set, true, false);
            }
            //System.out.println(GsonFactory.getPrettyGson().toJson(realDailyStat));
        } finally {
            log.info("done");
            if (mongo!=null) {
                mongo.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String logBase = "/home/services/push_notification/log";
        String mappingBase = "/home/services/push_notification/cache/userid_appid_mapping";
        int lookBackDays = 7;
        if (args.length >= 2) {
            logBase = args[0];
            mappingBase = args[1];
        }
        String mappingFile = DailyStatUtils.getLatestAvailableMappingFile(mappingBase, lookBackDays);
        if (StringUtils.isEmpty(mappingFile)) {
            System.out.println("UserId 2 AppId Mapping file doesn't exist...");
            System.exit(-1);
        }
        log.info("mapping file is :" + mappingFile);
        Map<Integer, String> userId2AppIdMapping = UserIdAppIdMapping.loadMapping(mappingFile);
        log.info("mapping size is :" + userId2AppIdMapping.size());
        String[] days = {DateUtil.dateToYYYY_MM_DD(new Date()), DateUtil.dateToYYYY_MM_DD(DateUtil.incrDate(new Date(), -1))};

        UpdatePushAndRead2 updatePushAndRead = new UpdatePushAndRead2();
        updatePushAndRead.updateCounters(logBase, Arrays.asList(days), userId2AppIdMapping);
    }
}
