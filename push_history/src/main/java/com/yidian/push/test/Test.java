package com.yidian.push.test;

import com.google.gson.Gson;

/**
 * Created by tianyuzhi on 15/10/12.
 */
public class Test {
    public static void main(String[] args) {
         class MethodInner {
            String key;
            String value;
            public MethodInner(String key, String value) {
                this.key = key;
                this.value = value;
            }
        }


        MethodInner methodInner = new MethodInner("a", "b");
        Gson gson = new Gson();
        System.out.println(gson.toJson(methodInner));
        System.out.println(gson.toJson(methodInner, MethodInner.class));
    }
}
