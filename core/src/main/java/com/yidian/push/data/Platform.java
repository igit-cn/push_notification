package com.yidian.push.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yidianadmin on 15-1-14.
 */
public enum Platform {
    @SerializedName("all")
    ALL("all"),
    @SerializedName("android")
    ANDROID("android"),
    @SerializedName("iPhone")
    IPHONE("iPhone");
    private String name;
    private Platform(String name) {
        this.name = name;
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
