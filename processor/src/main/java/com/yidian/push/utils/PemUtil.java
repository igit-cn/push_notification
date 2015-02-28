package com.yidian.push.utils;

/**
 * Created by yidianadmin on 15-1-13.
 */
public class PemUtil {
    public static String getAppPem(String pemBase, String appId) {
        return new StringBuilder(pemBase).append("/").append(appId).append(".pem").toString();
    }

    public static String getUmengPem(String pemBase, String appId) {
        return new StringBuilder(pemBase).append("/").append(appId).append(".umeng").toString();
    }

    public static String getXiaomiPem(String pemBase, String appId) {
        return new StringBuilder(pemBase).append("/").append(appId).append(".mmpp").toString();
    }
}
