package com.yidian.push.data;

import java.util.HashMap;

/**
 * For smart push
 */
public enum PushChannel {
    IOS(0, "IOS"),
    XIAOMI(1, "xiaomi"),
    UMENG(2, "umeng")
    ;

    private static HashMap<Integer, PushChannel> intToChannel = new HashMap<>();
    static {
        for (PushChannel pushChannel : PushChannel.values()) {
            intToChannel.put(pushChannel.id , pushChannel);
        }
    }

    private int id;
    private String name;
    private PushChannel(int id, String name) {
        this.id = id;
        this.name = name;
    }
    public static PushChannel findChannel(int id) {
        return intToChannel.get(id);
    }
    public int getId() {
        return id;
    }

}
