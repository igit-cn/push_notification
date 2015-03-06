package com.yidian.push.utils;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

public class FileLockTest {
    private static final String SystemTmpPath = System.getProperty("java.io.tmpdir");

    @Test
    public void testLockInstance() throws Exception {
        System.out.println(System.getProperty("user.dir"));
        String lockFile = SystemTmpPath + "/" + "FileLockTest.lock";
        System.out.println("1:" + FileLock.lockInstance(lockFile));
        System.out.println("2:" + FileLock.lockInstance(lockFile));
        System.out.println("3:" + FileLock.lockInstance(lockFile));
    }
}