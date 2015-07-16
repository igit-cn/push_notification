package com.yidian.push.generator.data;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/7/9.
 */
public enum ProtectMinutes {
    ALL("all", 0),
    ALL_YDDK("all_yddk", 0),
    AUTO("auto", 30),
    AUTO_BREAK("auto_break", 0),
    IN_ACTIVITY("all_inactivity", 0);

    private static Map<String, Integer> nameMinMapping = new HashMap<>(6);
    static {
        for (ProtectMinutes protectMinutes : ProtectMinutes.values()) {
            nameMinMapping.put(protectMinutes.name, protectMinutes.minutes);
        }
    }

    public static int getProtectMinute(String name) {
        if (StringUtils.isNotEmpty(name) && nameMinMapping.containsKey(name)) {
            return nameMinMapping.get(name);
        }
        else {
            return 0;
        }
    }

    ProtectMinutes(String name, int minutes) {
        this.name = name;
        this.minutes = minutes;
    }
    private String name = null;
    private int minutes = 0;
}
