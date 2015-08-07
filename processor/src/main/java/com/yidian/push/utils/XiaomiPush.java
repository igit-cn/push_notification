package com.yidian.push.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.MessageType;
import com.yidian.push.data.ResourceType;
import com.yidian.push.data.XiaomiMessage;
import com.yidian.push.data.XiaomiSingleMessage;
import com.yidian.push.push_request.PushRecord;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.client.config.RequestConfig;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/7/29.
 */
@Log4j
public class XiaomiPush {
    private static final int MAX_XIAOMI_BATCH = 1000 - 1;
    @Getter
    public static class XiaomiData {
        private String appName;
        private String packageName;
        private String secret;

        public XiaomiData(String appName, String packageName, String secret) {
            this.appName = appName;
            this.packageName = packageName;
            this.secret = secret;
        }
    }


    public static final int BATCH = 1000;
    public static volatile boolean isInitialized = false;
    public static Map<String, XiaomiData> AppIdXiaomiDataMapping = new HashMap<>(10);

    public static void init() throws IOException {
        if (isInitialized) {
            return;
        }
        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        String pemBase = config.getXiaomiPemBase();
        String pemPattern = config.getXiaomiPemPattern();
        List<String> files = FileUtil.getFiles(pemPattern, pemBase);
        for (String file : files) {
            String appId = FileUtil.getAppId(file);
            String str = FileUtils.readFileToString(new File(file));
            String[] arr = str.split("\n");
            if (arr.length >= 3) {
                // appName, packageName, secret
                String appName = arr[0];
                String packageName = arr[1];
                String secret = arr[2];
                AppIdXiaomiDataMapping.put(appId, new XiaomiData(appName, packageName, secret));
                log.info("Xiaomi get the support with file : " + file);
            }
        }
        isInitialized = true;
    }

    public static XiaomiMessage buildMessage(PushRecord pushRecord, String token, MessageType messageType) throws IOException {
        if (!isInitialized) {
            init();
        }
        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        if (!AppIdXiaomiDataMapping.containsKey(pushRecord.getAppId())) {
            log.error(pushRecord.getAppId() + " not support by xiaomi ");
            return null;
        }
        XiaomiData xiaomiData = AppIdXiaomiDataMapping.get(pushRecord.getAppId());
        ResourceType resourceType = ResourceType.getResourceType(pushRecord.getDocId());
        String sound = Sound.getXiaomiNotifySound();
        String notifyType = Sound.getXiaomiNotifyType(sound);
        String showTitle = pushRecord.getTitle();
        if (StringUtils.isEmpty(showTitle)) {
            showTitle = xiaomiData.getAppName();
        }

        XiaomiMessage message = new XiaomiMessage.Build()
                .withDescription(pushRecord.getDescription())
                .withBadge(1)
                .withSound(sound)
                .withTitle(showTitle)
                .withDocId(pushRecord.getDocId())
                .withResourceType(resourceType)
                .withPushType(pushRecord.getNewsType() + "")
                .withNofityId(pushRecord.getNid())
                .withNotifyType(notifyType)
                .withToken(token)
                .withMessageType(messageType)
                .build();
        return message;
    }

    public static void pushMultipleMessages(List<XiaomiMessage> messages, String appId) throws IOException {
        if (null == messages || messages.size() == 0) {
            return;
        }
        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        String url = config.getXiaomiPushMultipleMessagesUrl();
        int batch = config.getXiaomiPushBatch();
        int retryTimes = config.getRetryTimes();
        int ttl = config.getMessageExpireTimeInSeconds() * 1000;
        RequestConfig requestConfig = config.getRequestConfig();
        pushMultipleMessages(messages, appId, url, batch, retryTimes, ttl, requestConfig);
    }

    public static void pushMultipleMessages(List<XiaomiMessage> xiaomiMessageList,
                                            String appId, String url,
                                            int batch, int retry, int ttl,
                                            RequestConfig requestConfig) {
        if (null == xiaomiMessageList || xiaomiMessageList.size() == 0) {
            return;
        }

        String sendTime = DateTime.now().toString("HH:mm");
        XiaomiData xiaomiData = AppIdXiaomiDataMapping.get(appId);
        List<JSONObject> pushDataList = new ArrayList<>(xiaomiMessageList.size());
        for (XiaomiMessage xiaomiMessage : xiaomiMessageList) {
            // for recommend news: we just use the app name as the title
            String showTitle = xiaomiData.getAppName();

            JSONObject pushOne = new JSONObject();
            JSONObject payload = new JSONObject();
            JSONObject aps = new JSONObject();
            aps.put("alert", xiaomiMessage.getDescription());
            aps.put("badge", xiaomiMessage.getBadge());
            aps.put("sound", xiaomiMessage.getSound());
            payload.put("aps", aps);
            payload.put("rid", xiaomiMessage.getDocId());
            payload.put("rtype", xiaomiMessage.getResourceType().toString());
            payload.put("PT", xiaomiMessage.getPushType());
            pushOne.put("payload", payload);
            pushOne.put("nid", xiaomiMessage.getNotifyId());
            pushOne.put("token", xiaomiMessage.getToken());
            pushOne.put("title", showTitle);
            pushOne.put("pth", xiaomiMessage.getMessageType().getIntVal());

            JSONObject layoutData = new JSONObject();
            JSONObject text = new JSONObject();
            JSONObject image = new JSONObject();
            layoutData.put("text", text);
            layoutData.put("image", image);
            text.put("title", showTitle);
            text.put("text", xiaomiMessage.getDescription());
            text.put("date", sendTime);
            image.put("image", "hipu_push");

            JSONObject dataToPush = new JSONObject();
            dataToPush.put("target", xiaomiMessage.getToken());
            JSONObject message = new JSONObject();
            JSONObject extra = new JSONObject();
            dataToPush.put("message", message);
            message.put("extra", extra);
            message.put("title", showTitle);
            message.put("description", xiaomiMessage.getDescription());
            message.put("restricted_package_name", xiaomiData.getPackageName());
            message.put("notify_type", xiaomiMessage.getNotifyType());
            message.put("pass_through", xiaomiMessage.getMessageType().getIntVal());
            message.put("notify_id", xiaomiMessage.getNotifyId());
            message.put("time_to_live", ttl);
            message.put("payload", payload.toString());
            extra.put("layout_name", "push_notification_item");
            extra.put("layout_value", layoutData.toString());

            pushDataList.add(dataToPush);
        }

        Map<String, String> headers = new HashMap<>(3);
        headers.put("Authorization", "key=" + xiaomiData.getSecret());
        int length = pushDataList.size();
        int index = 0;
        while (index < length) {
            int start = index;
            int end = (index + batch) > length ? length : index + batch;
            index += batch;
            List<JSONObject> subList = pushDataList.subList(start, end);
            String messageToSend = GsonFactory.getNonPrettyGson().toJson(subList);
            Map<String, Object> params = new HashMap<>();
            params.put("messages", messageToSend.toString());
            int timesToRetry = retry;
            while (timesToRetry > 0) {
                try {
                    String response = HttpConnectionUtils.getPostResult(url, params, headers, requestConfig);
                    JSONObject json = JSON.parseObject(response);
                    if (null != json && "0".equals(json.getString("code"))) {
                        break;
                    } else {
                        if (null != json) {
                            log.error("failed with reason " + json.getString("reason"));
                        }
                        timesToRetry --;
                    }
                } catch (IOException e) {
                    log.error("xiaomi push failed with Exception " + ExceptionUtils.getFullStackTrace(e));
                    timesToRetry --;
                }
            }
        }
    }

