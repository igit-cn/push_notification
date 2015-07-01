package com.yidian.push.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yidianadmin on 15-1-14.
 */
@Getter
public enum Platform {
    @SerializedName("all")
    ALL("all", "", -1),
    @SerializedName("iPhone")
    IPHONE("iPhone", "PUSH", 0),
    @SerializedName("android")
    ANDROID("android", "PUSH_FOR_ANDROID", 1);

    private String name;
    private String table;
    private int tableId;
    private Platform(String name, String table, int tableId) {
        this.name = name;
        this.table = table;
        this.tableId = tableId;
    }


    transient public static final String PUSH = "PUSH";  //IPhone
    transient public static final String PUSH_FOR_ANDROID = "PUSH_FOR_ANDROID"; // android

    private static List<Platform> ALL_PLATFORMS = null;
    private static Map<String, Integer> TABLE_ID_MAPPING = null;
    static {
        ALL_PLATFORMS = new ArrayList<>();
        TABLE_ID_MAPPING = new HashMap<>(5);
        for (Platform platform : Platform.values()) {
            ALL_PLATFORMS.add(platform);
            TABLE_ID_MAPPING.put(platform.getTable(), platform.getTableId());
        }
    }
    public static List<Platform> getAllPlatforms() {
        return ALL_PLATFORMS;
    }
    public static boolean isIPhone(String table) {
        return PUSH.equals(table);
    }
    public static boolean isAndroid(String table) {
        return PUSH_FOR_ANDROID.equals(table);
    }
    public static int getTableId(String table) {
        if (TABLE_ID_MAPPING.containsKey(table)) {
            return TABLE_ID_MAPPING.get(table);
        }
        return -1;
    }
    @Override
    public String toString() {
        return name;
    }
}
