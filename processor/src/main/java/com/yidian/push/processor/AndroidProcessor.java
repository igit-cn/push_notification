package com.yidian.push.processor;

import com.yidian.push.config.Config;
import com.yidian.push.data.*;
import com.yidian.push.push_request.PushRecord;
import com.yidian.push.push_request.PushRequest;
import com.yidian.push.push_request.PushRequestManager;
import com.yidian.push.utils.*;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yidianadmin on 15-2-9.
 */
@Log4j
public class AndroidProcessor {
    private static final int BATCH_SIZE = 100;
    private static final int XIAOMI_BATCH_SIZE = 1000;

    public static void processOne(PushRequest pushRequest) throws IOException {
        try {
            String pushType = pushRequest.getPushType();
            if (Config.getInstance().getProcessorConfig().shouldUseRecommendPush(PushType.getPushType(pushType))) {
                processRecommend(pushRequest.getFileName());
            }
            else {
                processNormal(pushRequest.getFileName());
            }
            PushRequestManager.getInstance().markAsProcessed(pushRequest);
        } catch (Exception e) {
            PushRequestManager.getInstance().markAsBad(pushRequest);
            log.error("marked as bad due to exception : " + ExceptionUtils.getFullStackTrace(e));
        }
    }

    public static void processRecommend(String file) throws IOException {
        Charset UTF_8 = StandardCharsets.UTF_8;
        Path filePath = new File(file).toPath();
        BufferedReader reader = null;
        List<PushLog.LogItem> logs = new ArrayList<>(BATCH_SIZE);
        List<UmengMessage> umengMessages = new ArrayList<>(BATCH_SIZE);
        List<GetuiMessage> getuiMessages = new ArrayList<>(BATCH_SIZE);
        Map<String, List<XiaomiMessage>> appIdXiaomiMessageMapping = new HashMap<>(5);

        long lastUid = -1;
        long totalPushCount = 0;
        try {
            reader = Files.newBufferedReader(filePath, UTF_8);
            String line = null;
            while ((line = reader.readLine()) != null) {
                PushRecord pushRecord = new PushRecord(line);
                if (!pushRecord.isValid()) {
                    log.error("invalid record line:" + line);
                    continue;
                }
                ResourceType resourceType = ResourceType.getResourceType(pushRecord.getDocId());
                List<FilterToken.TokenPushChannelMessageType> tokenPassThroughList
                        = FilterToken.filterTokens(pushRecord, pushRecord.getPushChannel(), true);
                for (FilterToken.TokenPushChannelMessageType tokenPassThrough : tokenPassThroughList) {
                    PushChannel pushChannel = tokenPassThrough.getPushChannel();
                    String token = tokenPassThrough.getToken();
                    MessageType messageType = tokenPassThrough.getMessageType();
                    if (pushChannel == PushChannel.UMENG) {
                        UmengMessage message = UmengPush.buildMessage(pushRecord, token, messageType);
                        umengMessages.add(message);
                        if (umengMessages.size() >= BATCH_SIZE) {
                            UmengPush.push(umengMessages);
                            umengMessages.clear();
                        }
                    }
                    else if (pushChannel == PushChannel.GETUI) {
                        GetuiMessage message = GetuiPush.buildMessage(pushRecord, token, messageType);
                        getuiMessages.add(message);
                        if (getuiMessages.size() >= BATCH_SIZE) {
                            GetuiPush.push(getuiMessages);
                            getuiMessages.clear();
                        }
                    }
                    else if (pushChannel == PushChannel.XIAOMI) {
                        String appId = pushRecord.getAppId();
                        List<XiaomiMessage> xiaomiMessages = null;
                        if (appIdXiaomiMessageMapping.containsKey(appId)) {
                            xiaomiMessages = appIdXiaomiMessageMapping.get(appId);
                        }
                        else {
                            xiaomiMessages = new ArrayList<>(XIAOMI_BATCH_SIZE);
                            appIdXiaomiMessageMapping.put(appId, xiaomiMessages);
                        }
                        xiaomiMessages.add(XiaomiPush.buildMessage(pushRecord, token, messageType));
                        if (xiaomiMessages.size() >= XIAOMI_BATCH_SIZE) {
                            XiaomiPush.pushMultipleMessages(xiaomiMessages, appId);
                            xiaomiMessages.clear();
                        }
                    }
                }
                if (lastUid != pushRecord.getUid()) {
                    lastUid = pushRecord.getUid();
                    logs.add(new PushLog.LogItem(DateTime.now().getMillis(),
                            pushRecord.getUid(),
                            pushRecord.getDocId(),
                            pushRecord.getNewsChannel(),
                            pushRecord.getNewsType(),
                            pushRecord.getAppId()));
                    if (logs.size() >= BATCH_SIZE) {
                        WritePushLog.writeLogIgnoreException(Platform.ANDROID, logs);
                        logs.clear();
                    }
                    totalPushCount ++;
                }
            }
            for (String appId : appIdXiaomiMessageMapping.keySet()) {
                List<XiaomiMessage> messages = appIdXiaomiMessageMapping.get(appId);
                XiaomiPush.pushMultipleMessages(messages, appId);
                messages.clear();
            }
            if (umengMessages.size() > 0) {
                UmengPush.push(umengMessages);
                umengMessages.clear();
            }
            if (getuiMessages.size() > 0) {
                GetuiPush.push(getuiMessages);
                getuiMessages.clear();
            }
            WritePushLog.writeLogIgnoreException(Platform.ANDROID, logs);
            logs.clear();
            log.info("total push " + totalPushCount + " users for file " + file);
        } finally {
            if (null != reader) {try {reader.close();} catch (Exception ignore){}}
        }
    }

