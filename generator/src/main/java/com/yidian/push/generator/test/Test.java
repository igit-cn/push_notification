package com.yidian.push.generator.test;

import com.yidian.push.utils.DateUtil;
import org.joda.time.DateTime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by yidianadmin on 15-3-4.
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {
        DateTime dateTime = new DateTime();
        System.out.println(dateTime);
        System.out.println(dateTime.getSecondOfDay());
        System.out.println(dateTime.getSecondOfDay()/60);
        System.out.println( DateTime.now().getMinuteOfDay());
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

        ExecutorService service = Executors.newFixedThreadPool(1000);
        for (int i = 0; i < 4; i++) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    System.out.println("thread start");
                }
            };
            service.execute(run);
        }
        service.shutdown();
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        System.out.println("all thread complete");


    }
}

