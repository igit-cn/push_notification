package com.yidian.push.generator.util;

import com.yidian.push.generator.cache.CacheUtil;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Created by tianyuzhi on 15/7/9.
 */
public class CacheUtilTest {

    @Test
    public void testGetUserSInFile() throws Exception {
        String file = "/Users/tianyuzhi/t973";
        long startTime = System.currentTimeMillis();
        List<Long> longs = CacheUtil.getUserSInFile(file);
        System.out.println("cost time is seconds: " + (System.currentTimeMillis() - startTime)/ 1000.0);
        for (int i = 0; i < 3 && i < longs.size(); i ++) {
            System.out.println(longs.get(i));
        }
    }

    @Test
    public void testGetUserSInFile2() throws Exception {
        String file = "/Users/tianyuzhi/t973";
        long startTime = System.currentTimeMillis();
        List<Long> longs = CacheUtil.getUserSInFile2(file);
        System.out.println("cost time is seconds: " + (System.currentTimeMillis() - startTime)/ 1000.0);
        for (int i = 0; i < 3 && i < longs.size(); i ++) {
            System.out.println(longs.get(i));
        }
    }
}