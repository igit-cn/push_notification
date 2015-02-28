package com.yidian.push.push_request;

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
    private String pushType;
    private String pushChannel; //
    private int nid;
    private int useChannel;
    private int sound;


    public PushRecord(int uid, List<String> tokens, String appId,
                      String docId, String title, String pushType,
                      String pushChannel, int nid, int useChannel, int sound) {
        this.uid = uid;
        this.tokens = tokens;
        this.appId = appId;
        this.docId = docId;
        this.title = title;
        this.pushType = pushType;
        this.pushChannel = pushChannel;
        this.nid = nid;
        this.useChannel = useChannel;
        this.sound = sound;
    }

    public void addToken(String token) {
        if (tokens == null) {
            tokens = new ArrayList<>();
        }
        tokens.add(token);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(uid).append(FILED_SEPARATOR).append(StringUtils.join(tokens, TOKEN_SEPARATOR))
                .append(FILED_SEPARATOR).append(appId)
                .append(FILED_SEPARATOR).append(docId)
                .append(FILED_SEPARATOR).append(title)
                .append(FILED_SEPARATOR).append(pushType)
                .append(FILED_SEPARATOR).append(pushChannel)
                .append(FILED_SEPARATOR).append(nid)
                .append(FILED_SEPARATOR).append(useChannel)
                .append(FILED_SEPARATOR).append(sound);
        return sb.toString();
    }
}
