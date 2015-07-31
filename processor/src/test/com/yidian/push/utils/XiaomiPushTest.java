package com.yidian.push.utils;

import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.*;
import org.apache.http.client.config.RequestConfig;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tianyuzhi on 15/7/31.
 */
public class XiaomiPushTest {
    private ProcessorConfig processorConfig;
    @BeforeClass
    public void before() throws IOException {
        String projectDir = System.getProperty("user.dir");
        Config.setCONFIG_FILE(projectDir + "/processor/src/main/resources/config/config.json");
        processorConfig = Config.getInstance().getProcessorConfig();
        XiaomiPush.init();
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "DEBUG");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.impl.conn", "DEBUG");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.impl.client", "DEBUG");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.client", "DEBUG");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "DEBUG");
    }

    @Test
    public void testPushMultipleMessages() throws Exception {
        String url = processorConfig.getXiaomiPushMultipleMessagesUrl();
        int batch = 1;
        int retry = 2;
        int timeout = 5;
        int ttl = 3 * 3600 ; // 3 hours
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        String appId = "yidian";
        int notifyId = 0;
        String token = "MMPP8CBEBEC85544863583028663590";
        List<XiaomiMessage> list = Arrays.asList(
                new XiaomiMessage.Build()
                        .withDescription("xiaomi description 1")
                        .withBadge(1)
                        .withSound("")
                        .withTitle("xiaomi title 1")
                        .withDocId("0A6a7TUj")
                        .withResourceType(ResourceType.NEWS)
                        .withPushType(PushType.MORNING.getString())
                        .withNofityId(notifyId++)
                        .withNotifyType("1")
                        .withToken(token)
                        .withMessageType(MessageType.NOTIFICATION)
                        .build(),

                new XiaomiMessage.Build()
                        .withDescription("xiaomi description 2")
                        .withBadge(1)
                        .withSound("")
                        .withTitle("xiaomi title 2")
                        .withDocId("l_012100e0057f0842051f000d")
                        .withResourceType(ResourceType.TOPIC)
                        .withPushType(PushType.MORNING.getString())
                        .withNofityId(notifyId++)
                        .withNotifyType("1")
                        .withToken(token)
                        .withMessageType(MessageType.NOTIFICATION)
                        .build(),

                new XiaomiMessage.Build()
                        .withDescription("xiaomi description 3")
                        .withBadge(1)
                        .withSound("")
                        .withTitle("xiaomi title 3")
                        .withDocId("0A6hzn67")
                        .withResourceType(ResourceType.NEWS)
                        .withPushType(PushType.BREAK.getString())
                        .withNofityId(notifyId++)
                        .withNotifyType("1")
                        .withToken(token)
                        .withMessageType(MessageType.MESSAGE)
                        .build()

                );
//
//        list = Arrays.asList(
//
//
//                new XiaomiMessage.Build()
//                        .withDescription("xiaomi description 2")
//                        .withBadge(1)
//                        .withSound("")
//                        .withTitle("xiaomi title 2")
//                        .withDocId("l_012100e0057f0842051f000d")
//                        .withResourceType(ResourceType.TOPIC)
//                        .withPushType(PushType.MORNING.getString())
//                        .withNotifyId(notifyId++)
//                        .withNotifyType("1")
//                        .withToken(token)
//                        .withMessageType(MessageType.NOTIFICATION)
//                        .build()
//
//        );
        XiaomiPush.pushMultipleMessages(list, appId, url, batch, retry, ttl, config);

    }

    @Test
    public void testPushSingleMessage() throws Exception {
        String url = processorConfig.getXiaomiPushSingleMessageUrl();
        int batch = 2;
        int retry = 2;
        int timeout = 5;
        int ttl = 3 * 3600 ; // 3 hours
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        String sound = "";
        int notifyId = 0;
        String notifyType = Sound.getXiaomiNotifyType(sound);
        List<String> tokens = Arrays.asList(
                "MMPP8CBEBEC85544863583028663590",
                "MMPP0C1DAFCA3BB1865687021150394");
        String docId = "0A64Nbuh";
        XiaomiSingleMessage xiaomiSingleMessage = new XiaomiSingleMessage.Build()
                .withAppId("xiaomi")
                .withDescription("single message")
                .withBadge(1)
                .withSound(sound)
                .withTitle("with title")
                .withDocId(docId)
                .withResourceType(ResourceType.NEWS)
                .withPushType("64")
                .withNotifyId(notifyId)
                .withNotifyType(notifyType)
                .withTokens(tokens)
                .withMessageType(MessageType.NOTIFICATION)
                .build();
        XiaomiPush.pushSingleMessage(xiaomiSingleMessage, url, batch, retry, ttl, config);


        docId = "l_012100e0057f0842051f000d";
        notifyId ++;
        System.out.println(notifyId);
        xiaomiSingleMessage = new XiaomiSingleMessage.Build()
                .withAppId("xiaomi")
                .withDescription("single message: no title ")
                .withBadge(1)
                .withSound(sound)
                .withTitle("")
                .withDocId(docId)
                .withResourceType(ResourceType.TOPIC)
                .withPushType("64")
                .withNotifyId(notifyId)
                .withNotifyType(notifyType)
                .withTokens(tokens)
                .withMessageType(MessageType.NOTIFICATION)
                .build();
        XiaomiPush.pushSingleMessage(xiaomiSingleMessage, url, batch, retry, ttl, config);

    }
}