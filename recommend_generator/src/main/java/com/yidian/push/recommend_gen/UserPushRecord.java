package com.yidian.push.recommend_gen;

import com.yidian.push.data.PushType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by tianyuzhi on 15/9/12.
 */
@Getter
@Setter
public class UserPushRecord {
    public static class DocId_PushType {
        public String docId;
        public PushType pushType; // give the push_type by score.
        public DocId_PushType(String docId, PushType pushType) {
            this.docId = docId;
            this.pushType = pushType;
        }
    }


    private String userId = null;
    private List<DocId_PushType> docIdPushTypeList;

    public UserPushRecord(String userId, List<DocId_PushType> docId_pushTypeList) {
        this.userId = userId;
        this.docIdPushTypeList = docId_pushTypeList;
    }
}
