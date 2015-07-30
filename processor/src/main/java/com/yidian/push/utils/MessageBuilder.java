package com.yidian.push.utils;

import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.MessageType;
import com.yidian.push.data.ResourceType;
import com.yidian.push.data.UmengMessage;
import com.yidian.push.push_request.PushRecord;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * Created by tianyuzhi on 15/7/29.
 */
@Log4j
public class MessageBuilder {
    private static final int EXPIRE_TIME = 3 * 60 * 60; // 3 HOURS
    public static UmengMessage buildUmengMessage(PushRecord pushRecord, String token,  MessageType messageType, int expireTime) throws IOException {
        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        String appName = config.getUMENG_APPID_NAME_MAPPING().get(pushRecord.getAppId());
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
        String policy = new UmengMessage.PolicyBuilder().withExpireTimeInMinutes(expireTime).build();

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
}
