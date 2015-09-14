package com.yidian.push.recommend_gen;

/**
 * Created by tianyuzhi on 15/9/12.
 */
public class DocInfo {
    private String docId;
    private String channel;
    private String title;

    public DocInfo(String docId, String channel, String title) {
        this.docId = docId;
        this.channel = channel;
        this.title = title;
    }

    public static class Builder {
        private String docId;
        private String channel;
        private String title;

        public Builder withDocId(String docId) {
            this.docId = docId;
            return this;
        }
        public Builder withChannel(String channel) {
            this.channel = channel;
            return this;
        }
        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }
        public DocInfo build() {
            return new DocInfo(docId, channel, title);
        }
    }
}
