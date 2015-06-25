package com.yidian.push.generator.gen;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/6/24.
 */
public class GenerateRequestFileTest {

    @Test
    public void testGenRequestFileName() throws Exception {
        System.out.println(GenerateRequestFile.genRequestFileName("10.111.2.52", 3306, "PUSH_FOR_ANDROID", "2"));
        System.out.println(GenerateRequestFile.genRequestFileName("10.111.2.52", 3306, "PUSH", "2"));
    }
}