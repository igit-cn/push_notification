package com.yidian.push.stats;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yidianadmin on 15-2-27.
 */
@Setter
@Getter
public class UserStat {
    private int id;
    private List<String> androidOpen;
    private List<String> iPhoneOpen;
    private List<String> open;
    private List<String> read;
    private List<String> docs;

    public UserStat() {

    }
}
