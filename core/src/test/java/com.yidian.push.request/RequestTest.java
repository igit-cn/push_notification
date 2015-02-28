package com.yidian.push.request;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RequestTest {

    @Test
    public void testBuildRequest() {
        String str ="{\"_id\":\"07fmJsb1\",\"platform\":[\"iPhone\",\"android\"],\"hash\":\"\",\"docid\":\"07fmJsb1\",\"title\":\"\\u56db\\u5ddd\\u4e00\\u7edf\\u8ba1\\u5c40\\u526f\\u5c40\\u957f\\u6b7b\\u5728\\u6c11\\u653f\\u5c40\\u526f\\u5c40\\u957f\\u5bb6\\u4e2d\\uff0c\\u7f51\\u53cb\\u79f0\\u4e24\\u4eba\\u66fe\\u6253\\u6597\\uff0c\\u73b0\\u573a\\u548c\\u6b7b\\u8005\\u8eab\\u4e0a\\u6709\\u5927\\u91cf\\u8840\\u8ff9\\uff0c\\u5b98\\u65b9:\\u65e0\\u6253\\u6597\\u7cfb\\u9ad8\\u8840\\u538b\\u7a81\\u53d1\\u81f4\\u6b7b>>\\u8be6\\u7ec6\",\"date\":\"2015-01-13 21:16:14\",\"pushChannel\":\"\",\"createTime\":\"2015-01-13 21:16:14\",\"userids\":[\"all\"],\"pushType\":\"e\",\"operator\":\"zhangxin\",\"key\":\"4e15f3b1ef28ee1db4611ccb96272c6c\"}";
        Request request = Request.buildRequest(str);
        assert "07fmJsb1".equals(request.getId());
    }


}