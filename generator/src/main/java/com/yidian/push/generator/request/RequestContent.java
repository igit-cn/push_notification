package com.yidian.push.generator.request;

import com.google.gson.annotations.SerializedName;
import com.yidian.push.data.Platform;
import com.yidian.push.utils.GsonFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by yidianadmin on 15-1-12.
 */
@Getter
@Setter
public class RequestContent {
    transient private static int VALID_MAX_HEAD_LENGTH = 15;
    @SerializedName("_id")
    private String id;
    private List<Platform> platform;
    private String hash;
    @SerializedName("docid")
    private String docId;
    private String title;
    private String head = "";
    private String date;
    @SerializedName("channel")
    private String newsChannel;
    private String createTime;
    @SerializedName("userids")
    private List<String> userIds;
    private String type;
    private String operator;
    private String key;

    public static RequestContent buildRequestContent(String str) {
        return GsonFactory.getNonPrettyGson().fromJson(str, RequestContent.class);
    }

    public static RequestContent buildRequestContentFromFile(String file) throws IOException {
        String fileContent = FileUtils.readFileToString(new File(file));
        return buildRequestContent(fileContent);
    }
}
