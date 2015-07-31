package com.yidian.push.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.GetuiMessage;
import com.yidian.push.data.MessageType;
import com.yidian.push.data.ResourceType;
import com.yidian.push.push_request.PushRecord;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.config.RequestConfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/7/29.
 */
@Log4j
public class GetuiPush {
    @Setter
    @Getter
    public static class GetuiData{
        private String appName;
        private String appKey;
        private String appId;
        private String masterSecret;
        public GetuiData(String appName, String appKey, String appId, String masterSecret) {
            this.appName = appName;
            this.appKey = appKey;
            this.appId = appId;
            this.masterSecret = masterSecret;
        }
    }

    public static volatile boolean isInitialized = false;
    public static Map<String, GetuiData> AppIdGetuiDataMapping = new HashMap<>(10);

    public static void init() throws IOException {
        if (isInitialized) {return;}
        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        String pemBase = config.getGetuiPemBase();
        String pemPattern = config.getGetuiPemPattern();
        List<String> files =  FileUtil.getFiles(pemPattern, pemBase);
        for (String file : files) {
            String appId = FileUtil.getAppId(file);
            String str = FileUtils.readFileToString(new File(file));
            String[] arr = str.split("\n");
            if (arr.length >= 4) {
                // name, app_key, app_id, master_secret
                String appName = arr[0];
                String appKey = arr[1];
                String getTuiAppId = arr[2];
                String masterSecret = arr[3];
                AppIdGetuiDataMapping.put(appId, new GetuiData(appName, appKey, getTuiAppId, masterSecret));
                log.info("Getui get the support with file : " + file);
            }
        }
        isInitialized = true;
    }


    public static GetuiMessage buildMessage(PushRecord pushRecord, String token,  MessageType messageType) throws IOException {
        if (!isInitialized) {init();}
        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        if (!AppIdGetuiDataMapping.containsKey(pushRecord.getAppId())) {
            log.error(pushRecord.getAppId() + " not support by umeng ");
            return null;
        }
        GetuiData getuiData = AppIdGetuiDataMapping.get(pushRecord.getAppId());
        int expireTime = config.getMessageExpireTimeInSeconds();
        ResourceType resourceType = ResourceType.getResourceType(pushRecord.getDocId());
        String sound = Sound.getUmengNotifySound();
        String payload = new GetuiMessage.PayloadBuilder()
                .withTitle(pushRecord.getTitle())
                .withDescription(pushRecord.getDescription())
                .withDocId(pushRecord.getDocId())
                .withPushType(pushRecord.getNewsType() + "")
                .withResourceType(resourceType)
                .withSound(sound)
                .build();

        GetuiMessage message = new GetuiMessage.Build()
                .withAppId(getuiData.getAppId())
                .withToken(token)
                .withPayload(payload)
                .withAppKey(getuiData.getAppKey())
                .build();
        return message;
    }


    public static void push(List<GetuiMessage> messages) throws IOException {
        if (null == messages || messages.size() == 0){
            return;
        }
        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        String url = config.getGetuiPushBatchUrl();
        int batch = config.getGetuiPushBatch();
        int retryTimes = config.getRetryTimes();
        RequestConfig requestConfig = config.getRequestConfig();
        push(messages, url, batch, retryTimes, requestConfig);
    }

    public static void push(List<GetuiMessage> messageList, String url, int batch, int retry, RequestConfig requestConfig) {
        if (null == messageList || messageList.size() == 0){
            return;
        }

        int length = messageList.size();
        int index = 0;
        while (index < length) {
            int start = index;
            int end = (index + batch) > length ? length : index + batch;
            index += batch;
            List<GetuiMessage> subList = messageList.subList(start, end);
            String messageToSend = GsonFactory.getNonPrettyGson().toJson(subList);
            Map<String, Object> params = new HashMap<>();
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
                    log.error("Upns config failed");
                    timesToRetry --;
                }
            }
        }
    }
}

