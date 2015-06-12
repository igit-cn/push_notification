package com.yidian.push.utils;

import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * Created by yidianadmin on 15-3-5.
 * http://stackoverflow.com/questions/177189/how-to-implement-a-single-instance-java-application
 * also someone uses a local socket to make sue the single instance
 *    : http://www.rbgrn.net/content/43-java-single-application-instance
 *    : http://stackoverflow.com/questions/7397769/java-single-instance-software-with-socket-issue-in-closing-socket-under-windows
 *
 */
@Log4j
public class FileLock {
    /**
     * If the the program killed by kill -9, you should remove the lock file manually
     * @param lockFile
     * @return
     */
    public static boolean lockInstance(final String lockFile) {
        try {
            final File file = new File(lockFile);
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            final java.nio.channels.FileLock fileLock = randomAccessFile.getChannel().tryLock();
            if (fileLock != null) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            fileLock.release();
                            randomAccessFile.close();
                            file.delete();
                            log.info("release the lock file");
                        } catch (Exception e) {
                            log.error("Unable to remove LOCK FILE: " + lockFile, e);
                        }
                    }
                });
                log.info("get the LOCK FILE: " + lockFile);
                return true;
            }
        } catch (Exception e) {
            log.error("Unable to create and/or LOCK FILE: " + lockFile, e);
        }
        return false;
    }
}
