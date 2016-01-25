package com.yidian.push.weather.response;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by tianyuzhi on 16/1/25.
 */
@Getter
@Setter
public class Response {
    private static int SUCCESS_CODE = 0;
    private static int FAILED_CODE = 1;
    private static String SUCCESS_STATUS = "success";
    private static String FAILED_STATUS = "failed";

    private int code = FAILED_CODE;
    private String status = FAILED_STATUS;
    private String description = null;

    public void markSuccess() {
        code = SUCCESS_CODE;
        status = SUCCESS_STATUS;
    }
    public void markFailure() {
        code = FAILED_CODE;
        status = FAILED_STATUS;
    }
}
