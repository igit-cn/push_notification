package com.yidian.push.data;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/7/29.
 */
@Getter
public enum  MessageType {
    NOTIFICATION(0, "notification"),
    MESSAGE(1, "message");

    private static Map<Integer, MessageType> INT_TO_MESSAGE = new HashMap<>(5);
    private static Map<String, MessageType> STRING_TO_MESSAGE = new HashMap<>(5);

    static  {
        for (MessageType messageType : MessageType.values()) {
            INT_TO_MESSAGE.put(messageType.intVal, messageType);
            STRING_TO_MESSAGE.put(messageType.strVal, messageType);
        }
    }

    public static MessageType getMessageType(String str) {
        return STRING_TO_MESSAGE.get(str);
    }
    public static MessageType getMessageType(int iMessageType) {
        return INT_TO_MESSAGE.get(iMessageType);
    }

    MessageType(int intVal, String strVal) {
        this.intVal = intVal;
        this.strVal = strVal;
    }
    private int intVal;
    private String strVal;

    public String getStrVal() {
        return strVal;
    }
    public int getIntVal() {
        return intVal;
    }

    @Override
    public String toString() {
        return strVal;
    }

}
