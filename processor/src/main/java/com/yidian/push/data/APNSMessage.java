package com.yidian.push.data;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/7/27.
 */
public class APNSMessage {
    @SerializedName("appid")
    private String appId = null;
    private String alert = null;
    private int badge = 0;
    private String token = null;
    private String sound = null;
    private Map<String, String> params = null;

    public APNSMessage(String appId, String alert, int badge, String token, String sound, Map<String, String> params) {
        this.appId = appId;
        this.alert = alert;
        this.badge = badge;
        this.token = token;
        this.sound = sound;
        this.params = params;
    }

    public static class Build {
        private String appId = null;
        private String alert = null;
        private int badge = 0;
        private String token = null;
        private String sound = null;
        private Map<String, String> params = null;

        public Build withAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Build withAlert(String alert) {
            this.alert = alert;
            return this;
        }

        public Build withBadge(int badge) {
            this.badge = badge;
            return this;
        }

        public Build withToken(String token) {
            this.token = token;
            return this;
        }

        public Build withSound(String sound) {
            this.sound = sound;
            return this;
        }

        public Build withParam(String key, String value) {
            if (null == params) {
                params = new HashMap<>(3);
            }
            params.put(key, value);
            return this;
        }

        public APNSMessage build() {
            return new APNSMessage(appId, alert, badge, token, sound, params);
        }
    }


}
