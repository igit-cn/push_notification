package com.yidian.push.generator.gen;

import java.util.List;

/**
 * Created by tianyuzhi on 15/6/18.
 */
public class SqlUtil {
    public static String genQuotedStringList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        if (null != list && list.size() > 0) {
            sb.append("(");
            for (String str : list) {
                sb.append("\"").append(str).append("\"");
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
