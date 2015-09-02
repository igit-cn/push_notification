package com.yidian.push.response;

import lombok.Setter;

/**
 * Created by yidianadmin on 15-4-27.
 */
@Setter
public class AddRecordResponse {
    private int code = 0;
    private String status = null;
    private String description = null;

    public void markSuccess() {
        this.code = 0;
        this.status = "success";
    }
    public void markFailure() {
        this.code = 1;
        this.status = "failed";
    }
}
