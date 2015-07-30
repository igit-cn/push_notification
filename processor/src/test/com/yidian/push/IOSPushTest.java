package com.yidian.push;

import com.yidian.push.data.APNSMessage;
import com.yidian.push.utils.APNS;
import org.apache.http.client.config.RequestConfig;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by tianyuzhi on 15/7/29.
 */
public class IOSPushTest {

    @Test
    public void testPush() throws Exception {
        String token = "5ebdd79fc85f50698e057fc4862f3c36419eb6d60bf0579a2251d19a24017929";
        //List<APNSMessage> payloads, String url, int batch, int retry, RequestConfig requestConfig
        List<APNSMessage> payloadList = Arrays.asList(
                new APNSMessage.Build().withParam("rid", "0A8uHitj").withParam("rtype", "news").withAppId("new-yidian").withAlert("test 1 ios push").withBadge(1).withSound("1.caf").withToken(token).build(),
                new APNSMessage.Build().withParam("rid", "0A8uHitj").withParam("rtype", "news").withAppId("new-yidian").withAlert("test 2 ios push").withBadge(1).withSound("1.caf").withToken(token).build(),
                new APNSMessage.Build().withParam("rid", "0A8uHitj").withParam("rtype", "news").withAppId("new-yidian").withAlert("test 3 ios push").withBadge(1).withSound(null).withToken(token).build()
        );
        String url = "http://10.111.0.57:5266/push_service/apns_multiple/";
        int batch = 2;
        int retry = 2;
        int timeout = 5;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
        APNS.push(payloadList, url, batch, retry, config);

    }
}