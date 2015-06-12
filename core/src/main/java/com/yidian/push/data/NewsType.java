package com.yidian.push.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yidianadmin on 15-3-9.
 */
public enum NewsType {

    PUSH_DISABLED(0),
    PUSH_DEFAULT(1<<0),
    PUSH_MORNING(1<<1),
    PUSH_NOON(1<<2),
    PUSH_EVENING(1<<3),
    PUSH_BREAK(1<<4),
    PUSH_LOCAL(1<<5),
    PUSH_PERSONALIZATION(1<<6);
    private int intValue;
    private NewsType(int intValue) {
        this.intValue = intValue;
    }
    private static Map<Integer, String> IntToStringMapping = new HashMap<>();
    static {
        for (NewsType newsType : NewsType.values()) {
            IntToStringMapping.put(newsType.getIntValue(), String.valueOf(newsType.getIntValue()));
        }
    }
    public int getIntValue() {return intValue;}
    public String getStringValue() {return IntToStringMapping.get(intValue);}


}
