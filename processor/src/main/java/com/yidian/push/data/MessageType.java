package com.yidian.push.data;

import lombok.Getter;

/**
 * Created by tianyuzhi on 15/7/29.
 */
@Getter
public enum  MessageType {
    NOTIFICATION(0, "notification"),
    MESSAGE(1, "message");

    MessageType(int intVal, String strVal) {
        this.intVal = intVal;
        this.strVal = strVal;
    }
    private int intVal;
    private String strVal;

    public String getStrVal() {
        return strVal;
    }

    @Override
    public String toString() {
        return strVal;
    }

}
