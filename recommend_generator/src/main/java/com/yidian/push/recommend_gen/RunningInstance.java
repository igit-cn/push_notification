package com.yidian.push.recommend_gen;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tianyuzhi on 15/11/14.
 */

@Getter
public class RunningInstance {
    private static volatile AtomicInteger running = new AtomicInteger(0);

    public static int getRunningNumber() {
        return running.get();
    }

    public static void decRunningNumber() {
        running.decrementAndGet();
        return;
    }

    public static void incRunningNumber() {
        running.incrementAndGet();
        return;
    }

}
