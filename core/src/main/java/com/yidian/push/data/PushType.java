package com.yidian.push.data;

/**
 * Created by tianyuzhi on 15/5/26.
 */
public enum PushType {
    MORNING(1<<1),
    NOON(1<<2),
    EVENING(1<<3),
    BREAK(1<<4),
    LOCAL(1<<5),
    RECOMMEND(1<<6),
    NIGHT(1<<10),
    UNKNOWN(0);

    private int value;
    PushType(int value) {
        this.value = value;
    }
    public int getInt() {
        return value;
    }


}
