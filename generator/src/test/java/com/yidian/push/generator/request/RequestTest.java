package com.yidian.push.generator.request;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RequestTest {

    @Test
    public void testParseFileName() throws Exception {
        String fileName = RequestStatus.READY.toString() + "/" + 123456789;
        assert RequestStatus.READY == Request.parseFileName(fileName);
    }
}