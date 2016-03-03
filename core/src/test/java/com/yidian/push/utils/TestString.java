package com.yidian.push.utils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.testng.annotations.Test;

/**
 * Created by tianyuzhi on 16/3/2.
 */
public class TestString {
    @Test
    public void test() {
        String str = "{\"1\":\"2\", \"3\":\"\na\tb\n\"}";
        String strB = StringEscapeUtils.escapeJson(str);
        String strC = strB.replaceAll("\\t", "\\\\t");
        System.out.println(str + " " + strB + " " + strC);

    }
}
