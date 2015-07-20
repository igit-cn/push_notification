package com.yidian.push.generator;

import com.yidian.push.config.Config;
import com.yidian.push.data.Platform;
import com.yidian.push.generator.gen.RedisConnectionPool;
import com.yidian.push.generator.util.PushRequestRedisUtil;
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
        String projectDir = System.getProperty("user.dir");
        Config.setCONFIG_FILE(projectDir + "/src/main/resources/config/config.json");
        RedisConnectionPool.init();
    }

    @AfterClass
    public void after(){
        RedisConnectionPool.close();
    }

    @Test
    public void testGenNidLastPushTimeKey() throws Exception {
        PushRecord pushRecord = new PushRecord.Builder().setUid(123).setAppId("testApp").build();
        String table = Platform.IPHONE.getTable();
        String key = PushRequestRedisUtil.genNidLastPushTimeKey(pushRecord, Platform.getTableId(table));
        System.out.println(key);

    }

    @Test
    public void testGenNidAndLastPushTimeKeys() throws Exception {
        PushRecord pushRecord = new PushRecord.Builder().setUid(123).setAppId("testApp").build();
        PushRecord pushRecord2 = new PushRecord.Builder().setUid(123).setAppId("testApp").build();
        String table = Platform.ANDROID.getTable();


        List<PushRecord> pushRecordList = Arrays.asList(pushRecord, pushRecord2);
        System.out.println(PushRequestRedisUtil.genNidAndLastPushTimeKeys(pushRecordList, Platform.getTableId(table)));

    }

    @Test
    public void testGenPushChannelKey() throws Exception {
        PushRecord pushRecord = new PushRecord.Builder().setUid(123).setAppId("testApp").build();
        String table = Platform.ANDROID.getTable();
        System.out.println(PushRequestRedisUtil.genPushChannelKey(pushRecord, Platform.getTableId(table)));
    }
    @Test
    public void testGenPushChannelKeys() throws Exception {
        PushRecord pushRecord = new PushRecord.Builder().setUid(123).setAppId("testApp").build();
        PushRecord pushRecord2 = new PushRecord.Builder().setUid(124).setAppId("testApp").build();
        String table = Platform.ANDROID.getTable();

        List<PushRecord> pushRecordList = Arrays.asList(pushRecord, pushRecord2);
        System.out.println(PushRequestRedisUtil.genPushChannelKeys(pushRecordList, Platform.getTableId(table)));
    }


    @Test
    public void testGetNidAndLastPushTime() throws Exception {
        PushRecord pushRecord = new PushRecord.Builder().setUid(77).setAppId("yidian").build();
        PushRecord pushRecord2 = new PushRecord.Builder().setUid(65).setAppId("yidian").build();
        int redisId = 0;
        String table = Platform.IPHONE.getTable();
        List<PushRecord> pushRecordList = Arrays.asList(pushRecord, pushRecord2);

        System.out.println(PushRequestRedisUtil.getNidAndLastPushTime(redisId, table, pushRecordList));
    }


    @Test
    public void testGetPushChannel() throws Exception {
        PushRecord pushRecord = new PushRecord.Builder().setUid(77).setAppId("yidian").build();
        PushRecord pushRecord2 = new PushRecord.Builder().setUid(65).setAppId("yidian").build();
        int redisId = 0;
        String table = Platform.IPHONE.getTable();
        List<PushRecord> pushRecordList = Arrays.asList(pushRecord, pushRecord2);

        System.out.println(PushRequestRedisUtil.getPushChannel(redisId, table, pushRecordList));
    }
}