    public static void pushSingleMessage(XiaomiSingleMessage xiaomiSingleMessage) throws IOException {

        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        String url = config.getXiaomiPushSingleMessageUrl();
        int batch = config.getXiaomiPushBatch();
        int retryTimes = config.getRetryTimes();
        int ttl = config.getMessageExpireTimeInSeconds() * 1000;
        RequestConfig requestConfig = config.getRequestConfig();
        pushSingleMessage(xiaomiSingleMessage, url, batch, retryTimes, ttl, requestConfig);

    }

    public static void pushSingleMessage(XiaomiSingleMessage xiaomiMessage,
                                         String url,
                                         int batch, int retry, int ttl,
                                         RequestConfig requestConfig) {
        String appId = xiaomiMessage.getAppId();
        String sendTime = DateTime.now().toString("HH:mm");
        XiaomiData xiaomiData = AppIdXiaomiDataMapping.get(appId);
        String showTitle = xiaomiMessage.getTitle();
        if (StringUtils.isEmpty(showTitle)) {
            showTitle = xiaomiData.getAppName();
        }

        JSONObject payload = new JSONObject();
        JSONObject aps = new JSONObject();
        aps.put("alert", xiaomiMessage.getDescription());
        aps.put("badge", xiaomiMessage.getBadge());
        aps.put("sound", xiaomiMessage.getSound());
        payload.put("aps", aps);
        payload.put("rid", xiaomiMessage.getDocId());
        payload.put("rtype", xiaomiMessage.getResourceType().toString());
        payload.put("PT", xiaomiMessage.getPushType());
        String payloadStr = payload.toString();

        JSONObject layoutData = new JSONObject();
        JSONObject text = new JSONObject();
        JSONObject image = new JSONObject();
        layoutData.put("text", text);
        layoutData.put("image", image);
        text.put("title", showTitle);
        text.put("text", xiaomiMessage.getDescription());
        text.put("date", sendTime);
        image.put("image", "hipu_push");
        String layoutDataStr = layoutData.toString();

        Map<String, String> headers = new HashMap<>(3);
        headers.put("Authorization", "key=" + xiaomiData.getSecret());
        Map<String, Object> params = new HashMap<>(10);
        params.put("title", showTitle);
        params.put("description", xiaomiMessage.getDescription());
        params.put("restricted_package_name", xiaomiData.getPackageName());
        params.put("notify_type", xiaomiMessage.getNotifyType());
        params.put("pass_through", String.valueOf(xiaomiMessage.getMessageType().getIntVal()));
        params.put("notify_id", xiaomiMessage.getNotifyId());
        params.put("time_to_live", ttl);
        params.put("payload", payloadStr);
        params.put("extra.layout_name", "push_notification_item");
        params.put("extra.layout_value", layoutDataStr);

        int index = 0;
        int length = xiaomiMessage.getPushNumber();
        List<String> tokens = xiaomiMessage.getTokens();
        while (index < length) {
            int start = index;
            int end = (index + batch) > length ? length : index + batch;
            index += batch;
            List<String> subTokens = tokens.subList(start, end);
            params.put("alias", subTokens);
            //System.out.println(GsonFactory.getNonPrettyGson().toJson(params));
            int timesToRetry = retry;
            while (timesToRetry > 0) {
                try {
                    String response = HttpConnectionUtils.getPostResult(url, params, headers, requestConfig);
                    JSONObject json = JSON.parseObject(response);
                    if (null != json && "ok".equals(json.getString("result"))) {
                        break;
                    } else {
                        if (null != json) {
                            log.error("failed with reason " + json.getString("reason"));
                        }
                        timesToRetry--;
                    }
                } catch (IOException e) {
                    log.error("xiaomi push failed");
                    timesToRetry--;
                }
            }
        }
    }
}