package com.yidian.push.generator.gen;

import com.yidian.push.data.Platform;
import com.yidian.push.generator.data.AppId;
import com.yidian.push.push_request.PushRecord;
import com.yidian.push.push_request.PushRequest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/6/24.
 */
public class GenerateRequestFileTest {

    @Test
    public void testGenRequestFileName() throws Exception {
        System.out.println(GenerateRequestFile.genRequestFileName("10.111.2.52", 3306, "PUSH_FOR_ANDROID", "2"));
        System.out.println(GenerateRequestFile.genRequestFileName("10.111.2.52", 3306, "PUSH", "2"));
    }

    @Test
    public void testGetNid() {
//        if (Platform.isAndroid(table) && AppId.XIAOMI.equals(pushRecord.getAppId())) {
//            return (curNid + 1) % xiaomiMaxNotificationNumber;
//        }
//        else {
//            return (curNid + 1) % 5;
//        }
        PushRecord record = new PushRecord.Builder().setAppId("xiaomi").build();
        assert 1 == GenerateRequestFile.getNid(Platform.PUSH_FOR_ANDROID, 0, record);
        assert 0 == GenerateRequestFile.getNid(Platform.PUSH_FOR_ANDROID, 1, record);
        GenerateRequestFile.setXiaomiMaxNotificationNumber(3);
        assert 1 == GenerateRequestFile.getNid(Platform.PUSH_FOR_ANDROID, 0, record);
        assert 2 == GenerateRequestFile.getNid(Platform.PUSH_FOR_ANDROID, 1, record);
        assert 0 == GenerateRequestFile.getNid(Platform.PUSH_FOR_ANDROID, 2, record);
        GenerateRequestFile.setXiaomiMaxNotificationNumber(2);
        assert 1 == GenerateRequestFile.getNid(Platform.PUSH_FOR_ANDROID, 0, record);
        assert 0 == GenerateRequestFile.getNid(Platform.PUSH_FOR_ANDROID, 1, record);


    }
}