package com.yidian.push.generator.request;

import com.yidian.push.data.Platform;
import org.testng.annotations.Test;

import java.util.Arrays;

public class RequestContentTest {

    @Test
    public void testBuildRequestContent() throws Exception {
        String str ="{\"_id\":\"07fmJsb1\",\"platform\":[\"iPhone\",\"android\"],\"hash\":\"\",\"docid\":\"07fmJsb1\",\"description\":\"\\u56db\\u5ddd\\u4e00\\u7edf\\u8ba1\\u5c40\\u526f\\u5c40\\u957f\\u6b7b\\u5728\\u6c11\\u653f\\u5c40\\u526f\\u5c40\\u957f\\u5bb6\\u4e2d\\uff0c\\u7f51\\u53cb\\u79f0\\u4e24\\u4eba\\u66fe\\u6253\\u6597\\uff0c\\u73b0\\u573a\\u548c\\u6b7b\\u8005\\u8eab\\u4e0a\\u6709\\u5927\\u91cf\\u8840\\u8ff9\\uff0c\\u5b98\\u65b9:\\u65e0\\u6253\\u6597\\u7cfb\\u9ad8\\u8840\\u538b\\u7a81\\u53d1\\u81f4\\u6b7b>>\\u8be6\\u7ec6\",\"date\":\"2015-01-13 21:16:14\",\"newsChannel\":\"\",\"createTime\":\"2015-01-13 21:16:14\",\"userids\":[\"all\"],\"newsType\":\"e\",\"operator\":\"zhangxin\",\"key\":\"4e15f3b1ef28ee1db4611ccb96272c6c\"}";
        RequestContent requestContent = RequestContent.buildRequestContent(str);
        assert "07fmJsb1".equals(requestContent.getDocId());
        Platform[] plats = {Platform.IPHONE, Platform.ANDROID};
        assert Arrays.asList(plats).equals(requestContent.getPlatform());

        str = "{\"_id\":\"08OqC6h9\",\"platform\":[\"iPhone\",\"android\"],\"hash\":\"\",\"docid\":\"08OqC6h9\",\"description\":\"\\u56fd\\u5bb6\\u7edf\\u8ba1\\u5c40\\u6d88\\u606f\\uff0c2\\u6708\\u4efdcpi\\u6da8\\u5e45\\u56de\\u5347\\u81f31.4%\\uff0cppi\\u540c\\u6bd4\\u964d4.8%>>\\u8be6\\u7ec6\",\"date\":\"2015-03-10 09:43:23\",\"channel\":\"u703,sc24\",\"createTime\":\"2015-03-10 09:43:23\",\"userids\":[\"auto\"],\"type\":\"e\",\"operator\":\"xunjianguo\",\"key\":\"fe833ec4e9f216757e03af75201f2b5f\"}";
        RequestContent requestContent1 = RequestContent.buildRequestContent(str);
        assert "国家统计局消息，2月份cpi涨幅回升至1.4%，ppi同比降4.8%>>详细".equals(requestContent1.getDescription());


    }
}