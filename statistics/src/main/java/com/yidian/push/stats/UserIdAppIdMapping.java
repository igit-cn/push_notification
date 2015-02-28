package com.yidian.push.stats;



import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yidianadmin on 15-1-14.
 */
@Log4j
public class UserIdAppIdMapping {
    public static Map<Integer, String> loadMapping(String mapFile) throws IOException {
        Map<Integer, String> map = new HashMap<>();
        BufferedReader reader = null;
        int validRecordCount = 0;
        try {
            reader = new BufferedReader(new FileReader(new File(mapFile)));
            String line = null;
            while ((line=reader.readLine())!=null) {
                String[] arr = line.trim().split("\t");
                if (arr.length<2) {continue;}
                int userId = Integer.parseInt(arr[0]);
                String appId = arr[1];
                if (appId.contains("hipu")) {
                    appId = "yidian";
                }
                map.put(userId, appId);
                validRecordCount ++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        log.info(String.format("total read [%d] records from file[%s]", validRecordCount, mapFile));

        return map;
    }
}
