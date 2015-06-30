package com.yidian.push.generator;

import com.yidian.push.utils.DateUtil;
import org.joda.time.DateTime;

/**
 * Created by yidianadmin on 15-3-4.
 */
public class Test {
    public static void main(String[] args)
    {
        DateTime dateTime = new DateTime();
        System.out.println(dateTime);
        System.out.println(dateTime.getHourOfDay());
        System.out.println(dateTime.getMinuteOfHour());
        System.out.println(dateTime.getMinuteOfDay());
        System.out.println(DateUtil.getMinOfDay(dateTime));
        System.out.println(dateTime.toString("yyyy-MM-dd HH:mm:ss"));
        System.out.println(dateTime.toString("yyyyMMddHHmmss"));
        System.out.println(dateTime.getMillisOfSecond());
        System.out.println(dateTime.getSecondOfDay());
        System.out.println(dateTime.getSecondOfMinute());
        System.out.println((int)(System.currentTimeMillis() / 1000));

    }
}

