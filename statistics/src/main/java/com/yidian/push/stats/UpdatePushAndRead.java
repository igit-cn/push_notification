package com.yidian.push.stats;

import com.mongodb.*;
import com.yidian.push.data.Platform;
import com.yidian.push.utils.DateUtil;
import com.yidian.push.utils.GsonFactory;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;


/**
 * Created by yidianadmin on 15-2-27.
 */

@Log4j
public class UpdatePushAndRead {
    private static final String MONGO_HOST = "10.111.0.42";
    private static final int MONGO_PORT = 27017;

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

    public void updateDailyCounters(Platform platform, String day, int userId, Map<Integer, String> userId2AppIdMapping, String event,  Map<String, Map<String, Integer>> dailyStat) {
        if (!dailyStat.containsKey(day)) {
            dailyStat.put(day, new HashMap<String, Integer>());
        }
        Map<String, Integer> dayStat = dailyStat.get(day);
        List<String> keys = DailyStatUtils.genDailyStatKeys(platform, userId, userId2AppIdMapping, event);
        for (String key : keys) {
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
            Map<String, Map<String, Integer>> dailyStat = new HashMap<>();

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
            DBCursor cursor = dbCollection.find();
            System.out.println(cursor.size());
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
            System.out.println(GsonFactory.getPrettyGson().toJson(dailyStat));
        } finally {
            if (mongo!=null) {
                mongo.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String logBase = "/home/services/push_notification/log";
        String mappingBase = "/home/services/push_notification/cache/userid_appid_mapping";
        int lookBackDays = 7;
        String mappingFile = DailyStatUtils.getLatestAvailableMappingFile(mappingBase, lookBackDays);
        if (StringUtils.isEmpty(mappingFile)) {
            System.out.println("UserId 2 AppId Mapping file doesn't exist...");
            System.exit(-1);
        }
        log.info("mapping file is :" + mappingFile);
        Map<Integer, String> userId2AppIdMapping = UserIdAppIdMapping.loadMapping(mappingFile);
        log.info("mapping size is :" + userId2AppIdMapping.size());
        String[] days = {DateUtil.dateToYYYY_MM_DD(new Date()), DateUtil.dateToYYYY_MM_DD(DateUtil.incrDate(new Date(), -1))};

        UpdatePushAndRead updatePushAndRead = new UpdatePushAndRead();
        updatePushAndRead.updateCounters(logBase, Arrays.asList(days), userId2AppIdMapping);
    }

}
