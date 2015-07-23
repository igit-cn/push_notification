package com.yidian.push.generator.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.utils.HttpClientUtils;
import com.yidian.push.utils.URLUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/7/13.
 */
@Log4j
public class OutServiceUtil {
    private static final String RELATED_CHANNEL_URL = "http://lc1.haproxy.yidian.com:8050/relatedchannel/pushchannel";
    private static final String DATA_PLATFORM_NOTIFICATION_URL = "http://dataplatform.yidian.com:8083/api_test/dashboard/add_push";

    public static List<String> getRelatedChannels(String docId) {
        List<String> channels = new ArrayList<>(5);
        String url = new StringBuilder(RELATED_CHANNEL_URL).append("?docid=").append(docId).append("&count=5").toString();
        try {
            String content = URLUtil.getContent(url);
            JSONObject json = JSON.parseObject(content);
            if (null != json && "success".equals(json.getString("status"))) {
                if (json.containsKey("result")) {
                    JSONArray result = json.getJSONArray("result");
                    if (null != result && result.size() > 0) {
                        for (Object object : result) {
                            JSONObject jsonObject = (JSONObject) object;
                            if (jsonObject.containsKey("fromId")) {
                                String fromId = jsonObject.getString("fromId");
                                if (StringUtils.isNotEmpty(fromId)) {
                                    channels.add(fromId);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("call url failed :" + url  + ", with error :" + ExceptionUtils.getFullStackTrace(e));
        }
        return channels;
    }

    public static boolean sendPushEventToDataTeam(long pushNum, String desc) {
        Map<String, String> data = new HashMap<>(4);
        long seconds = System.currentTimeMillis() / 1000;
        data.put("timestamp", seconds + "");
        if (pushNum > 0) {
            data.put("pushnum", pushNum + "");
        }
        if (StringUtils.isNotEmpty(desc)) {
            data.put("des", desc);
        }
        boolean isSuccessful = false;
        try {
            String content = HttpClientUtils.getPostContent(DATA_PLATFORM_NOTIFICATION_URL, data);
            JSONObject json = JSON.parseObject(content);
            if (null != json && json.containsKey("success") && json.getBoolean("success")) {
                isSuccessful = true;
            }
            else {
                log.error("sendPushEventToDataTeam failed with response " + content);
            }
        } catch (Exception e) {
            log.error("sendPushEventToDataTeam failed with exception : " + ExceptionUtils.getFullStackTrace(e));
        }
        return isSuccessful;
    }

}