    /**
     * this kind of request are pushing the same record.

     */
    public static void processNormal(String file) throws IOException {
        Charset UTF_8 = StandardCharsets.UTF_8;
        Path filePath = new File(file).toPath();
        BufferedReader reader = null;
        List<PushLog.LogItem> logs = new ArrayList<>(BATCH_SIZE);
        List<UmengMessage> umengMessages = new ArrayList<>(BATCH_SIZE);
        List<GetuiMessage> getuiMessages = new ArrayList<>(BATCH_SIZE);
        Map<String, List<String>> appId_PassThough_NotifyId_NotifyType_Tokens_Mapping = new HashMap<>(30);

        long lastUid = -1;
        long totalPushCount = 0;
        try {
            reader = Files.newBufferedReader(filePath, UTF_8);
            String line = null;
            String title = null;
            String description = null;
            String docId = null;
            int pushType = 0;
            boolean isFirstTime = true;
            while ((line = reader.readLine()) != null) {
                PushRecord pushRecord = new PushRecord(line);
                if (!pushRecord.isValid()) {
                    log.error("invalid record line:" + line);
                    continue;
                }
                if (isFirstTime) {
                    title = pushRecord.getTitle();
                    description = pushRecord.getDescription();
                    pushType = pushRecord.getNewsType();
                    docId = pushRecord.getDocId();
                    isFirstTime = false;
                }
                ResourceType resourceType = ResourceType.getResourceType(pushRecord.getDocId());
                List<FilterToken.TokenPushChannelMessageType> tokenPassThroughList
                        = FilterToken.filterTokens(pushRecord, pushRecord.getPushChannel(), true);
                for (FilterToken.TokenPushChannelMessageType tokenPassThrough : tokenPassThroughList) {
                    PushChannel pushChannel = tokenPassThrough.getPushChannel();
                    String token = tokenPassThrough.getToken();
                    MessageType messageType = tokenPassThrough.getMessageType();
                    if (pushChannel == PushChannel.UMENG) {
                        UmengMessage message = UmengPush.buildMessage(pushRecord, token, messageType);
                        umengMessages.add(message);
                        if (umengMessages.size() >= BATCH_SIZE) {
                            UmengPush.push(umengMessages);
                            umengMessages.clear();
                        }
                    }
                    else if (pushChannel == PushChannel.GETUI) {
                        GetuiMessage message = GetuiPush.buildMessage(pushRecord, token, messageType);
                        getuiMessages.add(message);
                        if (getuiMessages.size() >= BATCH_SIZE) {
                            GetuiPush.push(getuiMessages);
                            getuiMessages.clear();
                        }
                    }
                    else if (pushChannel == PushChannel.XIAOMI) {
                        String appId = pushRecord.getAppId();
                        XiaomiMessage message = XiaomiPush.buildMessage(pushRecord, token, messageType);
                        String appId_PassThough_NotifyId_NotifyType = new StringBuilder()
                                .append(appId).append("_")
                                .append(message.getMessageType().toString()).append("_")
                                .append(message.getNotifyId()).append("_")
                                .append(message.getNotifyType()).toString();

                        List<String> tokenList = null;
                        if (appId_PassThough_NotifyId_NotifyType_Tokens_Mapping.containsKey(appId_PassThough_NotifyId_NotifyType)) {
                            tokenList = appId_PassThough_NotifyId_NotifyType_Tokens_Mapping.get(appId_PassThough_NotifyId_NotifyType);
                        }
                        else {
                            tokenList = new ArrayList<>(XIAOMI_BATCH_SIZE);
                            appId_PassThough_NotifyId_NotifyType_Tokens_Mapping.put(appId_PassThough_NotifyId_NotifyType, tokenList);
                        }
                        tokenList.add(token);
                        if (tokenList.size() >= XIAOMI_BATCH_SIZE) {
                            pushXiaomi(appId_PassThough_NotifyId_NotifyType, title, description, docId, pushType, tokenList);
                            tokenList.clear();
                        }
                    }
                }
                if (lastUid != pushRecord.getUid()) {
                    lastUid = pushRecord.getUid();
                    logs.add(new PushLog.LogItem(DateTime.now().getMillis(),
                            pushRecord.getUid(),
                            pushRecord.getDocId(),
                            pushRecord.getNewsChannel(),
                            pushRecord.getNewsType(),
                            pushRecord.getAppId()));
                    if (logs.size() >= BATCH_SIZE) {
                        WritePushLog.writeLogIgnoreException(Platform.ANDROID, logs);
                        logs.clear();
                    }
                    totalPushCount ++;
                }
            }
            for (String key : appId_PassThough_NotifyId_NotifyType_Tokens_Mapping.keySet()) {
                List<String> tokenList = appId_PassThough_NotifyId_NotifyType_Tokens_Mapping.get(key);
                pushXiaomi(key, title, description, docId, pushType, tokenList);
                tokenList.clear();
            }
            if (umengMessages.size() > 0) {
                UmengPush.push(umengMessages);
                umengMessages.clear();
            }
            if (getuiMessages.size() > 0) {
                GetuiPush.push(getuiMessages);
                getuiMessages.clear();
            }
            WritePushLog.writeLogIgnoreException(Platform.ANDROID, logs);
            logs.clear();
            log.info("total push " + totalPushCount + " users for file " + file);
        } finally {
            if (null != reader) {try {reader.close();} catch (Exception ignore){}}
        }
    }

