package com.yidian.push.generator;

/**
 * Created by tianyuzhi on 15/6/29.
 */
public class Table {
    public static final String PUSH = "PUSH";  //IPhone
    public static final String PUSH_FOR_ANDROID = "PUSH_FOR_ANDROID"; // android

    public static boolean isIPhone(String table) {
        return PUSH.equals(table);
    }
    public static int getTableId(String table) {
        if (PUSH.equals(table)) {
            return 0;
        }
        else if (PUSH_FOR_ANDROID.equals(table)) {
            return 1;
        }
        return -1;
    }
}
