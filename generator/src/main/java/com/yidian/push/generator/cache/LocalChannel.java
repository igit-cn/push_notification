package com.yidian.push.generator.cache;

import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tianyuzhi on 15/7/9.
 */
@Log4j
public class LocalChannel {
    public static Set<String> getLocalChannels (String cityChannelMappingFile) {
        Set<String> localChannels = new HashSet<>(300);
        try {
            BufferedReader br = new BufferedReader(new FileReader(cityChannelMappingFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(" ");
                if (arr.length >= 2) {
                    localChannels.add(arr[1]);
                }
            }
        } catch (IOException e) {
            log.error("could not get the local channels with exception : " + ExceptionUtils.getFullStackTrace(e));
        }
        return localChannels;
    }


}
