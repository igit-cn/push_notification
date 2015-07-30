package com.yidian.push.data;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.annotations.SerializedName;
import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.push_request.PushRecord;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * Created by tianyuzhi on 15/7/29.
 */
@Log4j
public class UmengMessage {
    @SerializedName("appid")
    private String appId;
    @SerializedName("third_party_id")
    private String thirdPartyId;
    private String alias;
    @SerializedName("alias_type")
    private String aliasType;
    private String payload;
    private String policy;

    public UmengMessage(String appId, String thirdPartyId, String alias, String aliasType, String payload, String policy) {
        this.appId = appId;
        this.thirdPartyId = thirdPartyId;
        this.alias = alias;
        this.aliasType = aliasType;
        this.payload = payload;
        this.policy = policy;
    }

    public static class PolicyBuilder {
        private int expireTimeInSeconds = 0;
        public PolicyBuilder withExpireTimeInMinutes(int expireTimeInSeconds) {
            this.expireTimeInSeconds = expireTimeInSeconds;
            return this;
        }

        public String build() {
            DateTime start = DateTime.now();
            DateTime end = start.plusSeconds(expireTimeInSeconds);
            JSONObject policy = new JSONObject();
            policy.put("start_time", start.toString("yyyy-MM-dd HH:mm:ss"));
            policy.put("expire_time", end.toString("yyyy-MM-dd HH:mm:ss"));
            return policy.toString();
        }
    }

    public static class PayloadBuilder {
        //title=None, description=None, docid=None, display_type='notification', push_type=""
        private String title;
        private String description;
        private String docId;
        private MessageType messageType;
        private String pushType;
        private ResourceType resourceType;
        private String sound;
        private String appName;

        public PayloadBuilder withTitle(String title) {
            this.title = title;
            return this;
        }
        public PayloadBuilder withDescription(String description) {
            this.description = description;
            return this;
        }
        public PayloadBuilder withDocId(String docId) {
            this.docId = docId;
            return this;
        }
        public PayloadBuilder withMessageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }
        public PayloadBuilder withPushType(String pushType) {
            this.pushType = pushType;
            return this;
        }
        public PayloadBuilder withResourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }
        public PayloadBuilder withSound(String sound) {
            this.sound = sound;
            return this;
        }
        public PayloadBuilder withAppName(String appName) {
            this.appName = appName;
            return this;
        }
        public String build() {
            String playSound = "true";
            if (StringUtils.isEmpty(sound)) {
                playSound = "false";
            }
            String ticker = new StringBuilder(appName).append(":").append(description).toString();
            String showTitle = title;
            if (StringUtils.isEmpty(title)) {
                showTitle = appName;
            }
            else {
                showTitle = title;
            }
            JSONObject payload = new JSONObject();
            payload.put("display_type", messageType.toString());
            JSONObject bodyJson = new JSONObject();
            payload.put("body", bodyJson);
            if (messageType == MessageType.MESSAGE) {
                bodyJson.put("ticker", ticker);
                bodyJson.put("largeIcon", "");
                bodyJson.put("icon", "");
                bodyJson.put("title", showTitle);
                bodyJson.put("text", description);
                bodyJson.put("play_vibrate", playSound);
                bodyJson.put("play_lights", "true");
                bodyJson.put("play_sound", playSound);
                bodyJson.put("after_open", "go_app");
                bodyJson.put("builder_id", 1);
                bodyJson.put("img", "");
                bodyJson.put("sound", "");
                JSONObject custom = new JSONObject();
                JSONObject aps = new JSONObject();
                aps.put("alert", description);
                aps.put("badge", 1);
                aps.put("sound", sound);
                aps.put("title", showTitle);
                custom.put("aps", aps);
                custom.put("rid", docId);
                custom.put("rtype", resourceType.toString());
                custom.put("PT", pushType);
                bodyJson.put("custom", custom);
            }
            else {
                bodyJson.put("ticker", ticker);
                bodyJson.put("largeIcon", "");
                bodyJson.put("icon", "");
                bodyJson.put("title", showTitle);
                bodyJson.put("text", description);
                bodyJson.put("play_vibrate", playSound);
                bodyJson.put("play_lights", "true");
                bodyJson.put("play_sound", playSound);
                bodyJson.put("after_open", "go_app");
                bodyJson.put("builder_id", 1);
                bodyJson.put("img", 1);
                bodyJson.put("sound", "");

                JSONObject extra = new JSONObject();
                if (resourceType == ResourceType.NEWS) {
                    extra.put("rType", resourceType.toString());
                    extra.put("docid", docId);
                    extra.put("title", showTitle);
                    extra.put("PT", pushType);
                    extra.put("push", 1);
                }
                else if (resourceType == ResourceType.TOPIC) {
                    extra.put("rType", resourceType.toString());
                    extra.put("channelid", docId);
                    extra.put("channelname", showTitle);
                    extra.put("PT", pushType);
                    extra.put("push", 1);
                }
                else if (resourceType == ResourceType.URL) {
                    extra.put("rType", resourceType.toString());
                    extra.put("docid", docId);
                    extra.put("title", showTitle);
                    extra.put("PT", pushType);
                    extra.put("push", 1);
                }
                payload.put("extra", extra);
            }
            return payload.toString();
        }

    }

    public static class Build {
        private String appId;
        private String thirdPartyId;
        private String alias;
        private String aliasType;
        private String payload;
        private String policy;

        public Build withAppId(String appId) {
            this.appId = appId;
            return this;
        }
        public Build withThirdPartyId(String thirdPartyId) {
            this.thirdPartyId = thirdPartyId;
            return this;
        }
        public Build withAlias(String alias) {
            this.alias = alias;
            return this;
        }
        public Build withAliasType(String aliasType) {
            this.aliasType = aliasType;
            return this;
        }
        public Build withPayload(String payload) {
            this.payload = payload;
            return this;
        }
        public Build withPolicy(String policy) {
            this.policy = policy;
            return this;
        }
        public UmengMessage build() {
            return new UmengMessage(appId, thirdPartyId, alias, aliasType, payload, policy);
        }
    }
}
