package com.yidian.push.client_pull.utils;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 16/3/22.
 */
public class OuterServiceUtilTest {

    @Test
    public void testGetTitles() throws Exception {
        List<String> docList = Arrays.asList("0ChnK9EJ", "0ChoY8zN");
        String url = "http://a1.go2yd.com/Website/contents/content";
        Map<String, String> docTitle = OuterServiceUtil.getTitles(url, docList);
        System.out.println(docTitle);

    }
}