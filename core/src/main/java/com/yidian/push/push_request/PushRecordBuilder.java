package com.yidian.push.push_request;

import com.yidian.push.data.PushChannel;

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
    private String head;
    private int newsType;
    private String newsChannel;
    private int nid = 0; // xiaomi notify id [0,4]
    private PushChannel pushChannel;
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
    public PushRecordBuilder setHead(String head) {
        this.head = head;
        return this;
    }
    public PushRecordBuilder setNewsType(int newsType){
        this.newsType = newsType;
        return this;
    }
    public PushRecordBuilder setNewsChannel(String newsChannel) {
        this.newsChannel = newsChannel;
        return this;
    }
    public PushRecordBuilder setNid(int nid) {
        this.nid = nid;
        return this;
    }
    public PushRecordBuilder setPushChannel(PushChannel pushChannel) {
        this.pushChannel = pushChannel;
        return this;
    }
    public PushRecordBuilder setSound(int sound) {
        this.sound = sound;
        return this;
    }

    public PushRecord build() {
        return new PushRecord(uid, tokens, appId, docId, title, head, newsType, newsChannel, nid, pushChannel, sound);
    }
}
