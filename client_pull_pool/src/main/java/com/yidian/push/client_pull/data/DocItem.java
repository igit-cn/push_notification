package com.yidian.push.client_pull.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by tianyuzhi on 16/3/21.
 */
@Getter
@Setter
public class DocItem {
    @SerializedName("docid")
    private String docId = null;
    private String title = null;
    private String fromId = null;
    private String date = null;

    public DocItem(String docId, String title, String fromId, String date) {
        this.docId = docId;
        this.title = title;
        this.fromId = fromId;
        this.date = date;
    }

    public static class Builder {
        private String docId = null;
        private String title = null;
        private String fromId = null;
        private String date = null;

        public Builder withDocId(String docId) {
            this.docId = docId;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withFromId(String fromId) {
            this.fromId = fromId;
            return this;
        }

        public Builder withDate(String date) {
            this.date = date;
            return this;
        }

        public DocItem build() {
            return new DocItem(docId, title, fromId, date);
        }
    }
}
