package com.yidian.push.data;

import com.google.gson.Gson;
import com.yidian.push.utils.GsonFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/7/21.
 */
public class EnvironmentTest {
    @Test
    public void test() {
        Environment environment = Environment.TEST;
        System.out.println(Environment.TEST);
        System.out.println(Environment.RRODUCTION);
        System.out.println(GsonFactory.getNonPrettyGson().fromJson("test1", Environment.class));
        System.out.println(GsonFactory.getNonPrettyGson().fromJson("pr1", Environment.class));

        assert Environment.TEST == GsonFactory.getNonPrettyGson().fromJson("test", Environment.class);
        assert Environment.RRODUCTION == GsonFactory.getNonPrettyGson().fromJson("production", Environment.class);
    }

}