package com.yidian.push.weather.util;


import org.testng.annotations.Test;

/**
 * Created by tianyuzhi on 16/1/19.
 */
public class AreaUtilTest {

    @Test
    public void testNormalize() throws Exception {
        String[] areas = {"吉林", "重庆省", "北京市", "北京", "潜江市","嘉峪关市","吉林市","白银市","百色市","北海市","朝阳市","海南市"};
        for (String area : areas) {
            System.out.println(area + " : " + AreaUtil.normalize(area));
        }
    }
}