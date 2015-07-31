package com.yidian.push.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.MessageType;
import com.yidian.push.data.ResourceType;
import com.yidian.push.data.UmengMessage;
import com.yidian.push.push_request.PushRecord;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.config.RequestConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/7/29.
 */
@Log4j
public class UmengPush {
    private static final int EXPIRE_TIME = 3 * 60 * 60; // 3 HOURS
    public static UmengMessage buildMessage(PushRecord pushRecord, String token, MessageType messageType) throws IOException {
        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        String appName = config.getUMENG_APPID_NAME_MAPPING().get(pushRecord.getAppId());
        int expireTime = config.getMessageExpireTimeInSeconds();
        if (StringUtils.isEmpty(appName)) {
            log.error(pushRecord.getAppId() + " not support by umeng ");
            return null;
        }
        ResourceType resourceType = ResourceType.getResourceType(pushRecord.getDocId());
        String sound = Sound.getUmengNotifySound();
        String payload = new UmengMessage.PayloadBuilder()
                .withTitle(pushRecord.getTitle())
                .withDescription(pushRecord.getDescription())
                .withDocId(pushRecord.getDocId())
                .withMessageType(messageType)
                .withPushType(pushRecord.getNewsType() + "")
                .withResourceType(resourceType)
                .withSound(sound)
                .withAppName(appName)
                .build();
        String policy = new UmengMessage.PolicyBuilder().withExpireTimeInSeconds(expireTime).build();

        UmengMessage umengMessage = new UmengMessage.Build()
                .withAppId(pushRecord.getAppId())
                .withThirdPartyId("test.yidianzixun.com")
                .withAlias(token)
                .withAliasType(pushRecord.getAppId())
                .withPayload(payload)
                .withPolicy(policy)
                .build();
        return umengMessage;
    }

    public static void push(List<UmengMessage> messages) throws IOException {
        if (null == messages || messages.size() == 0){
            return;
        }
        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        String url = config.getUmengPushBatchUrl();
        int batch = config.getUmengPushBatch();
        int retryTimes = config.getRetryTimes();
        RequestConfig requestConfig = config.getRequestConfig();
        push(messages, url, batch, retryTimes, requestConfig);
    }

    public static void push(List<UmengMessage> messageList, String url, int batch, int retry, RequestConfig requestConfig) {
        if (null == messageList || messageList.size() == 0){
            return;
        }

        int length = messageList.size();
        int index = 0;
        while (index < length) {
            int start = index;
            int end = (index + batch) > length ? length : index + batch;
            index += batch;
            List<UmengMessage> subList = messageList.subList(start, end);
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
