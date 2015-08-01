package com.yidian.push.test;

import com.yidian.push.utils.GsonFactory;

/**
 * Created by tianyuzhi on 15/7/28.
 */
public class Test {
    public static void main(String[] args) {
        String aStr = "1,2,3,,,";
        System.out.println(GsonFactory.getNonPrettyGson().toJson(aStr.split(",", 8)));
        String str = "sc123";
        System.out.print(Integer.toHexString(Integer.parseInt(str.substring(2))));
    }
}
