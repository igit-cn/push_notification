package com.yidian.push.weather.data;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 16/3/2.
 */
public class SoundTest {

    @Test
    public void test() {
        System.out.println(Sound.getSound("0"));
        System.out.println(Sound.getSound("1"));
    }
}