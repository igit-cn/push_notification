package com.yidian.push.generator.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by tianyuzhi on 15/7/2.
 */
public class Test2 {
    public static void startCPUHungryThread()
    {
        Runnable runnable = new Runnable(){
            public void run()
            {
                while(true)
                {
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i=0; i<2; i++)
        {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    startCPUHungryThread();
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

    }

}
