package com.yidian.push.weather.data;

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

    public static Sound getSound(String soundStr) {
        Sound sound = Sound.valueOf(soundStr);
        if (null == sound) {
            sound = NO_SOUND;
        }
        return sound;
    }
}
