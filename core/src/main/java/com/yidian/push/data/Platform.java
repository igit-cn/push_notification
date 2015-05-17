package com.yidian.push.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yidianadmin on 15-1-14.
 */
public enum Platform {
    @SerializedName("all")
    ALL("all", ""),
    @SerializedName("android")
    ANDROID("android", "PUSH_FOR_ANDROID"),
    @SerializedName("iPhone")
    IPHONE("iPhone", "PUSH");
    private String name;
    @Getter
    private String table;
    private Platform(String name, String table) {
        this.name = name;
        this.table = table;
    }

    private static List<Platform> ALL_PLATFORMS = null;
    static {
        ALL_PLATFORMS = new ArrayList<>();
        for (Platform platform : Platform.values()) {
            ALL_PLATFORMS.add(platform);
        }
    }
    public static List<Platform> getAllPlatforms() {
        return ALL_PLATFORMS;
    }
    @Override
    public String toString() {
        return name;
    }
}
