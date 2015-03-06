package com.yidian.push.generator.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by yidianadmin on 15-3-5.
 */
@Setter
@Getter
public class Request {
    private String fileName;
    private RequestStatus requestStatus;

    public Request(String fileName) {
        this.fileName = fileName;
        this.requestStatus = parseFileName(fileName);
    }

    public static RequestStatus parseFileName(String fileName) {
        String[] arr = fileName.split("/");
        RequestStatus requestStatus = null;
        if (arr.length > 1) {
            requestStatus = RequestStatus.getStatus(arr[arr.length-2]);
        }
        return requestStatus;
    }
}
