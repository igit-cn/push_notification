package com.yidian.push.utils;

import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.PushChannel;
import com.yidian.push.push_request.PushRecord;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/8/1.
 */
public class FilterTokenTest {
    @BeforeClass
    public void before() throws IOException {
        String projectDir = System.getProperty("user.dir");
        Config.setCONFIG_FILE(projectDir + "/processor/src/main/resources/config/config.json");
    }

    private String genToken(String alias, int pushLevel) {
        return alias + PushRecord.TOKEN_ITEM_SEPARATOR + pushLevel;
    }

    @Test
    public void test() throws IOException {
        List<String> tokenList = Arrays.asList(
                genToken("MMPP123", 1), genToken("UMPP123", 0), genToken("GTPP123", 1),
                genToken("UMPP334", 0), genToken("MMPP334", 0), genToken("GTPP334", 0),
                genToken("UMPP4567", 0), genToken("GTPP4567", 1),
                genToken("UMPP6789", 1), genToken("GTPP6789", 1),
                genToken("UMPP7890", 1)
        );
        String appId = "yidian";
        System.out.println(
                GsonFactory.getPrettyGson().toJson(FilterToken.filterTokens(tokenList, appId, PushChannel.IOS, false))
        );
        System.out.println(
                GsonFactory.getPrettyGson().toJson(FilterToken.filterTokens(tokenList, appId, PushChannel.UMENG, true))
               );
        System.out.println(
                GsonFactory.getPrettyGson().toJson(FilterToken.filterTokens(tokenList, appId, PushChannel.XIAOMI, true))
                );

        appId = "xiaomi";
        System.out.println(
                GsonFactory.getPrettyGson().toJson(FilterToken.filterTokens(tokenList, appId, PushChannel.IOS, false))
        );
        System.out.println(
                GsonFactory.getPrettyGson().toJson(FilterToken.filterTokens(tokenList, appId, PushChannel.UMENG, true))
        );
        System.out.println(
                GsonFactory.getPrettyGson().toJson(FilterToken.filterTokens(tokenList, appId, PushChannel.XIAOMI, true))
        );
    }

}