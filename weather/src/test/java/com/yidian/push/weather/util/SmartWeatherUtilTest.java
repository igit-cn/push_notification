package com.yidian.push.weather.util;

import com.alibaba.fastjson.JSONObject;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 16/1/21.
 */
public class SmartWeatherUtilTest {

    @Test
    public void testGenDocAndGetDocId() throws Exception {

    }

  // @Test
    public void testGenDoc() throws Exception {
        String url =  "http://lc2.haproxy.yidian.com:9100/post/save?action=2&uid=12299265";
        String title = "test-title";
        String content = "test-content";
        System.out.println(SmartWeatherUtil.genDoc(url, title, content, "64485"));

    }

    //@Test
    public void testGetDocId() throws Exception {
        String id = "2985601";
        String url = "http://lc2.haproxy.yidian.com:9100/post/get-post";
        System.out.println(SmartWeatherUtil.getDocId(url, id));
    }
}