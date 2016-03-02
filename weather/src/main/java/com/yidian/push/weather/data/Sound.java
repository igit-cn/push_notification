package com.yidian.push.weather.data;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianyuzhi on 16/3/1.
 */
public enum Sound {
    SOUND("1"),
    NO_SOUND("0");

    private String name = null;
    Sound(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    private static Map<String, Sound> stringSoundMap = new HashMap<>();
    static {
        for (Sound sound : Sound.values()) {
            stringSoundMap.put(sound.name, sound);
        }
    }

    public static Sound getSound(String soundStr) {
        Sound sound = stringSoundMap.get(soundStr);
        if (null == sound) {
            sound = NO_SOUND;
        }
        return sound;
    }
}
