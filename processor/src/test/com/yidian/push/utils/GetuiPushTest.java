package com.yidian.push.utils;

import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.GetuiMessage;
import com.yidian.push.data.MessageType;
import com.yidian.push.data.ResourceType;
import com.yidian.push.data.UmengMessage;
import org.apache.http.client.config.RequestConfig;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/7/31.
 */
public class GetuiPushTest {
    private ProcessorConfig processorConfig;
    @BeforeClass
    public void before() throws IOException {
        String projectDir = System.getProperty("user.dir");
        Config.setCONFIG_FILE(projectDir + "/src/main/resources/config/config.json");
        processorConfig = Config.getInstance().getProcessorConfig();
    }
    @Test
    public void testPush() throws Exception {
        String url = processorConfig.getGetuiPushBatchUrl();
        int batch = 1;
        int retry = 2;
        int timeout = 5;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        List<String> payloads = Arrays.asList(
                new GetuiMessage.PayloadBuilder()
                        .withDescription("getui description 1")
                        .withSound("")
                        .withBadge(1)
                        .withTitle("getui message 1")
                        .withDocId("0A6sYyJ8")
                        .withResourceType(ResourceType.NEWS)
                        .withPushType("2").build(),

                new GetuiMessage.PayloadBuilder()
                        .withDescription("getui description 2")
                        .withSound("")
                        .withBadge(1)
                        .withTitle("getui message 2")
                        .withDocId("l_012205c5075e07870bc010cf10ce")
                        .withResourceType(ResourceType.TOPIC)
                        .withPushType("64").build(),

                new GetuiMessage.PayloadBuilder()
                        .withDescription("getui description 3")
                        .withSound("")
                        .withBadge(1)
                        .withTitle("getui message 3")
                        .withDocId("0A7NXKUT")
                        .withResourceType(ResourceType.NEWS)
                        .withPushType("2").build()
        );
        String token = "GTPP8CBEBEC85544863583028663590";
        //String payload, String token, String appId, String appKey
        List<GetuiMessage> list = new ArrayList<>();
        for (String payload : payloads) {
            list.add(
                    new GetuiMessage.Build()
                            .withPayload(payload)
                            .withToken(token)
                            .withAppId("rX637WLVVc8wlQ8ux6qSP7")
                            .withAppKey("H7dPb6jqaO8h3kVbH2O4V2")
                            .build()
            );
        }
        GetuiPush.push(list, url, batch, retry, config);
    }
}