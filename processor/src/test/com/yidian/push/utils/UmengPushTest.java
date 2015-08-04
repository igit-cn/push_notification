package com.yidian.push.utils;

import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
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

/**
 * Created by tianyuzhi on 15/7/30.
 */
public class UmengPushTest {
    private ProcessorConfig processorConfig;
    @BeforeClass
    public void before() throws IOException {
        String projectDir = System.getProperty("user.dir");
        Config.setCONFIG_FILE(projectDir + "/processor/src/main/resources/config/config.json");
        processorConfig = Config.getInstance().getProcessorConfig();
        UmengPush.init();
    }
    @Test
    public void testPush() {
        String url = processorConfig.getUmengPushBatchUrl();
        int batch = 2;
        int retry = 2;
        int timeout = 5;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        List<String> payloads = Arrays.asList(
                new UmengMessage.PayloadBuilder().withSound("").withAppName("yidian")
                        .withDescription("description").withDocId("0A8uHitj")
                        .withMessageType(MessageType.MESSAGE).withPushType("2")

                        .withResourceType(ResourceType.NEWS).withTitle("title messgae").build(),
                new UmengMessage.PayloadBuilder().withSound("").withAppName("yidian")
                        .withDescription("description topic").withDocId("l_01240169038e016b0ee909ba1502")
                        .withMessageType(MessageType.NOTIFICATION).withPushType("64")
                        .withResourceType(ResourceType.TOPIC).withTitle("title topic notification").build(),
                new UmengMessage.PayloadBuilder().withSound("").withAppName("yidian")
                        .withDescription("description 3").withDocId("0A9PYF2N")
                        .withMessageType(MessageType.MESSAGE).withPushType("2")
                        .withResourceType(ResourceType.NEWS).withTitle("title 3").build()
        );
        String token = "UMPP8CBEBEC85544863583028663590";
        String policy = new UmengMessage.PolicyBuilder().withExpireTimeInSeconds(3600).build();
        List<UmengMessage> list = new ArrayList<>();
        for (String payload : payloads) {
            list.add(
                    new UmengMessage.Build().withAlias(token)
                            .withAliasType("yidian").withAppId("yidian")
                            .withThirdPartyId("test-from-yidian")
                            .withPayload(payload).withPolicy(policy).build()
            );
        }
        UmengPush.push(list, url, batch, retry, config);

    }
}