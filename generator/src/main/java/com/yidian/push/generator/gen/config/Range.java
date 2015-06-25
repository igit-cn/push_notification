package com.yidian.push.generator.gen.config;

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
}
