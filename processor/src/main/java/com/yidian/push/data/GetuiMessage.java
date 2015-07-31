package com.yidian.push.data;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by tianyuzhi on 15/7/29.
 */
public class GetuiMessage {
    private String payload;
    private String token;
    @SerializedName("appid")
    private String appId;
    @SerializedName("appkey")
    private String appKey;
    public GetuiMessage(String payload, String token, String appId, String appKey) {
        this.payload = payload;
        this.token = token;
        this.appId = appId;
        this.appKey = appKey;
    }

    public static class PayloadBuilder {
        private String description;
        private String sound;
        private int badge = 1;
        private String title;
        private String docId;
        private ResourceType resourceType;
        private String pushType;

        public PayloadBuilder withDescription(String description) {
            this.description = description;
            return this;
        }
        public PayloadBuilder withSound(String sound) {
            this.sound = sound;
            return this;
        }
        public PayloadBuilder withBadge(int badge) {
            this.badge = badge;
            return this;
        }
        public PayloadBuilder withTitle(String title) {
            this.title = title;
            return this;
        }
        public PayloadBuilder withDocId(String docId) {
            this.docId = docId;
            return this;
        }
        public PayloadBuilder withResourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }
        public PayloadBuilder withPushType(String pushType) {
            this.pushType = pushType;
            return this;
        }
        public String build() {
            JSONObject payload = new JSONObject();
            JSONObject aps = new JSONObject();
            payload.put("aps", aps);
            aps.put("alert", description);
            aps.put("badge", badge);
            aps.put("sound", sound);
            aps.put("title", title);
            payload.put("rid", docId);
            payload.put("rtype", resourceType.toString());
            payload.put("PT", pushType);
            return payload.toString();
        }
    }


    public static class Build {
        private String payload;
        private String token;
        private String appId;
        private String appKey;

        public Build withPayload(String payload) {
            this.payload = payload;
            return this;
        }

        public Build withToken(String token) {
            this.token = token;
            return this;
        }

        public Build withAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Build withAppKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public GetuiMessage build() {
            return new GetuiMessage(payload, token, appId, appKey);
        }
    }
}
