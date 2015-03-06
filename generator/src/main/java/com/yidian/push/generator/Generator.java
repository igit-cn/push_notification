package com.yidian.push.generator;

import com.yidian.push.utils.FileLock;

/**
 * Created by yidianadmin on 15-3-5.
 */
public class Generator {
    public static void main(String[] args) {
        String lockFile = "push_request_generator.lock";
        if (!FileLock.lockInstance(lockFile)) {
            System.out.println("One instance is already running, just quit.");
            System.exit(1);
        }
    }
}
