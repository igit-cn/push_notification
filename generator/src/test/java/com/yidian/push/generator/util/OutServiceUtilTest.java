package com.yidian.push.generator.util;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/7/13.
 */
public class OutServiceUtilTest {

    @Test
    public void testGetRelatedChannels() throws Exception {
        String docId = "09vgG9RE";
        List<String> docIdList = OutServiceUtil.getRelatedChannels(docId);
        System.out.println("docid [" + docId + "] has related channels : " + docIdList);
    }
}