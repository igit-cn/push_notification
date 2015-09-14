package com.yidian.push.utils;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by tianyuzhi on 15/9/12.
 */
public class HttpConnectionUtilsTest extends TestCase {

    public void testGetPostContent() throws Exception {
        String url = "http://dataplatform.yidian.com:4242/api/query?start=3m-ago&m=sum:prediction.default.qps.m1";
       // url = "http://www.baidu.com";
        System.out.println(HttpConnectionUtils.getGetResult(url, null));

        url = "http://10.111.0.108:9080/push_service/getLocalChannel";
        Map<String, Object> params = new HashMap<>();
        params.put("ip", "219.147.36.6");
        System.out.println("get: " + HttpConnectionUtils.getGetResult(url, params).trim());
        System.out.println("post: " + HttpConnectionUtils.getPostResult(url, params).trim());

    }
}