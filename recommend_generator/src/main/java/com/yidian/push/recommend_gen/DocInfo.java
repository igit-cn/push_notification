package com.yidian.push.recommend_gen;

/**
 * Created by tianyuzhi on 15/9/12.
 */
public class DocInfo {
    private String docId;
    private String fromId;
    private String title;

    public DocInfo(String docId, String fromId, String title) {
        this.docId = docId;
        this.fromId = fromId;
        this.title = title;
    }

    public static class Builder {
        private String docId;
        private String fromId;
        private String title;

        public Builder withDocId(String docId) {
            this.docId = docId;
            return this;
        }
        public Builder withFromId(String fromId) {
            this.fromId = fromId;
            return this;
        }
        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }
        public DocInfo build() {
            return new DocInfo(docId, fromId, title);
        }
    }
}
