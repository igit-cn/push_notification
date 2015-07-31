package com.yidian.push.data;

import lombok.Getter;

import java.util.List;

/**
 * Created by tianyuzhi on 15/7/30.
 */
@Getter
public class XiaomiSingleMessage {
    private String appId;
    private String description;
    private int badge = 1;
    private String sound;
    private String title;
    private String docId;
    private ResourceType resourceType;
    private String pushType;
    private int notifyId = 0;
    private String notifyType;
    private List<String> tokens;
    private MessageType messageType;
    public XiaomiSingleMessage(String appId, String description, int badge, String sound,
                               String title, String docId,
                               ResourceType resourceType, String pushType,
                               int notifyId, String notifyType,
                               List<String> tokens, MessageType messageType) {
        this.appId = appId;
        this.description = description;
        this.badge = badge;
        this.title = title;
        this.docId = docId;
        this.resourceType = resourceType;
        this.pushType = pushType;
        this.notifyId = notifyId;
        this.notifyType = notifyType;
        this.tokens = tokens;
        this.messageType = messageType;
    }

    public int getPushNumber() {
        if (null == tokens) {
            return 0;
        }
        else {
            return tokens.size();
        }
    }

    public static class Build {
        private String appId;
        private String description;
        private int badge = 1;
        private String sound;
        private String title;
        private String docId;
        private ResourceType resourceType;
        private String pushType;
        private int notifyId = 0;
        private String notifyType;
        private List<String> tokens;
        private MessageType messageType;


        public Build withAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Build withDescription(String description) {
            this.description = description;
            return this;
        }

        public Build withBadge(int badge) {
            this.badge = badge;
            return this;
        }

        public Build withSound(String sound) {
            this.sound = sound;
            return this;
        }

        public Build withTitle(String title) {
            this.title = title;
            return this;
        }
        public Build withDocId(String docId) {
            this.docId = docId;
            return this;
        }
        public Build withResourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }
        public Build withPushType(String pushType) {
            this.pushType = pushType;
            return this;
        }
        public Build withNofityId(int notifyId) {
            this.notifyId = notifyId;
            return this;
        }
        public Build withNotifyType(String notifyType) {
            this.notifyType = notifyType;
            return this;
        }
        public Build withTokens(List<String> tokens) {
            this.tokens = tokens;
            return this;
        }
        public Build withMessageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }
        public XiaomiSingleMessage build() {
            return new XiaomiSingleMessage(appId, description, badge,
                    sound, title, docId,
                    resourceType, pushType,
                    notifyId, notifyType,
                    tokens, messageType);
        }
    }

}
