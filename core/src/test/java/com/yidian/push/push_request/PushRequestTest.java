package com.yidian.push.push_request;

import com.google.gson.Gson;
import com.yidian.push.data.Platform;
import com.yidian.push.utils.GsonFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/8/1.
 */
public class PushRequestTest {

    @Test
    public void testGetPushRequestStatus() throws Exception {

        String fileName = "/ssd/data/push_notification/test_request/ready/20150801171524-10_111_0_70-3306-PUSH-16.0a563ae1_0f28_4bbf_a63e_611f00be515e.offset.0.job";
        PushRequest pushRequest = new PushRequest(fileName);
        System.out.println(GsonFactory.getNonPrettyGson().toJson(pushRequest));
        assert Platform.isIPhone(pushRequest.getTable());
        assert PushRequestStatus.READY == pushRequest.getPushRequestStatus();
    }
}