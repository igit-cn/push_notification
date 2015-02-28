package com.yidian.push.push_request;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yidianadmin on 15-2-9.
 */
public class PushRecordBuilder {
    private int uid;
    private List<String> tokens;
    private String appId;
    private String docId;
    private String title;
    private String pushType;
    private String pushChannel;
    private int nid;
    private int useChanel;
    private int sound;
    public PushRecordBuilder addToken(String token) {
        if (tokens == null) {
            tokens = new ArrayList<>();
        }
        tokens.add(token);
        return this;
    }
    public PushRecordBuilder setUid(int uid) {
        this.uid = uid;
        return this;
    }
    public PushRecordBuilder setAppId(String appId) {
        this.appId = appId;
        return this;
    }
    public PushRecordBuilder setDocId(String docId) {
        this.docId = docId;
        return this;
    }
    public PushRecordBuilder setTitle(String title) {
        this.title = title;
        return this;
    }
    public PushRecordBuilder setPushType(String pushType){
        this.pushType = pushType;
        return this;
    }
    public PushRecordBuilder setPushChannel(String pushChannel) {
        this.pushChannel = pushChannel;
        return this;
    }
    public PushRecordBuilder setNid(int nid) {
        this.nid = nid;
        return this;
    }
    public PushRecordBuilder setUseChannel(int useChannel) {
        this.useChanel = useChannel;
        return this;
    }
    public PushRecordBuilder setSound(int sound) {
        this.sound = sound;
        return this;
    }

    public PushRecord build() {
        return new PushRecord(uid, tokens, appId, docId, title, pushType, pushChannel, nid, useChanel, sound);
    }
}
