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
}
