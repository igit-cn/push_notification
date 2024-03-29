package com.yidian.push.generator.util;

import com.yidian.push.generator.cache.CacheUtil;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;

/**
 * Created by tianyuzhi on 15/7/9.
 */
public class CacheUtilTest {

    //@Test
    public void testGetUserSInFile() throws Exception {
        String file = "/Users/tianyuzhi/u539";
        long startTime = System.currentTimeMillis();
        List<Long> longs = CacheUtil.getUserSInFile(file);
        System.out.println("cost time is seconds: " + (System.currentTimeMillis() - startTime)/ 1000.0);
        System.out.println();
        FileWriter fileWriter = new FileWriter(new File("/Users/tianyuzhi/u539.users"));
        for (Long uid : longs) {
            fileWriter.write(uid + "\n");
        }

        fileWriter.close();
        for (int i = 0; i < 3 && i < longs.size(); i ++) {
            System.out.println(longs.get(i));
        }
    }

    //@Test
    public void testGetUserSInFile2() throws Exception {
        String file = "/Users/tianyuzhi/t973";
        long startTime = System.currentTimeMillis();
        List<Long> longs = CacheUtil.getUserSInFile2(file);
        System.out.println("cost time is seconds: " + (System.currentTimeMillis() - startTime) / 1000.0);
        for (int i = 0; i < 3 && i < longs.size(); i ++) {
            System.out.println(longs.get(i));
        }
    }

    @Test
    public void testGetEmptyFile() throws Exception {
        String file = "/tmp/push_notification/cache/a/b";
        List<Long> longs = CacheUtil.getUserSInFile2(file);
        List<Long> longList = CacheUtil.getUserSInFile(file);
    }

}