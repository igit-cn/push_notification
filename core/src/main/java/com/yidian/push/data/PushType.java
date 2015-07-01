package com.yidian.push.data;

/**
 * Created by tianyuzhi on 15/5/26.
 */
public enum PushType {
    MORNING(1<<1, "2"),
    NOON(1<<2, "4"),
    EVENING(1<<3, "8"),
    BREAK(1<<4, "16"),
    LOCAL(1<<5, "32"),
    RECOMMEND(1<<6, "64"),
    NIGHT(1<<10, "1024"),
    UNKNOWN(0, "0");

    private int value;
    private String strValue;
    PushType(int value, String strValue) {
        this.value = value;
        this.strValue = strValue;
    }
    public int getInt() {
        return value;
    }
    public String getString() {
        return strValue;
    }


}
