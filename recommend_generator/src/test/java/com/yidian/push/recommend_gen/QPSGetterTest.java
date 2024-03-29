package com.yidian.push.recommend_gen;

import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/9/14.
 */
public class QPSGetterTest {

    @Test
    public void getHostName() throws UnknownHostException {
        InetAddress address = InetAddress.getLocalHost();
        System.out.println(address.getHostName()); // 输出本机名

    }


    @Test
    public void testRefresh() throws IOException, InterruptedException {
        String url = "http://dataplatform.yidian.com:4242/api/query?start=3m-ago&m=sum:prediction.default.qps.m1";
        QPSGetter qpsGetter = new QPSGetter(url);
        for (int i = 0; i < 5; i ++) {
            qpsGetter.refresh();
            System.out.println("qps : " + qpsGetter.getQps());
            Thread.sleep(1 * 1000);
        }

    }

    @Test
    public void testSplit() {
        String str = "54875700,yaoshan,Android,H880+ Eight core,MTK6592";
        for (int i = -1; i < 6; i ++) {
            System.out.println(i + " " + Arrays.asList(str.split(",", i)));
        }
    }

    @Test
    public void testStringLength() {
        String str = "中文1，,";
        System.out.println(str.length());
    }

}