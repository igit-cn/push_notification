package com.yidian.push.instant.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyuzhi on 15/12/24.
 */
@Setter
@Getter
public class DocChannelInfo {
    /*
    {"docid":"0Beo8Sjb",
    "channels":[{"signature":"6cc4a0e87b2cd8a1411b5d8c688c2d6c","name":"保定","fromId":"u751","r":0.46035966835312947,"score":0.330924391746521},
        {"signature":"edacbca9bc49f7c4c47483e526083fc5","name":"时政","fromId":"c1","r":0.7876020669937134,"score":0.3630845248699188},
        {"signature":"d5edd57bccf7de7d2e52c4597ec3305e","name":"黑龙江","fromId":"u639","r":0.3614508301787491,"score":0.2612099349498749},
        {"signature":"e5cf504f13539245c5294858271a8017","name":"PM2.5","fromId":"u5906","r":0.554785436125961,"score":0.3097524642944336},
         {"signature":"a43ddc828a9da20befb017952616803d","name":"京津冀","fromId":"u9129","r":0.6210845053980729,"score":0.28750675916671753}],
     "modifiedAt":1450075994132}
     */
    @SerializedName("docid")
    private String docId = null;
    private List<Channel> channels = null;
    private long modifiedAt = 0;
    private String matchedQueryTag = null;


    public List<Channel> getChannels() {
        if (channels == null) {
            return new ArrayList<>(0);
        }
        else {
            return channels;
        }
    }

}
