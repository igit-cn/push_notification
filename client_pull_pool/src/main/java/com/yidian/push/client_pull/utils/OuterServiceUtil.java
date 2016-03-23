package com.yidian.push.client_pull.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.client_pull.data.DocItem;
import com.yidian.push.utils.HttpConnectionUtils;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyuzhi on 16/3/21.
 */
@Log4j
public class OuterServiceUtil {
    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";

    public static Map<String, String> getTitles(String url, List<String> docList) {
        if (null == docList || docList.size() == 0) {
            return new HashMap<>(0);
        }
        int batch = 60;
        int len = docList.size();
        int index = 0;
        Map<String, String> docToTitle = new HashMap<>(len);
        while (index < len) {
            int end = index + batch > len ? len : index + batch;
            List<String> subList = docList.subList(index, end);
            index = end;
            Map<String, Object> args = new HashMap<>();
            args.put("fields", "title");
            args.put("version", 999999);
            args.put("docid", StringUtils.join(subList, ","));
            try {
                String response = HttpConnectionUtils.getGetResult(url, args);
                JSONObject jsonObject = JSONObject.parseObject(response);
                if (null != jsonObject && SUCCESS.equals(jsonObject.getString("status"))
                        && jsonObject.containsKey("documents")) {
                    JSONArray array = jsonObject.getJSONArray("documents");
                    for (Object object: array) {
                        JSONObject item = (JSONObject)object;
                        String docId = item.getString("docid");
                        String title = item.getString("title");
                        if (StringUtils.isNotEmpty(docId) && StringUtils.isNotEmpty(title)) {
                            docToTitle.put(docId, title);
                        }
                    }
                }

            } catch (Exception e) {
                log.error("could not get title due to :" + ExceptionUtils.getFullStackTrace(e));
            }
        }
        return docToTitle;
    }

    public static List<DocItem> getDocItems(String url, int initSize, int maxPoolSize, int batch, RequestConfig requestConfig) {
        int start = 0;
        List<DocItem> res = new ArrayList<>(initSize);
        while (start < maxPoolSize) {
            Map<String, Object> args = new HashMap<>(3);
            //method=getlist&start=600&length=100&query=%7B"selected":true%7D
            args.put("method", "getlist");
            args.put("start", start);
            args.put("length", batch);
            args.put("query", "{\"selected\":true}");
            try {
                String response = HttpConnectionUtils.getGetResult(url, args, requestConfig);
                JSONObject jsonObject = JSONObject.parseObject(response);
                if (null != jsonObject && SUCCESS.equals(jsonObject.getString("status"))
                        && jsonObject.containsKey("result")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    for (Object object : jsonArray) {
                        JSONObject item = (JSONObject)object;
                        String docId = item.getString("docId");
                        long millSeconds = item.getLongValue("date");
                        String date = new DateTime(millSeconds).toString("yyyy-MM-dd HH:mm:ss");
                        String title = item.getString("title");
                        String fromId = null;
                        JSONArray fromIds = item.getJSONArray("fromIds");
                        if (null != fromIds && fromIds.size() != 0) {
                            String str = fromIds.getString(0);
                            String[] arr = str.split(":");
                            if (arr.length == 2) {
                                fromId = arr[1];
                            }
                        }
                        DocItem docItem = new DocItem.Builder().withDocId(docId)
                                .withTitle(title).withFromId(fromId)
                                .withDate(date).build();
                        res.add(docItem);
                    }
                }
                else if (null != jsonObject && FAILED.equals(jsonObject.getString("status"))) {
                    break;
                }
            } catch (IOException e) {
                log.error("could not get the news with exception:" + ExceptionUtils.getFullStackTrace(e));
            }
            start += batch;
        }
        return res;
    }

}
