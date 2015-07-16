package com.yidian.push.generator.gen;

import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
                    continue;
                }
                list = getUsersFromFile(fileName);
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
                userId = Long.parseLong(line);
            } catch (Exception e) {
            }
            list.add(userId);
        }
        return list;
    }
}
