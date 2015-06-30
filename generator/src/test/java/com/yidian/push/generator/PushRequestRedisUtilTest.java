package com.yidian.push.generator;

import com.yidian.push.config.Config;
import com.yidian.push.push_request.PushRecord;
import org.testng.annotations.*;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tianyuzhi on 15/6/29.
 */
public class PushRequestRedisUtilTest {

    @BeforeClass
    public void before() throws IOException {
        Config.setCONFIG_FILE("generator/src/main/resources/config/config.json");
        RedisConnectionPool.init();
    }

    @AfterClass
    public void after(){
        RedisConnectionPool.close();
    }

    @Test
    public void testGenNidLastPushTimeKey() throws Exception {
        PushRecord pushRecord = new PushRecord.Builder().setUid(123).setAppId("testApp").build();
        String table = Table.PUSH;
        String key = PushRequestRedisUtil.genNidLastPushTimeKey(pushRecord, Table.getTableId(table));
        System.out.println(key);

    }

    @Test
    public void testGenNidAndLastPushTimeKeys() throws Exception {
        PushRecord pushRecord = new PushRecord.Builder().setUid(123).setAppId("testApp").build();
        PushRecord pushRecord2 = new PushRecord.Builder().setUid(123).setAppId("testApp").build();
        String table = Table.PUSH_FOR_ANDROID;


        List<PushRecord> pushRecordList = Arrays.asList(pushRecord, pushRecord2);
        System.out.println(PushRequestRedisUtil.genNidAndLastPushTimeKeys(pushRecordList, Table.getTableId(table)));

    }

    @Test
    public void testGenPushChannelKey() throws Exception {
        PushRecord pushRecord = new PushRecord.Builder().setUid(123).setAppId("testApp").build();
        String table = Table.PUSH_FOR_ANDROID;
        System.out.println(PushRequestRedisUtil.genPushChannelKey(pushRecord, Table.getTableId(table)));
    }
    @Test
    public void testGenPushChannelKeys() throws Exception {
        PushRecord pushRecord = new PushRecord.Builder().setUid(123).setAppId("testApp").build();
        PushRecord pushRecord2 = new PushRecord.Builder().setUid(124).setAppId("testApp").build();
        String table = Table.PUSH_FOR_ANDROID;

        List<PushRecord> pushRecordList = Arrays.asList(pushRecord, pushRecord2);
        System.out.println(PushRequestRedisUtil.genPushChannelKeys(pushRecordList, Table.getTableId(table)));
    }


    @Test
    public void testGetNidAndLastPushTime() throws Exception {
        PushRecord pushRecord = new PushRecord.Builder().setUid(77).setAppId("yidian").build();
        PushRecord pushRecord2 = new PushRecord.Builder().setUid(65).setAppId("yidian").build();
        int redisId = 1;
        String table = Table.PUSH;
        List<PushRecord> pushRecordList = Arrays.asList(pushRecord, pushRecord2);

        System.out.println(PushRequestRedisUtil.getNidAndLastPushTime(redisId, table, pushRecordList));
    }


    @Test
    public void testGetPushChannel() throws Exception {
        PushRecord pushRecord = new PushRecord.Builder().setUid(77).setAppId("yidian").build();
        PushRecord pushRecord2 = new PushRecord.Builder().setUid(65).setAppId("yidian").build();
        int redisId = 1;
        String table = Table.PUSH;
        List<PushRecord> pushRecordList = Arrays.asList(pushRecord, pushRecord2);

        System.out.println(PushRequestRedisUtil.getPushChannel(redisId, table, pushRecordList));
    }
}