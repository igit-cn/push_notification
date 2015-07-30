package com.yidian.push.data;

import java.util.HashMap;

/**
 * For smart push
 */
public enum PushChannel {
    IOS(0, "0", "IOS"),
    XIAOMI(1, "1", "xiaomi"),
    UMENG(2, "2", "umeng"),
    GETUI(3, "3", "getui")
    ;

    private static HashMap<Integer, PushChannel> intToChannel = new HashMap<>();
    private static HashMap<String, PushChannel> strToChannel = new HashMap<>();

    static {
        for (PushChannel pushChannel : PushChannel.values()) {
            intToChannel.put(pushChannel.id , pushChannel);
            strToChannel.put(pushChannel.strId, pushChannel);
        }
    }

    private int id;
    private String strId;
    private String name;
    PushChannel(int id, String strId, String name) {
        this.id = id;
        this.strId = strId;
        this.name = name;
    }
    public static PushChannel findChannel(int id) {
        return intToChannel.get(id);
    }
    public static PushChannel findChannel(String id) {
        return strToChannel.get(id);
    }
    public int getId() {
        return id;
    }
    public String toString(){
        return name;
    }

}
