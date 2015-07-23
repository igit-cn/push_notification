package com.yidian.push.generator.gen;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/7/22.
 */
public class GetInactiveUsersTest {

    //@Test
    public void testGetUsersFromFile() throws Exception {
        long startTime = System.currentTimeMillis();
        GetInactiveUsers.getUsersFromFile("/Users/tianyuzhi/inactive_users.2015-07-22");
        System.out.println("1 cost time is seconds: " + (System.currentTimeMillis() - startTime)/ 1000.0);
        startTime = System.currentTimeMillis();
        GetInactiveUsers.getUsersFromFile2("/Users/tianyuzhi/inactive_users.2015-07-22");
        System.out.println("2 cost time is seconds: " + (System.currentTimeMillis() - startTime)/ 1000.0);


    }
}