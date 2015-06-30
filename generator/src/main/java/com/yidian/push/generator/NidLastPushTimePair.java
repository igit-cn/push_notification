package com.yidian.push.generator;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by tianyuzhi on 15/6/29.
 */
@Getter
@Setter
public class NidLastPushTimePair {
    public int nid;
    public long lastPushTime;
    public NidLastPushTimePair(int nid, long lastPushTime) {
        this.nid = nid;
        this.lastPushTime = lastPushTime;
    }

    public String toString() {
        return "" + nid + "," + lastPushTime;
    }
}
