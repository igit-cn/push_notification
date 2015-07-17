package com.yidian.push.data;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/7/17.
 */
public class PushTypeTest {

    @Test
    public void testGetPushType() throws Exception {
        assert PushType.BREAK == PushType.getPushType(PushType.BREAK.getInt());
        assert PushType.UNKNOWN == PushType.getPushType(-1);
    }
}