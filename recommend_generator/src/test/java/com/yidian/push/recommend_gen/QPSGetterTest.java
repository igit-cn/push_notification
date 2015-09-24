package com.yidian.push.recommend_gen;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/9/14.
 */
public class QPSGetterTest {

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
        String str = "1,2,";
        for (int i = -1; i < 6; i ++) {
            System.out.println(i + " " + Arrays.asList(str.split(",", i)));
        }
    }

}