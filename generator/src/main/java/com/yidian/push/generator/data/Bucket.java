package com.yidian.push.generator.data;

/**
 * Created by tianyuzhi on 15/7/9.
 */
public class Bucket {
    public static int getBucketId(int userId) {
        return userId % 10;
    }

    public static int getBucketId(long userId) {
        return (int)(userId % 10);
    }
}
