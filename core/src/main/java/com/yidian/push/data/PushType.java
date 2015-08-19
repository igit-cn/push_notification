package com.yidian.push.data;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/5/26.
 */
public enum PushType {
    MORNING(1<<1, "2", 1),
    NOON(1<<2, "4", 2),
    EVENING(1<<3, "8", 3),
    BREAK(1<<4, "16", 4),
    LOCAL(1<<5, "32", 5),
    RECOMMEND(1<<6, "64", 6),
    RECOMMEND_1(1<<7, "128", 7),
    RECOMMEND_2(1<<8, "256", 8),
    RECOMMEND_3(1<<9, "512", 9),
    NIGHT(1<<10, "1024", 10),
    UNKNOWN(0, "0", 0);

    private static Map<Integer, PushType> INT_TO_PUSH_TYPE = new HashMap<>(20);
    private static Map<String, PushType> STRING_TO_PUSH_TYPE = new HashMap<>(20);
    static {
        for (PushType pushType : PushType.values()) {
            INT_TO_PUSH_TYPE.put(pushType.getInt(), pushType);
            STRING_TO_PUSH_TYPE.put(pushType.getString(), pushType);
        }
    }

    public static PushType getPushType(int key) {
        if (INT_TO_PUSH_TYPE.containsKey(key)) {
            return INT_TO_PUSH_TYPE.get(key);
        }
        return UNKNOWN;
    }

    public static PushType getPushType(String  key) {
        if (STRING_TO_PUSH_TYPE.containsKey(key)) {
            return STRING_TO_PUSH_TYPE.get(key);
        }
        return UNKNOWN;
    }

    private int value;
    private String strValue;
    private int logIntVal;
    PushType(int value, String strValue, int logIntVal) {
        this.value = value;
        this.strValue = strValue;
        this.logIntVal = logIntVal;
    }
    public int getInt() {
        return value;
    }
    public String getString() {
        return strValue;
    }
    public int getLogIntVal() {
        return logIntVal;
    }

    public static boolean isRecommendPush(String pushType) {
        PushType pt = getPushType(pushType);
        if (pt == RECOMMEND || pt == RECOMMEND_1 || pt == RECOMMEND_2 || pt == RECOMMEND_3) {
            return true;
        }
        else {
            return false;
        }
    }


}
