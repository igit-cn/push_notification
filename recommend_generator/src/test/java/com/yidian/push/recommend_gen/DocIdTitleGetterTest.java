package com.yidian.push.recommend_gen;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/9/15.
 */
public class DocIdTitleGetterTest {
    private String url = "http://a1.go2yd.com/Website/contents/content";

    @Test
    public void testGetTitles() {
        List<String> docIds = Arrays.asList("0Afk0MMd", "0AfqdI7x", "0AfkH7VB");
        Map<String, String> res = DocIdTitleGetter.getTitles(url, 1, docIds);
        System.out.println(res);
    }

}