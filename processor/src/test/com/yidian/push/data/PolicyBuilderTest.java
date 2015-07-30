package com.yidian.push.data;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/7/30.
 */
public class PolicyBuilderTest {
    @Test
    public void test() {
        String policy = new UmengMessage.PolicyBuilder().withExpireTimeInMinutes(3 * 60 * 60).build();
        System.out.println(policy);
    }

}