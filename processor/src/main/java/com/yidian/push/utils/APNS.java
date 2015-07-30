package com.yidian.push.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.APNSMessage;
import lombok.extern.log4j.Log4j;
import org.apache.http.client.config.RequestConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/7/29.
 */
@Log4j
public class APNS {
    public static void push(List<APNSMessage> payloads) throws IOException {
        if (null == payloads || payloads.size() == 0){
            return;
        }
        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        String url = config.getIosPushBatchUrl();
        int batch = config.getIosPushBatch();
        int retryTimes = config.getRetryTimes();
        RequestConfig requestConfig = config.getRequestConfig();
        push(payloads, url, batch, retryTimes, requestConfig);
    }

    public static void push(List<APNSMessage> payloads, String url, int batch, int retry, RequestConfig requestConfig) {
        if (null == payloads || payloads.size() == 0){
            return;
        }

        int length = payloads.size();
        int index = 0;
        while (index < length) {
            int start = index;
            int end = (index + batch) > length ? length : index + batch;
            index += batch;
            List<APNSMessage> subList = payloads.subList(start, end);
            String messageToSend = GsonFactory.getNonPrettyGson().toJson(subList);
            Map<String, String> params = new HashMap<>();
            params.put("messages", messageToSend);
            int timesToRetry = retry;
            while (timesToRetry > 0) {
                try {
                    String response = HttpConnectionUtils.getPostResult(url, params, requestConfig);
                    JSONObject json = JSON.parseObject(response);
                    if (null != json && "0".equals(json.getString("code"))) {
                        break;
                    } else {
                        timesToRetry --;
                    }
                } catch (IOException e) {
                    log.error("APNS config failed");
                    timesToRetry --;
                }
            }
        }
    }
}
