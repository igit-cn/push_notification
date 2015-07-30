package com.yidian.push.data;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/7/30.
 */
public class PayloadBuilderTest {
    @Test
    public void test() {
//        private String title;
//        private String description;
//        private String docId;
//        private MessageType messageType;
//        private String pushType;
//        private ResourceType resourceType;
//        private String sound;
//        private String appName;
        String payload = new UmengMessage.PayloadBuilder()
                .withTitle("")
                .withDescription("description")
                .withDocId("docid")
                .withMessageType(MessageType.MESSAGE)
                .withPushType(PushType.BREAK.getString())
                .withResourceType(ResourceType.NEWS)
                .withSound("")
                .withAppName("yidian")
                .build();
        System.out.println(payload);

        payload = new UmengMessage.PayloadBuilder()
                .withTitle("title")
                .withDescription("description")
                .withDocId("docid")
                .withMessageType(MessageType.NOTIFICATION)
                .withPushType(PushType.BREAK.getString())
                .withResourceType(ResourceType.NEWS)
                .withSound("")
                .withAppName("yidian")
                .build();
        System.out.println(payload);
    }

}