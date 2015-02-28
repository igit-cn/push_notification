package com.yidian.push.stats;

import com.yidian.push.data.Platform;
import com.yidian.push.utils.DateUtil;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;

/**
 * Created by yidianadmin on 15-2-27.
 */
public class DailyStatUtils {
    private static final String[] APPID_YIDIAN = {"hipu","yidian","yddk","xiaomi","zxpad","haowai","weixinwen","hot","ydtxz","ydtp"};
    private static final Set<String> APPID_YIDIAN_SET = new HashSet<>(Arrays.asList(APPID_YIDIAN));
    private static final String DAILY_STAT_APPID_TOP_KEY = "by_appid";
    private static final String DAILY_STAT_APPID_APPX = "appx_exclude_yidian";

    public static int getBucketId(int userId) {return userId%10;}
    public static List<String> genDailyStatKeys(Platform platform, int userId, Map<Integer, String> userIdToAppId, String key) {
        List<String> list = new LinkedList<>();
        int bucketId = getBucketId(userId);
        String appId = userIdToAppId.get(userId);
        list.add(String.format("%s.%s", platform, key).intern());
        list.add(String.format("%s.%s_%d", platform, key, bucketId).intern());
        if (StringUtils.isNotEmpty(appId)) {
            list.add(String.format("%s.%s.%s.%s", DAILY_STAT_APPID_TOP_KEY, platform, appId, key).intern());
            list.add(String.format("%s.%s.%s.%s_%d", DAILY_STAT_APPID_TOP_KEY, platform, appId, key, bucketId).intern());
            if (!APPID_YIDIAN_SET.contains(appId)) {
                list.add(String.format("%s.%s.%s.%s", DAILY_STAT_APPID_TOP_KEY, platform, DAILY_STAT_APPID_APPX, key).intern());
                list.add(String.format("%s.%s.%s.%s_%d", DAILY_STAT_APPID_TOP_KEY, platform, DAILY_STAT_APPID_APPX, key, bucketId).intern());
            }
        }
        return list;
    }

    public static String getLogFile(String logBase, String day, Platform platform) {
        return String.format("%s/%s/%s.data", logBase, platform.toString(), day);
    }

    public static String getLogIndex(String logBase, String day, Platform platform) {
        return String.format("%s/%s/%s.index", logBase, platform.toString(), day);
    }

    public static Set<Integer> getPushUsers(String indexFile) throws IOException {
        if (!new File(indexFile).isFile()) {
            return null;
        }
        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)));
        inputStream.readInt();
        int userId = 0;
        Set<Integer> users = new HashSet<>();
        while (inputStream.available() > 0) {
            userId ++;
            int offset = inputStream.readInt();
            if (offset == 0) {
                continue;
            }
            users.add(userId);
        }
        if (users.size() > 0) {
            return users;
        } else {
            return null;
        }
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
}
