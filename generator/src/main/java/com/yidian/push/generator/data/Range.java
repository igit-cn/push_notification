package com.yidian.push.generator.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by tianyuzhi on 15/6/18.
 */
@Getter
@Setter
public class Range {
    int start;
    int end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public String toString() {
        return "start:" + start + ", end:" + end;
    }
}
