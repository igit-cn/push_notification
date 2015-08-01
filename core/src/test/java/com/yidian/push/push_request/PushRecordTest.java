package com.yidian.push.push_request;

import org.testng.annotations.Test;

/**
 * Created by tianyuzhi on 15/7/27.
 */
public class PushRecordTest {

    @Test
    public void test() {
        //57134772^AMMPP3C438E8E6EE9990001194038915^C0^Ayddk^A0A770Hwn^A喝酒绝对不能点的4样菜你知道吗？>>详细^A16^A^A0^A2^A^A0
        PushRecord pushRecord = new PushRecord.Builder().setAppId("appid")
                .setDocId("docid").setPushChannel(null).setDescription("desc").setNewsChannel("u539").setNewsType(1).build();
        String strRecord = pushRecord.toString();
        PushRecord pushRecord1 = new PushRecord(strRecord);
        assert "appid".equals(pushRecord1.getAppId());
        assert "desc".equals(pushRecord1.getDescription());
        assert null == pushRecord1.getPushChannel();

    }

}