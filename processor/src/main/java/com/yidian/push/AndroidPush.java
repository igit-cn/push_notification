package com.yidian.push;

import com.yidian.push.data.*;
import com.yidian.push.push_request.PushRecord;
import com.yidian.push.push_request.PushRequest;
import com.yidian.push.utils.APNS;
import com.yidian.push.utils.FilterToken;
import com.yidian.push.utils.WritePushLog;
import lombok.extern.log4j.Log4j;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yidianadmin on 15-2-9.
 */
@Log4j
public class AndroidPush implements Push {
    private static final int BATCH_SIZE = 1000;
    @Override
    public void pushFile(PushRequest request) {
        String pushType = request.getPushType();
        if (PushType.isRecommendPush(pushType)) {

        }

    }

    public void processRecommend(PushRequest request) throws IOException {
        Charset UTF_8 = StandardCharsets.UTF_8;
        Path filePath = new File(request.getFileName()).toPath();
        BufferedReader reader = null;
        List<PushLog.LogItem> logs = new ArrayList<>(BATCH_SIZE);
        List<APNSMessage> payloadList = new ArrayList<>(BATCH_SIZE);

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
                ResourceType rType = ResourceType.getResourceType(pushRecord.getDocId());
                List<FilterToken.TokenPushChannelMessageType> tokenPassThroughList
                        = FilterToken.filterTokens(pushRecord, pushRecord.getPushChannel(), true);
                for (FilterToken.TokenPushChannelMessageType tokenPassThrough : tokenPassThroughList) {

                }
                if (lastUid != pushRecord.getUid()) {
                    lastUid = pushRecord.getUid();
                    logs.add(new PushLog.LogItem(DateTime.now().getMillis(),
                            pushRecord.getUid(),
                            pushRecord.getDocId(),
                            pushRecord.getNewsChannel(),
                            pushRecord.getNewsType()));
                    if (logs.size() >= BATCH_SIZE) {
                        WritePushLog.writeLogIgnoreException(Platform.ANDROID, logs);
                        logs.clear();
                    }
                    totalPushCount ++;
                }
            }
            if (payloadList.size() > 0) {
                APNS.push(payloadList);
                payloadList.clear();
            }
            WritePushLog.writeLogIgnoreException(Platform.ANDROID, logs);
            logs.clear();
            log.info("total push " + totalPushCount + " users for file " + request.getFileName());
        } finally {
            if (null != reader) {try {reader.close();} catch (Exception ignore){}}
        }
    }

    public void processNormal(PushRecord record) {

    }
}
