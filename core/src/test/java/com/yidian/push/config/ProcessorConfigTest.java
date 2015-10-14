package com.yidian.push.config;

import com.yidian.push.data.PushType;
import org.testng.annotations.Test;

import java.util.HashSet;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/10/14.
 */
public class ProcessorConfigTest {

    @Test
    public void testShouldUseRecommendPush() {
        ProcessorConfig config = new ProcessorConfig();
        HashSet<PushType> set = new HashSet<>();
        set.add(PushType.LOCAL);
        set.add(PushType.RECOMMEND);
        set.add(PushType.RECOMMEND_1);
        set.add(PushType.RECOMMEND_2);
        set.add(PushType.RECOMMEND_3);
        config.setAndroidUseRecommendPushTypes(set);
        assert config.shouldUseRecommendPush(PushType.LOCAL);
        assert config.shouldUseRecommendPush(PushType.RECOMMEND);
        assert !config.shouldUseRecommendPush(PushType.BREAK);

        config.setAndroidUseRecommendPushTypes(null);
        assert !config.shouldUseRecommendPush(PushType.LOCAL);
        assert !config.shouldUseRecommendPush(PushType.RECOMMEND);
        assert !config.shouldUseRecommendPush(PushType.BREAK);
    }
}