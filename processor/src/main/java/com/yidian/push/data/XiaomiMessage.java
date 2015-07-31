package com.yidian.push.data;

import lombok.Getter;

/**
 * Created by tianyuzhi on 15/7/30.
 */
@Getter
public class XiaomiMessage {
    private String description;
    private int badge = 1;
    private String sound;
    private String title;
    private String docId;
    private ResourceType resourceType;
    private String pushType;
    private int notifyId = 0;
    private String notifyType;
    private String token;
    private MessageType messageType;
    public XiaomiMessage(String description, int badge, String sound,
                         String title, String docId,
                         ResourceType resourceType, String pushType,
                         int notifyId, String notifyType,
                         String token, MessageType messageType) {
        this.description = description;
        this.badge = badge;
        this.sound = sound;
        this.title = title;
        this.docId = docId;
        this.resourceType = resourceType;
        this.pushType = pushType;
        this.notifyId = notifyId;
        this.notifyType = notifyType;
        this.token = token;
        this.messageType = messageType;
    }

    public static class Build {
        private String description;
        private int badge = 1;
        private String sound;
        private String title;
        private String docId;
        private ResourceType resourceType;
        private String pushType;
        private int notifyId = 0;
        private String notifyType;
        private String token;
        private MessageType messageType;


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
        public Build withToken(String token) {
            this.token = token;
            return this;
        }
        public Build withMessageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }
        public XiaomiMessage build() {
            return new XiaomiMessage(description, badge,
                    sound, title, docId,
                    resourceType, pushType,
                    notifyId, notifyType,
                    token, messageType);
        }
    }

}
