package com.yidian.push.generator.gen;

import com.yidian.push.generator.data.Range;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/7/10.
 */
public class RefreshTokensTest {

    @Test
    public void testGenRanges() throws Exception {
        List<Range> ranges = RefreshTokens.genRanges(0, 5, 2);
        System.out.println(ranges);

        System.out.println(RefreshTokens.genRanges(5, -1, 2));

    }
}