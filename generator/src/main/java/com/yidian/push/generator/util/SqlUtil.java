package com.yidian.push.generator.util;

import java.util.List;

/**
 * Created by tianyuzhi on 15/6/18.
 */
public class SqlUtil {
    public static String genQuotedStringList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        if (null != list && list.size() > 0) {
            sb.append("(");
            for (String str : list) {
                if (!isFirst) {
                    sb.append(",");
                }
                isFirst = false;
                sb.append("\"").append(str).append("\"");
            }
            sb.append(")");
        }
        return sb.toString();
    }

    public static String genQuotedLongList(List<Long> list) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        if (null != list && list.size() > 0) {
            sb.append("(");
            for (Long str : list) {
                if (!isFirst) {
                    sb.append(",");
                }
                isFirst = false;
                sb.append(str);
            }
            sb.append(")");
        }
        return sb.toString();
    }
}