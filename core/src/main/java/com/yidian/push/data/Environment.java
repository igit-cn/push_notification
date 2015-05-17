package com.yidian.push.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yidianadmin on 15-3-9.
 */
public enum  Environment {
    @SerializedName("production")
    RRODUCTION("production"),
    @SerializedName("test")
    TEST("test");

    private String name;
    private Environment(String name) {this.name = name;}
}
