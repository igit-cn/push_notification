package com.yidian.push.generator.gen;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/6/30.
 */
public class SqlUtilTest {

    @Test
    public void testGenQuotedStringList() throws Exception {
        List<String> list = Arrays.asList("1", "2", "3");
        System.out.println(SqlUtil.genQuotedStringList(list));
    }
}