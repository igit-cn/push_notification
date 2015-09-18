package com.yidian.push.recommend_gen;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.utils.HttpConnectionUtils;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.client.config.RequestConfig;

import java.io.IOException;
import java.util.*;

/**
 * Created by tianyuzhi on 15/9/15.
 */
@Log4j
public class DocIdTitleGetter {
    private String url = "http://a1.go2yd.com/Website/contents/content?docid=0Af7Fkap&fields=title&version=999999";

    public static Map<String, String> getTitles(String url, int batchSize, List<String> docIds) {
        int timeout = 5;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
        return getTitles(url, batchSize, docIds, config);
    }

    public static Map<String, String> getTitles(String url, int batchSize, List<String> docIds, RequestConfig config) {
        if (null == docIds || docIds.size() == 0) {
            return new HashMap<>(0);
        }
        Map<String, String> res = new HashMap<>(docIds.size());
        int index = 0;
        int total = docIds.size();
        while (index < total) {
            int start = index;
            int end = (index + batchSize) >= total ? total : (index + batchSize);
            index = end;
            List<String> subList = docIds.subList(start, end);
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("fields", "title");
                params.put("version", 999999);
                params.put("docid", StringUtils.join(subList, ','));
                String jsonStr = HttpConnectionUtils.getGetContent(url, params);
                JSONObject jsonObject = JSONObject.parseObject(jsonStr);
                if (null != jsonObject
                        && "success".equals(jsonObject.getString("status"))
                        && jsonObject.containsKey("documents")) {
                    JSONArray documents = jsonObject.getJSONArray("documents");
                    for (Object object : documents) {
                        JSONObject document = (JSONObject)object;
                        String docId = document.getString("docid");
                        String title = document.getString("title");
                        if (StringUtils.isNotEmpty(docId)
                                && StringUtils.isNotEmpty(title)) {
                            res.put(docId, title);
                        }
                    }
                }
            } catch (IOException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
            }
        }
        return res;
    }

    public static Map<String, String> getTitles(String url, int batchSize, Set<String> docIdSet, RequestConfig config) {
        if (null == docIdSet || docIdSet.size() == 0) {
            return new HashMap<>(0);
        }
        List<String> docIdList = new ArrayList<>(docIdSet);
        return getTitles(url, batchSize, docIdList, config);
    }
    public static Map<String, String> getTitles(String url, int batchSize, Set<String> docIdSet) {
        if (null == docIdSet || docIdSet.size() == 0) {
            return new HashMap<>(0);
        }
        List<String> docIdList = new ArrayList<>(docIdSet);
        return getTitles(url, batchSize, docIdList);
    }
}
