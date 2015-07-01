package com.yidian.push.generator.gen;

import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
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
public class GetTodayFirstUserId {
    public static class HostPortTableUserId {
        String host;
        int port;
        String table;
        int userId;

        public HostPortTableUserId(String host, int port, String table, int userId) {
            this.host = host;
            this.port = port;
            this.table = table;
            this.userId = userId;
        }
    }

    public static List<HostPortTableUserId> getLatestFirstUserId(String path, int lookBackDays) throws IOException {
        List<HostPortTableUserId> list = new ArrayList<>(4);
        for (int i = 0; i < lookBackDays; i ++) {
            String day = DateTime.now().plusDays(i * -1).toString("yyyy-MM-dd");
            String fileName = new StringBuilder(path).append("/host_table_userid.").append(day).toString();
            File file = new File(fileName);
            if (!file.isFile()) {continue;}
            list = getFirstUserFromFile(fileName);
        }
        if (null == list || list.size() < 0) {
            log.error("could not get the first user id");
        }
        return list;
    }

    public static List<HostPortTableUserId> getTodayFirstUserId(String path) throws IOException {
        String today = DateTime.now().toString("yyyy-MM-dd");
        return getFirstUserIdOfDay(path, today);
    }

    public static List<HostPortTableUserId> getFirstUserIdOfDay(String path, String day) throws IOException {
        String file = new StringBuilder(path).append("/host_table_userid.").append(day).toString();
        return getFirstUserFromFile(file);
    }

    public static List<HostPortTableUserId> getFirstUserFromFile(String file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = null;
        List<HostPortTableUserId> list = new ArrayList<>(4);
        while ((line = bufferedReader.readLine()) != null) {
            String[] arr = line.split(" ");
            if (arr.length != 4) {
                continue;
            }
            String host = arr[0];
            int port = 3306;
            try {
                port = Integer.parseInt(arr[1]);
            } catch (Exception e) {
            }
            String table = arr[2];
            int userId = 0;
            try {
                userId = Integer.parseInt(arr[3]);
            } catch (Exception e) {
            }
            HostPortTableUserId hostPortTableUserId = new HostPortTableUserId(host, port, table, userId);
            list.add(hostPortTableUserId);
        }
        return list;
    }
}
