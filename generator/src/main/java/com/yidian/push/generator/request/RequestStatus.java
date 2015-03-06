package com.yidian.push.generator.request;

import java.util.HashMap;

/**
 * Created by yidianadmin on 15-3-4.
 */
public enum RequestStatus {
    READY("ready"),
    PROCESSING ("processing"),
    PROCESSED ( "processed"),
    BAD("bad"),
    UNKNOWN("unknown");

    private static HashMap<RequestStatus, String> ALL_STATUS_NAME = null;
    private static HashMap<String, RequestStatus> ALL_NAME_STATUS = null;
    static {
        RequestStatus[] all = RequestStatus.values();
        ALL_STATUS_NAME = new HashMap<>();
        ALL_NAME_STATUS = new HashMap<>();
        for (RequestStatus requestStatus : all) {
            ALL_STATUS_NAME.put(requestStatus, requestStatus.name);
            ALL_NAME_STATUS.put(requestStatus.name, requestStatus);
        }
    }

    public static RequestStatus getStatus(String name) {
        if (ALL_NAME_STATUS.containsKey(name)) {
            return ALL_NAME_STATUS.get(name);
        }
        return UNKNOWN;
    }

    private String name = null;
    private RequestStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
