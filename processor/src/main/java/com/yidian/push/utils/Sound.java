package com.yidian.push.utils;

import org.joda.time.DateTime;

/**
 * Created by tianyuzhi on 15/7/27.
 */
public class Sound {
    public static int startTime = 7 * 60;
    public static int endTime = 24 * 60 - 1;
    private static String IOS_SOUND = "1.caf";

    public static String getIOSNotifySound() {
        int secondOfDay = DateTime.now().getSecondOfDay();
        if (startTime <= secondOfDay
                && secondOfDay < endTime) {
            return IOS_SOUND;
        }
        else {
            return null;
        }
    }

    public static String getUmengNotifySound() {
        int secondOfDay = DateTime.now().getSecondOfDay();
        if (startTime <= secondOfDay
                && secondOfDay < endTime) {
            return "";
        }
        else {
            return "no.sound";
        }
    }
}
