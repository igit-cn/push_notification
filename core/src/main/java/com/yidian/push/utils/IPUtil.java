package com.yidian.push.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yidianadmin on 14-8-1.
 */
public class IPUtil {
    public static final long IP_MAX_INT = 4294967295L;
    public static final String IP_PATTERN_STRING = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$";
    private static Pattern pattern = Pattern.compile(IP_PATTERN_STRING);


    public static boolean isValidIP(String ipAddress) {
        if (null == ipAddress) {
            return false;
        }
        Matcher matcher = pattern.matcher(ipAddress);
        boolean isValid = true;
        if (matcher.matches()) {
            String[] arr = ipAddress.split("\\.");
            for (String ipDot : arr) {
                int tmp = Integer.parseInt(ipDot);
                if (tmp > 255 || tmp < 0) {
                    isValid = false;
                    break;
                }
            }
        }
        else {
            isValid = false;
        }
        return isValid;

    }

    public static boolean isValidIP(long ip) {
        if (ip >= 0 && ip <= IP_MAX_INT) {
            return true;
        }
        else {
            return false;
        }
    }

    public static long stringToLong(String ip) {
        if (!isValidIP(ip)) {
            return -1;
        }
        else {
            long  ipLong= 0;
            String[] arr = ip.split("\\.");
            for (String ipDot : arr) {
                ipLong = ipLong * 256 + Integer.parseInt(ipDot);
            }
            return ipLong;
        }
    }
}
