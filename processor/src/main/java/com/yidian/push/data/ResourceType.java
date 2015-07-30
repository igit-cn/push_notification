package com.yidian.push.data;

import org.apache.commons.lang.StringUtils;

/**
 * Created by tianyuzhi on 15/7/27.
 */
public enum  ResourceType {
    TOPIC("topic"),
    NEWS("news"),
    URL("url"),
    UNKNOWN("unknown");

    private String strVal;
    ResourceType(String strVal) {
        this.strVal = strVal;
    }

    @Override
    public String toString()
    {
        return strVal;
    }

    private static final String DOCID_PREFIX = PushLog.DOC_ID_PREFIX;
    private static final String LIST_PREFIX = PushLog.LIST_PREFIX;
    private static final String TYPE_TOPIC = "topic";
    private static final String TYPE_NEWS = "news";
    private static final String TYPE_UNKNOWN = "unknown";


    public static ResourceType getResourceType(String docId) {
        if (StringUtils.isNotEmpty(docId)) {
            if (docId.startsWith(LIST_PREFIX)) {
                return TOPIC;
            }
            else {
                return NEWS;
            }
        }
        else {
            return UNKNOWN;
        }
    }
}
