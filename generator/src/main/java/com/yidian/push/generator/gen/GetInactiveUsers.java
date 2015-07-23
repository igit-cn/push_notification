package com.yidian.push.generator.gen;

import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyuzhi on 15/6/19.
 */
@Log4j
public class GetInactiveUsers {


    public static List<Long> getInactiveUsers(String path, String prefix, int lookBackDays) {
        List<Long> list = null;
        try {
            for (int i = 0; i < lookBackDays; i++) {
                String day = DateTime.now().plusDays(i * -1).toString("yyyy-MM-dd");
                String fileName = getFileName(path, prefix, day);
                File file = new File(fileName);
                if (!file.isFile()) {
                    log.info(file + " doesn't exist");
                    continue;
                }
                log.info("try to get in active users from file : " + fileName);
                list = getUsersFromFile(fileName);
                log.info("get the " + list.size() + " users from file : " + fileName);
                break;
            }
        } catch (IOException e) {
            log.error("get the inactive users failed with exception: " + ExceptionUtils.getFullStackTrace(e));
        }
        if (null == list || list.size() <= 0) {
            log.error("could not get the inactive user id");
        }
        return list;
    }

    private static String getFileName(String path, String prefix, String day) {
        return new StringBuilder(path).append("/").append(prefix).append(".").append(day).toString();
    }

    public static List<Long> getUsersFromFile(String file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = null;
        List<Long> list = new ArrayList<>(400000);
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            long userId = 0;
            try {
                String [] arr = line.split("\t");
                if (arr.length >= 3) {
                    long uid = Long.parseLong(arr[0]);
                    String appId = arr[1];
                    String status = arr[2];
                    if ("xiaomi".equals(appId) && "0".equals(status)) {
                        userId = uid;
                    }
                }
                userId = Long.parseLong(line);
            } catch (Exception e) {
            }
            if (userId > 0) {
                list.add(userId);
            }
        }
        return list;
    }

    public static List<Long> getUsersFromFile2(String file) throws IOException {

        Path path = new File(file).toPath();
        List<Long> list = new ArrayList<>(400000);

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (String line : lines) {
            line = line.trim();
            long userId = 0;
            try {
                String[] arr = line.split("\t");
                if (arr.length >= 3) {
                    long uid = Long.parseLong(arr[0]);
                    String appId = arr[1];
                    String status = arr[2];
                    if ("xiaomi".equals(appId) && "0".equals(status)) {
                        userId = uid;
                    }
                }
                userId = Long.parseLong(line);
            } catch (Exception e) {
            }
            if (userId > 0) {
                list.add(userId);
            }
        }
        return list;
    }
}

