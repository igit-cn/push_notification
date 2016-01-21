package com.yidian.push.weather.data;

import lombok.Getter;

/**
 * Created by tianyuzhi on 16/1/18.
 */
public enum WeatherType {
    OBSERVE("observe"),
    FORECAST("forecast"),
    AIR("air"),
    ALARM("alarm");

    @Getter
    private String name = null;

    WeatherType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
