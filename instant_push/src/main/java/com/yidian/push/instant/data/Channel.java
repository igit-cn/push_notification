package com.yidian.push.instant.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by tianyuzhi on 15/12/24.
 */
@Getter
@Setter
public class Channel {
    /*
    {"signature":"de70cb68fa2c7105c1a7db0695929d7a"
    ,"name":"jingxuan"
    ,"fromId":"u22592"
    ,"r":0.7
    ,"score":0.17900006473064423},
     */
    private String signature = null;
    private String name = null;
    private String fromId = null;
    @SerializedName("r")
    private double relevance = 0.0;
    private double score = 0.0;
}
