package com.yidian.push.request;

import com.google.gson.annotations.SerializedName;
import com.yidian.push.utils.GsonFactory;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by yidianadmin on 15-1-12.
 */
@Getter
@Setter
public class Request {
    @SerializedName("_id")
    private String id;
    private List<String> platform;
    private String hash;
    @SerializedName("docid")
    private String docId;
    private String title;
    private String date;
    private String channel;
    private String createTime;
    @SerializedName("userids")
    private List<String> userIds;
    private String type;
    private String operator;
    private String key;

    public static Request buildRequest(String str) {
        return GsonFactory.getNonPrettyGson().fromJson(str, Request.class);
    }
}
