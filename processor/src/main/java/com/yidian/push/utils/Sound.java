package com.yidian.push.utils;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

/**
 * Created by tianyuzhi on 15/7/27.
 */
public class Sound {
    public static int startTime = 7 * 60 * 60;
    public static int endTime = 24 * 60 * 60 - 1;
    private static String IOS_SOUND = "1.caf";
    private static String NO_SOUND = "no.sound";

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
            return NO_SOUND;
        }
    }

    // TODO : combine the sound and notify type into one.

    public static String getXiaomiNotifySound() {
        int secondOfDay = DateTime.now().getSecondOfDay();
        if (startTime <= secondOfDay
                && secondOfDay < endTime) {
            return "";
        }
        else {
            return NO_SOUND;
        }
    }

    public static String getXiaomiNotifySound(String notifyType) {
        if (Boolean.TRUE.toString().equals(notifyType)) {
            return "";
        }
        else {
            return NO_SOUND;
        }
    }

    public static String getXiaomiNotifyType() {
        int secondOfDay = DateTime.now().getSecondOfDay();
        if (startTime <= secondOfDay
                && secondOfDay < endTime) {
            return "1";
        }
        else {
            return "0";
        }
    }

    public static String getXiaomiNotifyType(String sound) {
        if (StringUtils.isEmpty(sound)) {
            return "1";
        }
        else {
            return "0";
        }
    }




}
