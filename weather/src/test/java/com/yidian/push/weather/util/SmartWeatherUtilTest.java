package com.yidian.push.weather.util;

import org.testng.annotations.Test;

/**
 * Created by tianyuzhi on 16/1/21.
 */
public class SmartWeatherUtilTest {
    // ignore the test , for it will generate the doc online .

    //@Test
    // when run, generated doc will be like : http://www.yidianzixun.com/0CE5ScrP
    public void testGenDocAndGetDocId() throws Exception {
        String url = "http://10.101.0.150:9999/service/inject_self_news";
        String title = "test_title测试标题";
        String content = "test_content测试内容";
        String date = "1454050109000"; // in micro-seconds
        String uid = "tianzy";
        String source = "中国气象局";
        String srcUrl = "http://openweather.weather.com.cn/Home/Help/Product.html";

        String docId = SmartWeatherUtil.genDocAndGetDocId(url, title, content, date, uid, source, srcUrl);
        System.out.println(docId);
    }

   //@Test
    public void testGenDoc() throws Exception {
        String url =  "http://10.111.0.153:9901/post/save?action=2&uid=12299265";
        String title = "test-title中文标题";
        String content = "test-content中文内容";
        System.out.println(SmartWeatherUtil.genDoc(url, title, content, "64485"));

    }

   // @Test
    public void testGetDocId() throws Exception {
        String id = "86";
        String url = "http://10.111.0.153:9901/post/get-post";
        System.out.println(SmartWeatherUtil.getDocId(url, id));
    }

    @Test
    public void testGenDocAndGetDocIdThroughMedia() throws Exception {

    }
}