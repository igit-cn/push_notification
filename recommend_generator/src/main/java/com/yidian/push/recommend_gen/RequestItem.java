package com.yidian.push.recommend_gen;

import com.yidian.push.data.Platform;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

/**
 * Created by tianyuzhi on 15/9/12.
 */
@Getter
public class RequestItem {
    private String userId = null;
    private String model = null;
    private int num = 5;
    private Platform platform;
    private String appId;

    public RequestItem(String userId, String model, int num, Platform platform, String appId) {
        this.userId = userId;
        this.model = model;
        this.platform = platform;
        this.appId = appId;
        this.num = num;
    }

    public boolean isValid() {
        return StringUtils.isNotEmpty(userId) && (null != platform) && (num > 0);
    }

    public static class Builder {
        private String userId = null;
        private String model = null;
        private Platform platform;
        private String appId;
        private int num = 5;

        public Builder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder withModel(String model) {
            this.model = model;
            return this;
        }

        public Builder withNum(int num) {
            this.num = num;
            return this;
        }

        public Builder withPlatform(Platform platform) {
            this.platform = platform;
            return this;
        }

        public Builder withAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public RequestItem build() {
            return new RequestItem(userId, model, num, platform, appId);
        }
    }
}