    private static void pushXiaomi(String appId_PassThough_NotifyId_NotifyType, String title,
                            String description, String docId,
                            int pushType, List<String> tokens) throws IOException {
        if (null == tokens || tokens.size() == 0) {
            return;
        }
        String[] arr = appId_PassThough_NotifyId_NotifyType.split("_");
        if (arr.length < 4) {
            log.error("bad key for appId_PassThough_NotifyId_NotifyType : " + appId_PassThough_NotifyId_NotifyType);
            return;
        }
        String appId = arr[0];
        MessageType messageType = MessageType.getMessageType(arr[1]);
        int notifyId = Integer.parseInt(arr[2]);
        String notifyType = arr[3]; // related to sound
        ResourceType resourceType = ResourceType.getResourceType(docId);
        String sound = Sound.getXiaomiNotifySound(notifyType);

        XiaomiSingleMessage xiaomiSingleMessage = new XiaomiSingleMessage.Build()
                .withAppId(appId)
                .withDescription(description)
                .withBadge(1)
                .withSound(sound)
                .withTitle(title)
                .withDocId(docId)
                .withResourceType(resourceType)
                .withPushType(pushType + "")
                .withNotifyId(notifyId)
                .withNotifyType(notifyType)
                .withTokens(tokens)
                .withMessageType(messageType)
                .build();

        XiaomiPush.pushSingleMessage(xiaomiSingleMessage);

    }
}
