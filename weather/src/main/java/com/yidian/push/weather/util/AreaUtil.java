package com.yidian.push.weather.util;

import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tianyuzhi on 16/1/19.
 */
public class AreaUtil {
    private static final String LOCATION_CITY_SUFFIX = "市";
    private static final String LOCATION_PROVINCE_SUFFIX = "省";
    private static final Set<String> LOCATION_NON_PROCESS = new HashSet<>();
    static {
        String[] nonProcess = {"潜江市","嘉峪关市","吉林市","白银市","百色市","北海市","朝阳市","海南市"};
        for (String city : nonProcess) {
            LOCATION_NON_PROCESS.add(city);
        }
    }

    public static String normalize(String location) {
        if (StringUtils.isEmpty(location)) {
            return location;
        }
        if (location.indexOf("/") != -1) {
            location = location.substring(0, location.indexOf("/"));
        }
        if (LOCATION_NON_PROCESS.contains(location)) {
            return location;
        }
        else if (location.endsWith(LOCATION_PROVINCE_SUFFIX)) {
            return location.substring(0, location.indexOf(LOCATION_PROVINCE_SUFFIX));
        }
        else if (location.endsWith(LOCATION_CITY_SUFFIX)) {
            return location.substring(0, location.indexOf(LOCATION_CITY_SUFFIX));
        }
        return location;
    }
}
