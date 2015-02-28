package com.yidian.push.push_request;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by yidianadmin on 15-1-12.
 */
@Setter
@Getter
public class PushRequest {
    public final static String FILED_SEPARATOR = "\u0001";
    public final static String TOKEN_SEPARATOR = "\u0002";
    private String fileName = null;
    private String pushType = null;
    private String table = null;
    private PushRequestStatus pushRequestStatus;

    public PushRequest(String fileName) {
        this.fileName = fileName;
        parseFileName(fileName);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        parseFileName(fileName);
    }

    public PushRequestStatus getStatus() {
        String[] arr = fileName.split("/");
        if (arr.length > 1) {
            String typeStr = arr[arr.length-2];
            return PushRequestStatus.getStatus(typeStr);
        }
        return null;
    }

    public void parseFileName(String name) {
        //#20141121163251-10_111_0_70-PUSH_FOR_ANDROID-16.fb402db4_7158_11e4_900c_d4ae52a6cc47.offset.0.job
        String prefix = name.split("\\.")[0];
        String[] arr = prefix.split("-");
        if (arr.length >= 4) {
            this.pushType = arr[3];
        }
        this.table = arr[2];
        this.pushRequestStatus = getStatus();
    }
}
