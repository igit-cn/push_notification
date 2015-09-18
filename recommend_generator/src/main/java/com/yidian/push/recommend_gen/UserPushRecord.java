package com.yidian.push.recommend_gen;

import com.yidian.push.data.Platform;
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
    private static final String CTR_A = "\u0001";
    private static final String CTR_B = "\u0002";
    private static final String CTR_C = "\u0003";
    public static class DocId_PushType {
        public String docId;
        public PushType pushType; // give the push_type by score.
        public DocId_PushType(String docId, PushType pushType) {
            this.docId = docId;
            this.pushType = pushType;
        }
    }


    private String userId = null;
    private Platform platform;
    private String appId;
    private List<DocId_PushType> docIdPushTypeList;

    public UserPushRecord(String userId, Platform platform, String appId, List<DocId_PushType> docId_pushTypeList) {
        this.userId = userId;
        this.platform = platform;
        this.appId = appId;
        this.docIdPushTypeList = docId_pushTypeList;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(userId).append(CTR_A);
        if (null != docIdPushTypeList) {
            boolean isFirst = true;
            for (DocId_PushType docId_pushType : docIdPushTypeList) {
                if (!isFirst) {
                    sb.append(CTR_C);
                }
                isFirst = false;
                sb.append(docId_pushType.docId).append(CTR_B).append(docId_pushType.pushType.getString());
            }
        }
        return sb.toString();
    }
}
