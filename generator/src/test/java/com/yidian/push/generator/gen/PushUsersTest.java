package com.yidian.push.generator.gen;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/7/15.
 */
public class PushUsersTest {

    @Test
    public void testSplitUsersIntoSeparateList() throws Exception {
        List<Long> list = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
        System.out.println(PushUsers.splitUsersIntoSeparateList(list, 1));
        System.out.println(PushUsers.splitUsersIntoSeparateList(list, 2));
        System.out.println(PushUsers.splitUsersIntoSeparateList(list, 3));
        System.out.println(PushUsers.splitUsersIntoSeparateList(list, 4));
        System.out.println(PushUsers.splitUsersIntoSeparateList(list, 5));
        System.out.println(PushUsers.splitUsersIntoSeparateList(list, 6));
        System.out.println(PushUsers.splitUsersIntoSeparateList(list, 7));
        System.out.println(PushUsers.splitUsersIntoSeparateList(list, 8));
        System.out.println(PushUsers.splitUsersIntoSeparateList(list, 9));
        System.out.println(PushUsers.splitUsersIntoSeparateList(list, 10));

    }
}