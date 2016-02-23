package com.yidian.push.instant.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

/**
 * Created by tianyuzhi on 16/2/23.
 */
public class TestDate {
    public static void main(String[] args) {
        Date date = new Date();
        //date = null;
        String utc = new DateTime(date, DateTimeZone.UTC).toString("yyyy-MM-dd HH:mm:ss");
        System.out.println(date);
        System.out.println(utc);

    }
}
