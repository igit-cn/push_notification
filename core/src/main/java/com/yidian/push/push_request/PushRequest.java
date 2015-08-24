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

    public void parseFileName(String fileName) {
        ///ssd/data/push_notification/test_request/ready/20150801171524-10_111_0_70-3306-PUSH-16.0a563ae1_0f28_4bbf_a63e_611f00be515e.offset.0.job
        String[] tmp = fileName.split("/");
        String name = tmp[tmp.length-1];
        String prefix = name.split("\\.")[0];
        String[] arr = prefix.split("-");
        if (arr.length >= 5) {
            this.pushType = arr[4];
        }
        if (arr.length >= 4) {
            this.table = arr[3];
        }
        this.pushRequestStatus = getStatus();
    }
}
