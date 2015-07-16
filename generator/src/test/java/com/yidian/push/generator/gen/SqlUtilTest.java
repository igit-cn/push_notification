package com.yidian.push.generator.gen;

import com.yidian.push.generator.util.SqlUtil;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by tianyuzhi on 15/6/30.
 */
public class SqlUtilTest {

    @Test
    public void testGenQuotedStringList() throws Exception {
        List<String> list = Arrays.asList("1", "2", "3");
        System.out.println(SqlUtil.genQuotedStringList(list));
    }

    @Test
    public void testGenQuotedLongList() {
        List<Long> list = Arrays.asList(1L, 2L, 3L);
        System.out.println(SqlUtil.genQuotedLongList(list));
    }
}