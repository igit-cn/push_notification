package com.yidian.push.data;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class PlatformTest {

    @Test
    public void testToString() throws Exception {
        System.out.println(Platform.ANDROID);
        System.out.println(Platform.IPHONE);

    }
}