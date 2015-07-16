package com.yidian.push.generator.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.utils.URLUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyuzhi on 15/7/13.
 */
@Log4j
public class OutServiceUtil {
    private static final String RELATED_CHANNEL_URL = "http://lc1.haproxy.yidian.com:8050/relatedchannel/pushchannel";

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

//    def call_related_channel(docid):
//            try:
//    p1 = '?docid=%s&count=99' % docid
//            r1 = json.loads(urllib2.urlopen(RELATED_CHANNEL_URL + p1).read())
//    except:
//    r1 = None
//    return r1

}
