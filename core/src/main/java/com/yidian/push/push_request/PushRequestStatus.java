package com.yidian.push.push_request;

import java.util.HashMap;

/**
 * Created by yidianadmin on 15-1-12.
 */
public enum PushRequestStatus {
    PREPARING("preparing"),
    READY("ready"),
    PROCESSING ("processing"),
    PROCESSED ( "processed"),
    LOGGING("logging"),
    LOGGED("logged"),
    BAD("bad"),
    UNKNOWN("unknown");

    private String name = null;

    private PushRequestStatus(String name) {
        this.name = name;
    }

    private static HashMap<PushRequestStatus, String> ALL_STATUS_NAME = null;
    private static HashMap<String, PushRequestStatus> ALL_NAME_STATUS = null;
    static {
        PushRequestStatus[] all = PushRequestStatus.values();
        ALL_STATUS_NAME = new HashMap<>();
        ALL_NAME_STATUS = new HashMap<>();
        for (PushRequestStatus requestStatus : all) {
            ALL_STATUS_NAME.put(requestStatus, requestStatus.name);
            ALL_NAME_STATUS.put(requestStatus.name, requestStatus);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public static HashMap<PushRequestStatus, String> getAllStatusName() {
        return ALL_STATUS_NAME;
    }
    public static  PushRequestStatus getStatus(String status) {
        if (ALL_NAME_STATUS.containsKey(status)) {
            return ALL_NAME_STATUS.get(status);
        }
        return UNKNOWN;
    }
    public static boolean isValid(PushRequestStatus status) {
        if (status!=null && status != UNKNOWN) {
            return true;
        }
        return false;
    }
}
