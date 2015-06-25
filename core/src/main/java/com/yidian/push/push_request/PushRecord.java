package com.yidian.push.push_request;

import com.yidian.push.data.PushChannel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yidianadmin on 15-1-12.
 */
@Getter
@Setter
public class PushRecord {
    public static final String FILED_SEPARATOR = "\u0001";
    public static final String TOKEN_SEPARATOR = "\u0002";
    public static final String TOKEN_ITEM_SEPARATOR = "\u0003";
    private int uid;
    private List<String> tokens;
    private String appId;
    private String docId;
    private String title;
    private String description;
    private int newsType;
    private String newsChannel;
    private int sound;
    private int nid = 0; // xiaomi notify id [0,4]
    private PushChannel pushChannel;


    public PushRecord(int uid, List<String> tokens, String appId,
                      String docId, String title, String description, int newsType,
                      String newsChannel, int nid, PushChannel pushChannel, int sound) {
        this.uid = uid;
        this.tokens = tokens;
        this.appId = appId;
        this.docId = docId;
        this.title = title;
        this.description = description;
        this.newsType = newsType;
        this.newsChannel = newsChannel;
        this.nid = nid;
        this.pushChannel = pushChannel;
        this.sound = sound;
    }

    public void addToken(String token) {
        if (tokens == null) {
            tokens = new ArrayList<>();
        }
        tokens.add(token);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(uid).append(FILED_SEPARATOR)
                .append(StringUtils.join(tokens, TOKEN_SEPARATOR))
                .append(FILED_SEPARATOR).append(appId == null ? "" : appId)
                .append(FILED_SEPARATOR).append(docId == null ? "" : docId)
                .append(FILED_SEPARATOR).append(title == null ? "" : title)
                .append(FILED_SEPARATOR).append(description == null ? "" : description)
                .append(FILED_SEPARATOR).append(newsType)
                .append(FILED_SEPARATOR).append(newsChannel == null ? "" : newsChannel)
                .append(FILED_SEPARATOR).append(sound)
                .append(FILED_SEPARATOR).append(nid)
                .append(FILED_SEPARATOR).append(pushChannel.getId());
        return sb.toString();
    }

    public static class Builder {
        private int uid;
        private List<String> tokens;
        private String appId;
        private String docId;
        private String title;
        private String head;
        private int newsType;
        private String newsChannel;
        private int nid = 0; // xiaomi notify id [0,1] only leave two in the notification center
        private PushChannel pushChannel;
        private int sound;

        public Builder addToken(String token) {
            if (tokens == null) {
                tokens = new ArrayList<>();
            }
            tokens.add(token);
            return this;
        }

        public Builder setUid(int uid) {
            this.uid = uid;
            return this;
        }

        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder setDocId(String docId) {
            this.docId = docId;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setHead(String head) {
            this.head = head;
            return this;
        }

        public Builder setNewsType(int newsType) {
            this.newsType = newsType;
            return this;
        }

        public Builder setNewsChannel(String newsChannel) {
            this.newsChannel = newsChannel;
            return this;
        }

        public Builder setNid(int nid) {
            this.nid = nid;
            return this;
        }

        public Builder setPushChannel(PushChannel pushChannel) {
            this.pushChannel = pushChannel;
            return this;
        }

        public Builder setSound(int sound) {
            this.sound = sound;
            return this;
        }

        public PushRecord build() {
            return new PushRecord(uid, tokens, appId, docId, title, head, newsType, newsChannel, nid, pushChannel, sound);
        }
    }
}
