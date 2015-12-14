package com.yidian.push.recommend_gen;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/12/9.
 */
public class OnlineGeneratorTest {

    @Test
    public void testGetPushId() throws Exception {
        String url = "http://10.101.1.220/id/get-push-id";
        Map<String, Object> params = new HashMap<>();
        params.put("type", "realtime");
        params.put("push_tag", "test" + "-main");
        params.put("day", "2015-11-07");
        String pid = OnlineGenerator.getPushId(url, params);
        System.out.println(pid);

    }
}