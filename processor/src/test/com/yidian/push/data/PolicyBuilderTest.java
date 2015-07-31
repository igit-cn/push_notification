package com.yidian.push.data;

import org.testng.annotations.Test;

/**
 * Created by tianyuzhi on 15/7/30.
 */
public class PolicyBuilderTest {
    @Test
    public void test() {
        String policy = new UmengMessage.PolicyBuilder().withExpireTimeInSeconds(3 * 60 * 60).build();
        System.out.println(policy);
    }

}