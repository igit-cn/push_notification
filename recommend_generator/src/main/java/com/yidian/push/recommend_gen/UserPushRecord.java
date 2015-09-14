package com.yidian.push.recommend_gen;

import com.yidian.push.data.PushType;

import java.util.List;

/**
 * Created by tianyuzhi on 15/9/12.
 */
public class UserPushRecord {
    public static class DocId_PushType {
        public String docId;
        public PushType pushType; // give the push_type by score.
    }

    private String userId = null;
    private List<DocId_PushType> docIdPushTypeList;
}
