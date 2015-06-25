package com.yidian.push.generator;

import com.yidian.push.data.PushType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by tianyuzhi on 15/6/18.
 */
@Setter
@Getter
public class Task {
    private String pushTitle = "";
    private String pushDocId = "";
    private String pushHash = "";
    private PushType pushType;
    private String pushDate;
    private List<String> pushChannel;
    private String pushHead = "";
    private String table = "";
    private List<String> appIdInclude = null;
    private List<String> appIdExclude = null;
    private int protectMinutes = 0;
    private int startTime = 6 * 60;
    private int endTime = 24 * 60 - 1;

    public Task(String pushTitle, String pushDocId, String pushHash,
                PushType pushType, String pushDate,
                List<String> pushChannel, String pushHead, String table,
                List<String> appIdInclude, List<String> appIdExclude,
                int protectMinutes, int startTime, int endTime) {
        this.pushTitle = pushTitle;
        this.pushDocId = pushDocId;
        this.pushHash = pushHash;
        this.pushType = pushType;
        this.pushDate = pushDate;
        this.pushChannel = pushChannel;
        this.pushHead = pushHead;
        this.table = table;
        this.appIdExclude = appIdInclude;
        this.appIdExclude = appIdExclude;
        this.protectMinutes = protectMinutes;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static class Builder {
        private String pushTitle = "";
        private String pushDocId = "";
        private String pushHash = "";
        private PushType pushType;
        private String pushDate;
        private List<String> pushChannel;
        private String pushHead = "";
        private String table = "";
        private List<String> appIdInclude = null;
        private List<String> appIdExclude = null;
        private int protectMinutes = 0;
        private int startTime = 6 * 60;
        private int endTime = 24 * 60 - 1;


        public Builder setPushTitle(String pushTitle) {
            this.pushTitle = pushTitle;
            return this;
        }

        public Builder setPushDocId(String pushDocId) {
            this.pushDocId = pushDocId;
            return this;
        }

        public Builder setPushHash(String pushHash) {
            this.pushHash = pushHash;
            return this;
        }

        public Builder setPushType(PushType pushType) {
            this.pushType = pushType;
            return this;
        }

        public Builder setPushDate(String pushDate) {
            this.pushDate = pushDate;
            return this;
        }

        public Builder setPushChannel(List<String> pushChannel) {
            this.pushChannel = pushChannel;
            return this;
        }

        public Builder setPushHead(String pushHead) {
            this.pushHead = pushHead;
            return this;
        }

        public Builder setTable(String table) {
            this.table = table;
            return this;
        }

        public Builder setAppIdInclude(List<String> appIdInclude) {
            this.appIdInclude = appIdInclude;
            return this;
        }

        public Builder setAppIdExclude(List<String> appIdExclude) {
            this.appIdExclude = appIdExclude;
            return this;
        }

        public Builder setProtectMinutes(int protectMinutes) {
            this.protectMinutes = protectMinutes;
            return this;
        }

        public Builder setStartTime(int startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder setEndTime(int endTime) {
            this.endTime = endTime;
            return this;
        }

        public Task build() {
            return new Task(pushTitle, pushDocId, pushHash,
                    pushType, pushDate,
                    pushChannel, pushHead, table,
                    appIdInclude, appIdExclude,
                    protectMinutes, startTime, endTime);
        }
    }


}
